package uk.co.mmscomputing.device.capi;

import java.io.IOException;

import uk.co.mmscomputing.concurrent.TimeUnit;
import uk.co.mmscomputing.concurrent.Semaphore;
import uk.co.mmscomputing.concurrent.ArrayBlockingQueue;
import uk.co.mmscomputing.util.metadata.Metadata;

import uk.co.mmscomputing.device.capi.ctrl.ListenReq;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.ConnectB3ActiveInd;
import uk.co.mmscomputing.device.capi.q931.BearerCapability;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;
import uk.co.mmscomputing.device.capi.protocol.SpeechProtocol;
import uk.co.mmscomputing.device.capi.exception.CapiException;

public class CapiServerApplication extends CapiCallApplication{
  
  protected ArrayBlockingQueue incoming = null;       // set of undealt incoming calls

  public CapiServerApplication(Metadata md)throws CapiException{
    super(md); 
    incoming = new ArrayBlockingQueue(maxLogicalCon);
  }

  public void run( ){                                 // Runnable interface
    try{
      int cipmask=md.getInt("capi.cipmask");

      CapiController[] controllers = CapiEnumerator.getControllers();
      for(int i=0;i<controllers.length;i++){
        put(new ListenReq(appid,controllers[i].getId(),cipmask));
      }
    }catch(IOException ioe){ 
      md.fireListenerUpdate(ioe);
      ioe.printStackTrace();
    }
    super.run();
  }

  public void close(){
    try{
      CapiController[] controllers = CapiEnumerator.getControllers();
      for(int i=0;i<controllers.length;i++){
        put(new ListenReq(appid,controllers[i].getId(),0)); // stop listening
      }
    }catch(CapiException ie){}
    super.close();
    try{
      incoming.put(null);                             // unblock accept; interrupt
    }catch(InterruptedException ie){}
  }

  public CapiChannel accept()throws InterruptedException{    
    return (CapiChannel)incoming.take();              // main application server thread : wait for incoming calls
  }

  protected void received(CapiPLC plc, ConnectInd msg)throws CapiException{
    new Thread(new Pickup(plc,msg)).start();          // incoming physical call
  }

  protected void received(CapiNCC ncc,ConnectB3ActiveInd msg)throws CapiException{
    try{
      super.received(ncc,msg);
      if(ncc.isIncoming()){                           // logical incoming call is connected now
        incoming.put(ncc);                            // add to incoming list, so that main app server
      }                                               // can 'accept' it.
    }catch(InterruptedException ie){
      throw new CapiException(ie.getMessage());
    }
  }

  private class Pickup implements Runnable{

    CapiPLC    plc;
    ConnectInd msg;

    public Pickup(CapiPLC plc, ConnectInd msg){
      this.plc=plc;
      this.msg=msg;
    }

    private void indicateCall()throws IOException{
      CapiMetadata.Indication mdi=new CapiMetadata.Indication(msg.lineid);

      mdi.setController(plc.getCtrlId());

      mdi.localno=msg.getLocalNo();
      mdi.remoteno=msg.getRemoteNo();

      int sp=md.getInt("capi.bandwidth");
      mdi.setProtocol(new SpeechProtocol(sp));          // use by default

      md.fireListenerUpdate(mdi);                       // main application needs to fill mdi struct

      BProtocol protocol=mdi.getProtocol();

      if(protocol instanceof SpeechProtocol){
        BearerCapability bc=msg.getBearerCapability();  // System.err.println(bc);
        int sc=bc.getSpeechCoding();
        if(sc!=-1){                                     // if we receive a valid BC struct use its speech coding
          plc.setSpeechCoding(sc);
        }
      }

      plc.setProtocol(protocol);
      plc.setLocalNo(mdi.localno);
      plc.setRemoteNo(mdi.remoteno);

      if(mdi.accept){
        Semaphore blocker=mdi.blocker;
        if(blocker!=null){                            // program wants to wait for a bit
          plc.setPickupBlocker(blocker);              // if peer disconnects plc can release this thread
          put(new AlertReq(msg.appid,msg.lineid));  	// tell telecom network that we listen
          try{                                        // wait until time runs out or some other thread released blocker
            blocker.tryAcquire(mdi.waitformsecs,TimeUnit.MILLISECONDS);
          }catch(InterruptedException ie){}
        }
      }

      if(!plc.isOpen()){return;}								      // disconnected by remote                    
      plc.setPickedUp(mdi.gotPickedUp);               // main app might want to know

      String localNo=mdi.localno;
      String localNoSub="";
      String[] slno=localNo.split("-");
      if(slno.length==2){ localNo=slno[0]; localNoSub=slno[1];}

      plc.setProtocol(mdi.getProtocol());
      plc.setPlugin(mdi.getPlugin());

      put(new ConnectResp(
        msg.appid,
        msg.lineid,
        mdi.rejectFlag,
        mdi.getProtocol(),
        localNo,
        localNoSub,
        StructOut.empty,
        StructOut.empty
      ));
    }

    public void run(){

      Thread.currentThread().setName(getClass().getName()+".0x"+Integer.toHexString(plc.getLineID()));

      try{ 
        indicateCall();
      }catch(IOException ioe){ 
        md.fireListenerUpdate(ioe);
      }
    }
  }

}