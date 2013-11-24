package uk.co.mmscomputing.device.capi;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;
import uk.co.mmscomputing.device.capi.facility.*;

public class CapiPLC extends CapiChannel{

  private   boolean          ischangingprotocols = false;

  private   boolean          outgoing   = false;
  private   Vector           nccs       = new Vector();  // set of logical  connections

  private   int              msgno      = 0;

  private   int              reason     = -1;            // disconnect reason
  private   Semaphore        ds         = new Semaphore(0,true);


  private   int              isonhold   = 0;

  public CapiPLC(CapiApplication appl,ConnectInd msg){
    super(appl,msg.lineid);
    this.outgoing=false;
    this.isopen=true;
  }

  public CapiPLC(CapiApplication appl,ConnectReq msg){
    super(appl,0);
    this.outgoing=true;
    this.isopen=true;
    this.msgno=msg.msgno;                                // with msgno we will be able to map a ConnectConf to this ConnectReq
  }

  int  getMsgno(){return msgno;}                         // called from CapiApplication
  void setLineid(int pcli){lineid=pcli;}                 // called from CapiApplication

  public CapiNCC getNCC(int ncci)throws CapiException{
    CapiNCC ncc;
    for(Enumeration e=nccs.elements();e.hasMoreElements();){
      ncc=(CapiNCC)e.nextElement();
      if(ncc.getLineID()==ncci){return ncc;}
    }
    CapiException ce = new CapiException(getClass().getName()+".getNCC(0x"+Integer.toHexString(ncci)+")\n\tCannot find NCC.");
    ce.printStackTrace();
    throw ce;
  }

  public CapiNCC getNCC(){return (CapiNCC)nccs.get(0);}

  private void addNCC(CapiNCC ncc){
    nccs.add(ncc);
    ncc.setProtocol(protocol);
    ncc.setSpeechCoding(getSpeechCoding());
    ncc.setLocalNo(getLocalNo());
    ncc.setRemoteNo(getRemoteNo());
    ncc.setPlugin(getPlugin());
    ncc.setPickupBlocker(getPickupBlocker());
  }

  CapiNCC addNCC(ConnectB3Ind msg){
    CapiNCC ncc=new CapiNCC(appl,msg);addNCC(ncc);return ncc;
  }

  CapiNCC addNCC(ConnectB3Conf msg){
    CapiNCC ncc=new CapiNCC(appl,msg);addNCC(ncc);return ncc;
  }

  void remove(CapiNCC ncc)throws CapiException{

//    System.err.println("REMOVE NCC = 0x"+Integer.toHexString(lineid));

    nccs.remove(ncc);

    // receive this signal everytime an ncc has received a DisconnectB3Ind

    if(nccs.isEmpty()){
      if(ischangingprotocols){
        ischangingprotocols=false;
        appl.put(new SelectBProtocolReq(applid,lineid,protocol));
      }else if(isonhold==1){                             // put "on hold" in process; don't disconnect plc
      }else{
        activeDisconnect();
      }
    }
  }

  public CapiInputStream getInputStream(int ncci)throws IOException{
    return getNCC(ncci).getInputStream();
  }

  public CapiOutputStream getOutputStream(int ncci)throws IOException{
    return getNCC(ncci).getOutputStream();
  }

  public CapiInputStream getInputStream()throws IOException{
    return ((CapiNCC)nccs.get(0)).getInputStream();
  }

  public CapiOutputStream getOutputStream()throws IOException{
    return ((CapiNCC)nccs.get(0)).getOutputStream();
  }

  void received(ConnectActiveInd msg)throws CapiException{
    if(outgoing){
      appl.put(new ConnectB3Req(msg.appid,msg.lineid));
    }
  }

  int  getDisconnectReason(){return reason;}

  void received(DisconnectInd msg)throws CapiException{  // No messages will be sent to plc after DisconnectInd
    isonhold=0;
    reason=msg.getErrNo();                             // 0x34xx are cause values from network. Q.850/ETS 300 102 - 1: Octet 4
    if(checkIsNotDisconnected()){

      CapiNCC[] lines=(CapiNCC[])nccs.toArray(new CapiNCC[0]);
      for(int i=0;i<lines.length;i++){                 // tell nccs about disconnect if still open
        lines[i].disconnect(reason);
        nccs.remove(lines[i]);
      }

    }
    ds.release();                                      // release disconnect semaphore
    releasePickupBlocker();                            // if a thread is waiting for a connection release (CapiServerApplication.connect)
  }

  public void selectProtocol(BProtocol protocol)throws CapiException{
    if(isopen){
      ischangingprotocols=true;
      this.protocol=protocol;

      CapiNCC[] lines=(CapiNCC[])nccs.toArray(new CapiNCC[0]);
      for(int i=0;i<lines.length;i++){
        try{
          lines[i].setChangedProtocol(true);
          lines[i].close();                                // CapiNCC.close will wait for DisconnectB3Ind
        }catch(IOException ioe){
          ioe.printStackTrace();
        }
      }
    }
  }

  public void selectFaxProtocol()throws CapiException{
    selectProtocol(new FaxBProtocol(0,0,"",""));
  }

  private void activeDisconnect()throws CapiException{
    if(checkIsNotDisconnected()){
      appl.put(new DisconnectReq(applid,lineid));
    }
  }

  public void close()throws IOException{                   // called from none capi thread
    if(checkIsOpen()){
      CapiNCC[] lines=(CapiNCC[])nccs.toArray(new CapiNCC[0]);
      for(int i=0;i<lines.length;i++){                     // close all open nccs
        try{
          lines[i].close();                                // CapiNCC.close will wait for DisconnectB3Ind
        }catch(IOException ioe){
          ioe.printStackTrace();
        }
      }                                                    // all nccs are closed
      activeDisconnect();                                  // active disconnect
    }
    try{ds.acquire();}catch(InterruptedException ie){}     // wait for DisconnectInd
    ds.release();                                          // release another thread if waiting
  }

// ---- SUP SERVICE ----

// ---- HOLD/RETRIEVE ----

  public boolean isOnHold(){
    while(isonhold==1){                                    // wait until we receive HoldInd/RetrieveInd
      try{Thread.currentThread().sleep(100);}catch(Exception e){}
    }
    return isonhold==2;
  }

  public boolean hold()throws CapiException{
    if(isonhold==0){
      isonhold=1;
      appl.put(new SupServiceReq.HoldReq(applid,lineid));
    }
    return isOnHold();                                     // wait until we receive HoldInd
  }

  public boolean retrieve()throws CapiException{
    if(isonhold==2){
      isonhold=1;
      appl.put(new SupServiceReq.RetrieveReq(applid,lineid));
    }
    return !isOnHold();                                    // wait until we receive RetrieveInd
  }

  public CapiChannel retrieveChannel()throws CapiException{// retrieve B-Channel
    if(isDisconnected()){return null;}
    if(!isOnHold()){
      put(new ConnectB3Req(applid,lineid));
      while(nccs.size()==0){                               // wait until we receive new NCC
        try{Thread.currentThread().sleep(100);}catch(Exception e){}
      }
    }
    return getNCC();
  }

// ---- END : HOLD/RETRIEVE ----

// ---- ECT Explicit Call Transfer ----

  public void explicitCallTransferTo(CapiPLC active)throws CapiException{   // ECT
    if(!hold()){                                                            // Need one channel on hold
      throw new CapiException("Explicit Call Transfer: Cannot put call on hold.");
    }
    
    CapiNCC[] lines=(CapiNCC[])nccs.toArray(new CapiNCC[0]);
    for(int i=0;i<lines.length;i++){                                        // close all open nccs
      try{
        lines[i].close();                                                   // CapiNCC.close will wait for DisconnectB3Ind
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }                                                                       // all nccs are closed

//  The PLCI of the active connection is in the parameter PLCI, and the PLCI of the held connection is in
//  the parameter Facility Request Parameter/Supplementary Service-specific parameter/PLCI).

    SupServiceReq.ECTReq ectReq=new SupServiceReq.ECTReq(applid,active.getLineID(),getLineID());    // explicit call transfer
    appl.put(ectReq);
  }

// ---- END : ECT Explicit Call Transfer ----

  void received(SupServiceInd msg)throws CapiException{
    if(msg instanceof SupServiceInd.HoldInd){
      appl.put(new SupServiceResp.HoldResp(msg.appid,msg.lineid));
      isonhold=(((SupServiceInd.HoldInd)msg).getReason()==0)?2:0;
    }else if(msg instanceof SupServiceInd.RetrieveInd){
      appl.put(new SupServiceResp.RetrieveResp(msg.appid,msg.lineid));
      isonhold=(((SupServiceInd.RetrieveInd)msg).getReason()==0)?0:2;
    }
  }

  void received(SupServiceConf msg)throws CapiException{
    if(msg instanceof SupServiceConf.HoldConf){
      isonhold=(((SupServiceConf.HoldConf)msg).getInfo()==0)?1:0;
    }else if(msg instanceof SupServiceConf.RetrieveConf){
    }
  }

// ---- END : SUP SERVICE ----


// ---- FACILITY ----


  void received(FacilityInd msg)throws CapiException{
    if(msg instanceof DTMFInd){
      appl.put(new FacilityResp(msg.appid,msg.lineid,((FacilityInd)msg).getSelector()));
      Enumeration e=nccs.elements();
      while(e.hasMoreElements()){
        ((CapiNCC)e.nextElement()).receivedDTMFInd((DTMFInd)msg);
      }
    }else if(msg instanceof SupServiceInd){ 
      received((SupServiceInd)msg);
    }else{
      appl.put(new FacilityResp(msg.appid,msg.lineid,((FacilityInd)msg).getSelector()));
      System.err.println(msg);
      new Exception().printStackTrace();
    }
  }

  void received(FacilityConf msg)throws CapiException{
    if(msg instanceof DTMFConf){
      Enumeration e=nccs.elements();
      while(e.hasMoreElements()){
        ((CapiNCC)e.nextElement()).receivedDTMFConf((DTMFConf)msg);
      }
    }else if(msg instanceof SupServiceConf){
      received((SupServiceConf)msg);
    }else if(msg instanceof EchoCancellerConf.GetSupportedServicesConf){
      EchoCancellerConf.GetSupportedServicesConf ecc=(EchoCancellerConf.GetSupportedServicesConf)msg;
      appl.put(new EchoCancellerReq.EnableReq(msg.appid,msg.lineid,ecc.getOptions(),ecc.getMaxTailLength(),ecc.getMaxPreDelay()));
    }
  }

// ---- END : FACILITY ----


  public void setPlugin(CapiPlugin plugin){
    super.setPlugin(plugin);
    Enumeration e=nccs.elements();
    while(e.hasMoreElements()){
      ((CapiNCC)e.nextElement()).setPlugin(plugin);
    }
  }
}