package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.concurrent.Semaphore;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.*;

public class SimpleSpeechSend{

  private final int BUFSIZE = 512;

  protected 	 int appid=-1;

  private      InputStream  in=null;

  private	     Semaphore    blocker=new Semaphore(7,true);	  // buffer blocker
  private      DataB3Req[]  dataB3Reqs=new DataB3Req[8];      // Cache for DataB3Reqs.
  private      byte[][]     buffers=new byte[8][BUFSIZE];
  private 	   int          handle=0;							            // write message id

  public SimpleSpeechSend()throws IOException{
    jcapi.checkInstalled();
    appid=jcapi.register(1,7,BUFSIZE);
  }

  private void put(MsgOut msg)throws IOException{
    jcapi.putMessage(appid,msg.getBytes());
  }

  public void connect(String remote)throws IOException{// the number you want to call
    int CIP_SPEECH=1;									                 // simple speech
    int lineid=1;						                           // for simplicity assume first controller is free
    String local="";                                   // put your own number here

    put(new ConnectReq(
        appid,lineid,CIP_SPEECH,
        remote,local,"","",
        new SpeechProtocol(),
        StructOut.empty,
        StructOut.empty,
        StructOut.empty,
        StructOut.empty
    ));
  }

  public void close()throws IOException{
    jcapi.release(appid);		                           //	force CapiWaitForMessage to return
  }

  private void send(final int lineid){
    new Thread(){
      public void run(){
        System.err.println("START SENDING");
        try{
          int len;
          while((len=in.read(buffers[handle]))!=-1){
            System.err.println("write["+handle+"]="+len+" bytes");
            System.err.println("write data [blocked]");
            blocker.acquire();
            System.err.println("write data [released block]");

            DataB3Req req=new DataB3Req(appid,lineid,handle,0,buffers[handle],len); 
            dataB3Reqs[handle]=req;
            put(req);
            handle=(handle+1)%8;
          }
          put(new DisconnectB3Req(appid,lineid));	// active disconnect
        }catch(Exception e){
          System.err.println(e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();
  }

  protected void handleMessage(MsgIn msg)throws IOException{

//    System.err.println(msg);

    if(msg instanceof ConnectActiveInd){          // remote picked up
      put(new ConnectActiveResp(msg.appid,msg.lineid));
      put(new ConnectB3Req(appid,msg.lineid));	  // request logical line
    }else if(msg instanceof ConnectB3ActiveInd){	// Connected
      System.err.println("CONNECTED");
      put(new ConnectB3ActiveResp(msg.appid,msg.lineid));
      send(msg.lineid);														// start sender thread
    }else if(msg instanceof DataB3Conf){		
      int handle=((DataB3Conf)msg).getHandle();   // capi tells us that it is done with that buffer.
      dataB3Reqs[handle].release();               // release native pointer
      System.err.println("blocker [released block]");
      blocker.release();
    }else if(msg instanceof DisconnectB3Ind){		  // disconnect logical line
      put(new DisconnectB3Resp(msg.appid,msg.lineid));
      put(new DisconnectReq(appid,msg.lineid));   // active disconnect
    }else if(msg instanceof DisconnectInd){		    // disconnected physical line
      put(new DisconnectResp(msg.appid,msg.lineid));
      throw new IOException("Normal Disconnect"); // end main loop :)
    }else if(msg instanceof DataB3Ind){
      put(new DataB3Resp(msg.appid,msg.lineid,((DataB3Ind)msg).getHandle()));
    }else{
      System.err.println(msg);
    }
  }

  byte[] buf=null;                                // reusable buffer

  public void send(InputStream in){
    this.in=in;
    try{
      while(true){
        jcapi.waitForMessage(appid);							// block until message arrives
        buf=jcapi.getMessage(appid,buf);
        MsgIn msg=MsgIn.create(buf);              // create java message
        handleMessage(msg);
      }
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
      ioe.printStackTrace();
    }
    try{
      close();
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
  }

  public static void main(String[] argv){
    try{
      String no="**20";
      if(argv.length>0){no=argv[0];}
      String file="uk/co/mmscomputing/device/capi/samples/capture.raw";
      if(argv.length>1){file=argv[1];}
      System.err.println("Try to send raw isdn sound file "+file+" to "+no);
      SimpleSpeechSend s=new SimpleSpeechSend();
      s.connect(no);
      s.send(new FileInputStream(file));
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
  }


}