package uk.co.mmscomputing.sms;

import java.io.*;
import uk.co.mmscomputing.device.capi.*;

public class SMSCapiSender implements SMSReceiver,Runnable{

  static private int         mr = 1;

  private SMSLandLineChannel smschannel=null;
  private String             centre,destination,text;

  public SMSCapiSender(String centr,String dest, String txt){
    destination=dest;
    centre=centr;
    text=txt;

    System.out.println("3\bSEND MESSAGE TO: "+destination+" via "+centre+" :\n"+text);
  }

  public void send(SMSDataUnit msg)throws IOException{
    smschannel.send(msg);
  }

  public void received(SMSDataUnit msg)throws IOException{   // SMSReceiver called from SMSLandLineInputStream
    if(msg instanceof SMSDLLEstablished){                    // once connection is established
      SMSSubmit submit=new SMSSubmit(mr++,destination,text); 
      smschannel.send(submit);                               // send text message
    }else if(msg instanceof SMSSubmitAckReport){
      smschannel.send(new SMSDLLReleased());
    }
  }

  public void send()throws IOException{
    CapiCallApplication capi        = CapiSystem.getSystem().getCaller();
    CapiChannel         capichannel = null;

    try{
      capichannel=capi.connect(centre,10000);                // send connect request and wait for connection (max 10 sec.)
      smschannel=new SMSLandLineChannel(capichannel.getPCMOutputStream(),capichannel.getPCMInputStream(),this);

      InputStream in=smschannel.getInputStream();
      int len;byte[] bytes=new byte[256];
      while((len=in.read(bytes))!=-1){                       // if everything is OK we won't get any bytes here,
        System.out.println(new String(bytes,0,len));         // but we need to start the 'reading' processes.
      }            
    }catch(IOException ioe){
      ioe.printStackTrace();
      System.out.println("9\b"+getClass().getName()+".send:\n\t"+ioe);
    }finally{
      if(smschannel!=null){smschannel.close();}
      if(capichannel!=null){capichannel.close();}
    }
  }

  public void run(){
    try{
      send();
    }catch(Exception e){
      System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
    }
  }
}