package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.exception.*;
import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.parameter.*;
import uk.co.mmscomputing.device.capi.q931.*;

public class SimpleSpeechReceive{

  protected 	int appid=-1;
  protected	  int lineid=1;						                      // for simplicity assume first controller is free

  private	Vector buffers=null;			                        // list of input buffers

  public SimpleSpeechReceive()throws IOException{
    jcapi.checkInstalled();
    appid=jcapi.register(1,7,512);
    buffers=new Vector();
  }

  private void put(MsgOut msg)throws IOException{
    jcapi.putMessage(appid,msg.getBytes());
  }

  public void close()throws IOException{
    jcapi.release(appid);                                   // release resources
  }

  protected void receive(DataB3Ind msg)throws IOException{  // capi thread store byte arrays
    byte[] data=msg.getPayload(null);
    put(new DataB3Resp(msg.appid,msg.lineid,msg.getHandle()));
    buffers.add(data);                                      // simply 'save' byte arrays in a Vector object
//    System.err.println("receive data "+data.length);
    System.err.println(".");
  }

  public byte[] read()throws IOException{                   // application thread get stored byte arrays from vector
    if(buffers.size()!=0){ 
      byte[] data=(byte[])buffers.firstElement();
      buffers.removeElementAt(0);
      return data;
    }
    return null; 
  }

  synchronized protected void indicateCall(ConnectInd msg)throws IOException{
    System.err.println("local number "+msg.getLocalNo());
    System.err.println("remote number "+msg.getRemoteNo());

    UserUser.Out uu=new UserUser.Out("hi");
    AdditionalInfo.Out addInfo=new AdditionalInfo.Out(uu);

    put(new ConnectResp(
      appid,
      msg.lineid,
      CapiConstants.ACCEPT,							// accept
//      CapiConstants.IGNORE,							// ignore
      new SpeechProtocol(),				      // get raw data 
      msg.getLocalNo(),									// use whatever number we are listening to
      "",																// subb address
      StructOut.empty,
//      StructOut.empty
      addInfo
    ));
  }

  protected void handleMessage(final MsgIn msg)throws IOException{
    if(msg instanceof ConnectInd){								          // someone calls us
      indicateCall((ConnectInd)msg);
    }else if(msg instanceof ConnectActiveInd){
      put(new ConnectActiveResp(msg.appid,msg.lineid));
    }else if(msg instanceof ConnectB3Ind){
      put(new ConnectB3Resp(appid,msg.lineid));	            // accept call
    }else if(msg instanceof ConnectB3ActiveInd){	          // Connected
      put(new ConnectB3ActiveResp(msg.appid,msg.lineid));
      lineid=msg.lineid;				                            // save for other  thread
      buffers.clear();
    }else if(msg instanceof DataB3Ind){			
      receive(((DataB3Ind)msg));
    }else if(msg instanceof DisconnectB3Ind){		            // disconnect logical line
      put(new DisconnectB3Resp(msg.appid,msg.lineid));
    }else if(msg instanceof DisconnectInd){		              // disconnected physical line
      put(new DisconnectResp(msg.appid,msg.lineid));	       
      throw new IOException("Normal Disconnect");           // end main loop :)
    }else{
      System.err.println(msg);
    }
  }

  byte[] buf=null;

  protected void waitForMessage()throws IOException{
    jcapi.waitForMessage(appid);							    // block till message arrives
    buf=jcapi.getMessage(appid,buf);
    MsgIn msg=MsgIn.create(buf);                  // turn byte array into java MsgIn object
    handleMessage(msg);
  }

  public void save(OutputStream out)throws IOException{
    byte[] buffer;
    while((buffer=read())!=null){
      out.write(buffer);
    }    
    out.close();
  }

  public void receive(OutputStream out)throws IOException{
    put(new ListenReq(appid,lineid));
    try{
      while(true){
        waitForMessage();
      }
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
    try{
      close();
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
    save(out);
  }

  public static void main(String[] argv){
    try{
      String file="uk/co/mmscomputing/device/capi/samples/capture.raw";
      if(argv.length>0){file=argv[0];}
      SimpleSpeechReceive sr=new SimpleSpeechReceive();
      sr.receive(new FileOutputStream(file));
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
  }


}