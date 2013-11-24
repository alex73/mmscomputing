package uk.co.mmscomputing.device.capi;

import java.io.*;

import uk.co.mmscomputing.concurrent.TimeUnit;
import uk.co.mmscomputing.concurrent.Semaphore;
import uk.co.mmscomputing.util.metadata.Metadata;

import uk.co.mmscomputing.device.capi.ctrl.ConnectReq;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class CapiCallApplication extends CapiApplication{
    
  public CapiCallApplication(Metadata md)throws CapiException{
    super(md);           
  }

// ---- connect methods ----

  public CapiChannel connect(ConnectReq msg,Semaphore blocker,long msecs)throws IOException{  // main application thread : wait for connection
    int reason;

    if(blocker==null){throw new CapiInformation(getClass().getName()+".connect:\n\tConnection request timed out.");}

    CapiPLC plc=newPLC(msg);                              // create new plc
    plc.setPickupBlocker(blocker);
    put(msg);                                             // send ConnectReq to capi-controller; It should ring now somewhere ! 
    try{
      if(msecs<3000){msecs=3000;}                         // try at least 3 secs; gives us some time to receive ConnectConf
      blocker.tryAcquire(msecs,TimeUnit.MILLISECONDS);    // wait until plc releases semaphore
    }catch(InterruptedException ie){}
    try{
      CapiNCC ncc = plc.getNCC();                         // throws ArrayIndexOutOfBoundsException if no ncc available
      if(ncc.isOpen()){return ncc;}                       // return open ncc
      plc.close();                                        // otherwise active disconnect
      reason = ncc.getDisconnectReason();
    }catch(ArrayIndexOutOfBoundsException aioobe){        // no logical connection yet
      plc.close();                                        // active disconnect
      reason = plc.getDisconnectReason();
    }
    if(reason==0x3490){throw new CapiInformation(getClass().getName()+".connect:\n\tConnection request timed out.");}
    throw new CapiIOException(reason);
  }

  public CapiChannel connect(ConnectReq msg,long msecs)throws IOException{  // main application thread : wait for connection
    return connect(msg,new Semaphore(0,true),msecs);
  }

  public CapiChannel connect(String remote,Semaphore s,long millisecs, BProtocol protocol)throws IOException{
    String local=md.getString("capi.localno");
    ConnectReq msg=new ConnectReq(appid,getControllerId(),CIP_SPEECH,remote,local,"","",protocol);
    return connect(msg,s,millisecs);
  }

  public CapiChannel connect(String remote,long millisecs, BProtocol protocol)throws IOException{
    return connect(remote,new Semaphore(0,true),millisecs,protocol);
  }

  public CapiChannel connect(String remote,long millisecs)throws IOException{
    int bandwidth=md.getInt("capi.bandwidth");
    return connect(remote,millisecs,new SpeechProtocol(bandwidth));
  }

  public CapiChannel faxconnect(String remote,Semaphore s,long millisecs,String header)throws IOException{
    CapiController ctrl=getController();
    if(!ctrl.isSupportingGroup3Fax()){
      throw new CapiException(getClass().getName()+".faxconnect:\n\tController ["+ctrl.getId()+"] does not support fax T.30 protocol.");
    }
    String local=md.getString("capi.localno");
    return connect(remote,s,millisecs,new FaxBProtocol(0,0,local,header));
  }

  public CapiChannel faxconnect(String remote,long millisecs,String header)throws IOException{
    return faxconnect(remote,new Semaphore(0,true),millisecs,header);
  }

  public CapiChannel faxconnect(String remote,Semaphore s,long millisecs,String local,String header)throws IOException{
    CapiController ctrl=getController();
    if(!ctrl.isSupportingGroup3Fax()){
      throw new CapiException(getClass().getName()+".faxconnect:\n\tController ["+ctrl.getId()+"] does not support fax T.30 protocol.");
    }
    return connect(remote,s,millisecs,new FaxBProtocol(0,0,local,header));
  }

  public CapiChannel faxconnect(String remote,long millisecs,String local,String header)throws IOException{
    return faxconnect(remote,new Semaphore(0,true),millisecs,local,header);
  }

}