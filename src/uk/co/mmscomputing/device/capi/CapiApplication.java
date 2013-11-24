package uk.co.mmscomputing.device.capi;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.concurrent.*;

import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.q931.*;
import uk.co.mmscomputing.device.capi.parameter.*;
import uk.co.mmscomputing.device.capi.exception.*;
import uk.co.mmscomputing.device.capi.facility.*;

public class CapiApplication extends Thread implements CapiConstants{

  protected int               appid=-1;
  protected int               maxLogicalCon=-1;   
  protected boolean           listeningToSupServices=false;   // listen to Supplementary Services

  protected Metadata          md=null;
  private   boolean           running=true;
  private   Semaphore         rs=new Semaphore(0,true);
  private   Vector            plcs=new Vector();              // set of active physical  connections

  public CapiApplication(Metadata md)throws CapiException{
    this.md=md;
    md.getInt(capiSpeechCodingID,LAYER1USERINFO_ALAW);        // use A-Law, if not set
    maxLogicalCon=md.getInt(capiMaxLogicalConnectionsID,2);   // use two b-channels, if not set

    jcapi.checkInstalled();
    appid=jcapi.register(maxLogicalCon,MaxNumB3DataBlocks,DefaultB3DataBlockSize);
  }
    
  public int     getApplID(){ return appid;}
  public boolean isRunning(){ return running;}

  public boolean isListeningToSupService(){ return listeningToSupServices;}

  protected CapiController getController()throws CapiException{
    int ctrlid=md.getInt(capiControllerID,1);                 // System.out.println("\b5capi.controller.id "+ctrlid);
    return CapiEnumerator.getController(ctrlid);
  }

  protected int getControllerId()throws CapiException{
    return getController().getId();
  }

/*synchronized*/public void put(MsgOut msg)throws CapiException{// THE ONLY place where we send our capi req or resp
    if(!running){return;}
//    if(msg.cmd!=CAPI_DATA_B3){md.fireListenerUpdate(msg);}  // testing; tell listeners; somebody might want to know.
    jcapi.putMessage(appid,msg.getBytes());
  }

  public void close(){
    if(!running){return;}
    try{
      int cntrl = getControllerId();
//      put(new ListenReq(appid,cntrl,0));                    // stop listening
      closePLCs();
      running=false;
      MsgOut msg=new ListenReq(appid,cntrl,0);                  // send dummy message to unblock jcapi.waitForMessage
      jcapi.putMessage(appid,msg.getBytes());                   // cannot use put here! running is false

      try{rs.acquire();}catch(InterruptedException ie){}
      System.err.println(getClass().getName()+".close: RELEASED CAPI APPLICATION "+appid);
    }catch(Exception ce){
      System.err.println(getClass().getName()+".close:\n\t"+ce);
      md.fireListenerUpdate("9\b"+getClass().getName()+".close:\n\t"+ce);
    }
  }

  public CapiPLC getPLC(int plci)throws CapiException{
    plci&=0x0000FFFF;
    if(plci<256){return null;}                                // msg for controller only

    for(Enumeration e=plcs.elements();e.hasMoreElements();){
      CapiPLC plc=(CapiPLC)e.nextElement();
      if(plc.lineid==plci){
        return plc;
      }
    }
    throw new CapiException(getClass().getName()+".getPLC("+plci+")\n\tCannot find PLC with plci = 0x"+Integer.toHexString(plci)+" !");
  }

  private CapiPLC newPLC(ConnectInd msg){
    CapiPLC plc=new CapiPLC(this,msg);plcs.add(plc);return plc;
  }

  protected CapiPLC newPLC(ConnectReq msg){
    CapiPLC plc=new CapiPLC(this,msg);plcs.add(plc);return plc;
  }

  private CapiPLC getPLC(ConnectConf msg)throws CapiException{
    int msgno = msg.msgno;
    for(Enumeration e=plcs.elements();e.hasMoreElements();){
      CapiPLC plc=(CapiPLC)e.nextElement();
      if(plc.getMsgno()==msgno){
        plc.setLineid(msg.lineid);
        return plc;
      }
    }
    throw new CapiException(getClass().getName()+".getPLC("+msgno+")\n\tCannot find PLC with connect msgno = "+msgno+" !");
  }

  public void closePLCs(){
    CapiPLC[] lines=(CapiPLC[])plcs.toArray(new CapiPLC[0]);
    for(int i=0;i<lines.length;i++){
      try{
        lines[i].close();
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

//  ---- Conf/Ind methods

  protected void received(CapiPLC plc,ConnectInd msg)throws CapiException{
    put(new ConnectResp(msg.appid,msg.lineid,IGNORE));         // ignore call
    // see CapiServerApplication
  }

  protected void received(CapiNCC ncc,ConnectB3ActiveInd msg)throws CapiException{
    BProtocol protocol=ncc.getProtocol();
    NCPI ncpi=msg.getNCPI(protocol);
    if(ncpi!=null){md.fireListenerUpdate(ncpi);}               // ncpi gives some useful info; i.e. T30 Fax protocol

    if(ncpi!=null){
      switch(protocol.B3Protocol){
      case CAPI_PROTOCOL_T30_FAX: break;
      case CAPI_PROTOCOL_ISO8208: break;
      case CAPI_PROTOCOL_MODEM:   put(new V42InfoReq(msg.appid,msg.lineid));break;
      }
    }
  }

  protected void receivedResetB3Ind(CapiNCC ncc,ResetB3Ind msg)throws CapiException{
    NCPI ncpi=msg.getNCPI(ncc.getProtocol());
    if(ncpi!=null){md.fireListenerUpdate(ncpi);}               // ncpi gives some useful info; i.e. T30 Fax protocol
//    ncc.receivedResetB3Ind((ResetB3Ind)msg);
  }

  protected void receivedConnectB3T90ActiveInd(CapiNCC ncc,ConnectB3T90ActiveInd msg)throws CapiException{
    NCPI ncpi=msg.getNCPI(ncc.getProtocol());
    if(ncpi!=null){md.fireListenerUpdate(ncpi);}               // ncpi gives some useful info; i.e. T30 Fax protocol
//    ncc.receivedConnectB3T90ActiveInd((ConnectB3T90ActiveInd)msg);
  }

// ---- private methods

  private void handleIndMsg(CapiIndMsg msg)throws CapiException{
    CapiPLC plc=null;CapiNCC ncc=null;

    if((msg.lineid&0x0000FF00)!=0){
      if(msg.cmd==CAPI_CONNECT){                                 
        plc=newPLC((ConnectInd)msg);                           // create new plc
      }else{
        plc=getPLC(msg.lineid);                                // get existing plc
      }
      if((msg.lineid&0xFFFF0000)!=0){
        if(msg.cmd==CAPI_CONNECT_B3){                            
          ncc=plc.addNCC((ConnectB3Ind)msg);                   // create ncc
        }else{
          ncc=plc.getNCC(msg.lineid);
        }
      }
    }
    switch(msg.cmd){
    case CAPI_DATA_B3: ncc.received((DataB3Ind)msg); break;    // receive incoming data
    case CAPI_ALERT:   break;
    case CAPI_CONNECT:                                         // incoming call
      plc.setSpeechCoding(md.getInt(capiSpeechCodingID));      // default set by main application
      received(plc,(ConnectInd)msg);                           // let subclasses handle it
      break;
    case CAPI_CONNECT_ACTIVE:                                  // physically connected
      put(new ConnectActiveResp(msg.appid,msg.lineid));        // tell capi to set up call
      plc.received((ConnectActiveInd)msg);
      break;
    case CAPI_CONNECT_B3:                                      // incoming connection in set-up phase
      put(new ConnectB3Resp(msg.appid,msg.lineid));            // accept call

      NCPI ncpi=((ConnectB3Ind)msg).getNCPI(ncc.getProtocol());
      if(ncpi!=null){md.fireListenerUpdate(ncpi);}             // ncpi gives some useful info; i.e. T30 Fax protocol
      if(ncpi!=null){
        switch(ncc.getProtocol().B3Protocol){
        case CAPI_PROTOCOL_T30_FAX: break;
        case CAPI_PROTOCOL_ISO8208: break;
        case CAPI_PROTOCOL_MODEM:   put(new V42InfoReq(msg.appid,msg.lineid));break;
        }
      }
      break;
    case CAPI_CONNECT_B3_ACTIVE:                               // connected
      ncc.received((ConnectB3ActiveInd)msg);                   // ncc is open for business now
      ncc.setPickedUp(plc.getPickedUp());                      // if plc has been picked up manually set ncc pickedUp=true
      received(ncc,(ConnectB3ActiveInd)msg);                   // tell CapiServerApplication about new connection
      put(new ConnectB3ActiveResp(msg.appid,msg.lineid));
      ncc.releasePickupBlocker();                              // This releases blocker in (outgoing) CapiCallApplication.connect
                                                               // or (incoming) in CapiServerApplication$Pickup.indicateCall
      break;
    case CAPI_CONNECT_B3_T90_ACTIVE:
      if(!ncc.isOpen()){
        receivedConnectB3T90ActiveInd(ncc,(ConnectB3T90ActiveInd)msg);
        put(new ConnectB3T90ActiveResp(msg.appid,msg.lineid));
      }
      break;
    case CAPI_DISCONNECT_B3:
      put(new DisconnectB3Resp(msg.appid,msg.lineid));
      ncc.received((DisconnectB3Ind)msg);
      plc.remove(ncc);                                         // capi won't send anything anymore to this ncc
      break;
    case CAPI_DISCONNECT:
      plc.received((DisconnectInd)msg);
      put(new DisconnectResp(msg.appid,msg.lineid));	       
      plcs.remove(plc);
      break;
    case CAPI_FACILITY:
      plc.received((FacilityInd)msg);
      break;
    case CAPI_INFO:
      put(new InfoResp(msg.appid,msg.lineid));
/*
      if(msg instanceof InfoInd.IIBearerCapability){                      
        BearerCapability bc=((InfoInd.IIBearerCapability)msg).bc;
        plc.setSpeechCoding(bc.getSpeechCoding());
      }
*/
      break;
    case CAPI_RESET_B3:
      receivedResetB3Ind(ncc,(ResetB3Ind)msg);
      put(new ResetB3Resp(msg.appid,msg.lineid));
      break;
    case CAPI_MANUFACTURER:
      break;
    default:
      System.err.println("3\b"+getClass().getName()+".handleIndMsg:\n\tUnknown indication message\n\t"+msg);
      break;
    }
  }

  private void handleConfMsg(CapiConfMsg msg)throws IOException{
    CapiPLC plc=null;CapiNCC ncc=null;

    if((msg.lineid&0x0000FF00)!=0){
      if(msg.cmd==CAPI_CONNECT){                               // outgoing call
        plc=getPLC((ConnectConf)msg);                          // get plc with msg.msgno that belonged to ConnectReq
      }else{
        plc=getPLC(msg.lineid);                                // get existing plc
      }
      if((msg.lineid&0xFFFF0000)!=0){
        if(msg.cmd==CAPI_CONNECT_B3){                          // outgoing connection in set-up phase
          ncc=plc.addNCC((ConnectB3Conf)msg);                  // create ncc
        }else{
          ncc=plc.getNCC(msg.lineid);
        }
      }
    }

    int info=msg.getInfo();
    if((info&0x0000FF00)==0){                                  // no errors occurred
      switch(msg.cmd){
      case CAPI_DATA_B3:                                       // data out confirmation
        ncc.received((DataB3Conf)msg);
        break;
      case CAPI_ALERT:
        break;
      case CAPI_CONNECT:                                       // outgoing call
        plc.setSpeechCoding(md.getInt(capiSpeechCodingID));    // default set by main application
        break;
      case CAPI_CONNECT_B3:                                    // outgoing connection in set-up phase
        break;
      case CAPI_DISCONNECT_B3:                                 // logical connection clear down has been initiated
//        ncc.received((DisconnectB3Conf)msg);
        break;
      case CAPI_DISCONNECT:
        break;
      case CAPI_FACILITY:
        if(msg instanceof SupServiceConf.ListenConf){
          SupServiceConf.ListenConf lcmsg=(SupServiceConf.ListenConf)msg;
          if(lcmsg.getInfo()==0){
            listeningToSupServices=true;
          }else{
            System.err.println("SupServiceReq.ListenReq failed\n"+msg);
          }
        }
        if(plc!=null){
          plc.received((FacilityConf)msg);
        }
        break;
      case CAPI_INFO:
        break;
      case CAPI_LISTEN:
        break;
      case CAPI_MANUFACTURER:
        break;
      case CAPI_RESET_B3:
        break;
      case CAPI_SELECT_B_PROTOCOL:
        System.err.println(getClass().getName()+": Switched Protocol: 0x"+Integer.toHexString(msg.lineid));
//        System.err.println(msg.toString());
        break;
      default:
        System.err.println("3\b"+getClass().getName()+".handleConfMsg:\n\tUnknown confirmation message\n\t"+msg);
        break;
      }
    }else{                                                     // some error occurred
      switch(msg.cmd){
      case CAPI_FACILITY:
        plc.received((FacilityConf)msg);
        break;
      case CAPI_CONNECT_B3:                                    // outgoing connection failed in set-up phase
        put(new DisconnectReq(msg.appid,msg.lineid));          // disconnect line
        break;
      }
    }
  }

  public void run( ){                                          // Runnable interface
    MsgIn  msg;
    Rider  r=new Rider();
    byte[] data=new byte[128];

    Thread.currentThread().setName(getClass().getName()+"."+getApplID());

    while(running){
      msg=null;
      try{
        jcapi.waitForMessage(appid);                           // wait for capi message
        if(!running){break;}
        data=jcapi.getMessage(appid,data);                     // use msg byte array if big enough otherwise return new byte array
        r.set(data);
        msg=MsgIn.create(r);                                   // turn capi message into java object

        if(msg.cmd!=CAPI_DATA_B3){md.fireListenerUpdate(msg);} // testing; tell listeners; somebody might want to know.

        if(msg.scmd==CAPI_CONF){                               // if confirmation message
          handleConfMsg((CapiConfMsg)msg);
        }else{                                                 // else indication message
          handleIndMsg((CapiIndMsg)msg);
        }
      }catch(CapiException cioe){
        cioe.printStackTrace();
        if(msg!=null){System.err.println(msg.toString());}
        md.fireListenerUpdate(cioe);
      }catch(Throwable t){
        t.printStackTrace();
        if(msg!=null){System.err.println(msg.toString());}
        md.fireListenerUpdate("9\b"+getClass().getName()+".run:\n\tFatal Error; Stopped capi application thread "+appid+".\n\t"+t);
        break;
      }
    }
    try{
      jcapi.release(appid);		                                 // release resources
      rs.release();
      System.err.println(getClass().getName()+".run: RELEASED CAPI APPLICATION "+appid);
    }catch(Exception e){e.printStackTrace();}
  }

  protected void finalize()throws Throwable{
    close();super.finalize();
  }
}