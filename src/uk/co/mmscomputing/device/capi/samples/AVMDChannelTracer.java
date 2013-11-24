package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.avm.*;
import uk.co.mmscomputing.device.capi.q931.*;

// http://www.avm.de/ftp/developer/index.html [2006-09-10]
// dtrace32 uses following manufacturer messages. (Used capiktrc to trace message flow)

public class AVMDChannelTracer implements Runnable{

  private     boolean running=false;

  protected  int appid=-1;
  protected   int lineid=1;

  public AVMDChannelTracer()throws IOException{
    jcapi.checkInstalled();
    appid=jcapi.register(1,2,128);
  }

  private void put(MsgOut msg)throws IOException{
    jcapi.putMessage(appid,msg.getBytes());
  }

  static public final int RR    = 0x01; // Receiver Ready
  static public final int RNR   = 0x05; // Receiver Not Ready
  static public final int REJ   = 0x09; // Reject
  static public final int SABME = 0x6f; // Set Asynchronous Balanced Mode Extended
  static public final int DM    = 0x0f; // Disconnect Mode
  static public final int UI    = 0x03; // Unnumbered Information
  static public final int DISC  = 0x43; // Disconnect
  static public final int UA    = 0x63; // Unnumbered Acknowledgement
  static public final int FRMR  = 0x87; // Frame Reject
  static public final int XID   = 0xaf; // Exchange Identification

  protected void handleMessage(MsgIn msg)throws IOException{
    if(msg instanceof AVMDTraceInd){
      AVMDTraceInd avmmsg=(AVMDTraceInd)msg;
      put(new AVMResp(appid,lineid,avmmsg.getClassId(),avmmsg.getFunctionId(),StructOut.empty));

      byte[] ddata = avmmsg.getBytes();
      Rider r=new Rider(ddata);

      System.out.println(r.toString());             // only avm specific data

      r.read();r.read();                            // not quite sure what these bytes are for

                                                    // D-channel layer 2: lapd/Q.921
/*

      8   7   6   5   4   3   2   1
  2      SAPI               |C/R| 0                Address Field
  3      TEI                    | 1
                                                   Control Field
  4      N(S)                   | 0                (I)nformation Format
  5      N(R)                   | P
                                                   OR
  4   0 | 0 | 0 | 0 | s | s | 0 | 1                (S)upervisory Format
  5      N(R)                   |P/F

                                                   OR
  4   M | M | M |P/F| M | M | 1 | 1                (U)nnumbered Format


     P = poll; requires an immediate response (primary station) 
   / F = final frame in response (secondary station)

     SAPI
     0  Frame is transporting signaling information
     1  Frame is carrying data for the Q.931 packet mode data
     16 Frame is carrying data for the X.25 packet-mode data
     62 Frame for test or recovery
     63 Frame is used for management purpose


     TEI
     0  - 63 Manually allocated TEI values
     64 -126 Automatically allocated TEI values
     127     Broadcasting
*/

      int sapi=r.read();
      int crbit=(sapi>>1)&0x01;                     // command/response bit: from user=0 OR from network=1 means command, otherwise response
      sapi>>=2;
      int tei=r.read();
      tei>>=1;

      int ctl=r.read();

      int pf,nr=0,ns=0;
      if((ctl&0x03)==0x03){                         // U-Format (4 bytes)
        pf  = ctl &  0x10; 
        ctl = ctl & ~0x10; 
      }else{                                        // I- & S-Format (5 bytes)
        nr=r.read();
        pf = nr&0x01; 
        nr>>=1;
      }

      System.out.println("lapd: cr="+crbit+" sapi="+sapi+" tei="+tei+" ctl=0x"+Integer.toHexString(ctl)+" pf="+pf);

      switch(sapi){
      case 0 :    System.out.println(" Frame is transporting signaling information "); break;
      case 1 :    System.out.println(" Frame is carrying data for the Q.931 packet mode data "); break;
      case 16:    System.out.println(" Frame is carrying data for the X.25 packet-mode data "); break;
      case 62:    System.out.println(" Frame for test or recovery "); break;
      case 63:    System.out.println(" Frame is used for management purpose "); break;
      }

      switch(ctl){
      case RR:    System.out.println(" RR  - Receiver Ready nr="+nr); break;   // S-Format
      case RNR:   System.out.println(" RNR - Receiver Not Ready nr="+nr); break;
      case REJ:   System.out.println(" REJ - Reject nr="+nr); break;

      case SABME: System.out.println(" SABME - Set Asynchronous Balanced Mode Extended "); break;   // U-Format
      case DM:    System.out.println(" DM    - Disconnect Mode "); break;
      case UI:    System.out.println(" UI    - Unnumbered Information "); break;
      case DISC:  System.out.println(" DISC  - Disconnect "); break;
      case UA:    System.out.println(" UA    - Unnumbered Acknowledgement "); break;
      case FRMR:  System.out.println(" FRMR  - Frame Reject "); break;
      case XID:   System.out.println(" XID   - Exchange Identification "); break;
      }

      if((ctl&0x01)==0){                                   // I-Format
        ns=ctl>>1;
        System.out.println(" Information Format nr="+nr+" ns="+ns);
      }

      try{
        Q931Message qmsg=Q931Message.create(r);            // D-channel layer 3: Q.931
        System.out.println(qmsg.toString());
      }catch(IndexOutOfBoundsException ioobe){
      }catch(Exception e){
        e.printStackTrace();
      }
    }else{
      System.out.println(msg);
    }
  }

  byte[] buf=null;

  public void run(){
    running=true;
    try{
      put(new AVMDTraceReq.Start(appid,lineid));        // start tracing D-Channel information

      while(running){
        jcapi.waitForMessage(appid);                    // block until message arrives
        buf=jcapi.getMessage(appid,buf);
        MsgIn msg=MsgIn.create(buf);                    // turn byte array into java MsgIn object
        handleMessage(msg);
      }

      put(new AVMDTraceReq.Stop(appid,lineid));         // stop tracing D-Channel information

      jcapi.waitForMessage(appid);

    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }finally{
      try{
        jcapi.release(appid);
      }catch(IOException ioe){
        System.err.println(ioe.getMessage());
      }
    }
  }

  public static void main(String[] argv){
    try{
      AVMDChannelTracer tracer=new AVMDChannelTracer();
      new Thread(tracer).start();
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }


}