package uk.co.mmscomputing.device.capi;

import java.io.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class CapiNCC extends CapiChannel{

  protected CapiInputStream  in;
  protected CapiOutputStream out;

  private   boolean          outgoing;

  private   int              reason           = -1;
  private   Semaphore        ds               = new Semaphore(0,true);

  CapiNCC(CapiApplication appl,ConnectB3Conf msg){     // outgoing call
    super(appl,msg.lineid);

    outgoing=true;
    in =new CapiInputStream(this);
    out=new CapiOutputStream(this);
  }

  CapiNCC(CapiApplication appl,ConnectB3Ind msg){      // incoming call
    super(appl,msg.lineid);

    outgoing=false;
    in =new CapiInputStream(this);
    out=new CapiOutputStream(this);
  }

  boolean isOutgoing(){ return outgoing;}
  boolean isIncoming(){ return !outgoing;}

  void received(ConnectB3ActiveInd msg){
    if(checkIsNotConnected()){
    }
  }

  void received(DataB3Ind msg)throws CapiException{
    if(isConnected()){
      in.received(msg);
    }else{
      appl.put(new DataB3Resp(msg.appid,msg.lineid,msg.getHandle())); 
    }
  }

  void sendDataResp(DataB3Resp msg)throws CapiException{ // called from CapiInputStream when DataB3Ind has been processed 
    appl.put(msg);                                     
  }

  void received(DataB3Conf msg)throws CapiException{
    out.received(msg);
  }

  void disconnect(int r)throws CapiException{            // call only from CapiApplication thread
    reason=(r==0)?0x3410:r;
    checkIsNotDisconnected();                            // passive disconnect initiated by peer
    in.disconnect();                                     // close input & ouput stream
    out.disconnect();                                    // and waste data if any in output queue
    ds.release();                                        // release disconnect semaphore
  }

  int  getDisconnectReason(){return reason;}

  void received(DisconnectB3Ind msg)throws CapiException{  // cannot send anything anymore
    reason=msg.getReason();
    disconnect(reason);
  }

  void selectFaxProtocol()throws CapiException{
    appl.getPLC(lineid&0x0000FFFF).selectFaxProtocol();
  }

  // Input or Output Thread calls

  public CapiInputStream getInputStream()throws IOException{return in;}
  public CapiOutputStream getOutputStream()throws IOException{return out;}

  DataB3Req write(int handle, byte[] b, int len)throws IOException{    
    DataB3Req req=new DataB3Req(applid,lineid,handle,0x00,b,len);
    appl.put(req);
    return req;
  }

  private void activeDisconnect()throws CapiException{    
    if(checkIsNotDisconnected()){                      // if not disconnected yet
      appl.put(new DisconnectB3Req(applid,lineid));    // send DisconnectB3Req and
    }
    try{ds.acquire();}catch(InterruptedException ie){} // wait for DisconnectB3Ind
    ds.release();                                      // release other threads
  }

  void closedInput()throws IOException{                // called from CapiInputStream
    if(out.isOpen()){return;}             
    activeDisconnect();                                // application closed both streams now 
  }

  void closedOutput()throws IOException{               // called from CapiOutputStream
    if(in.isOpen()){return;}               
    activeDisconnect();                                // application closed both streams now
  }

  public void close()throws IOException{               // do not call from CapiApplication thread
    if(checkIsOpen()){
      in.close();
      out.close();                                     // wait until all DataB3Conf come in
    }
  }
}