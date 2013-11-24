package uk.co.mmscomputing.sms;

import java.io.*;
import uk.co.mmscomputing.device.capi.*;

public class SMSSCCapiReceiver implements SMSDataUnitListener,Runnable{

  // Receiver for a test service centre.

  private CapiChannel  capichannel = null;
  private SMSSCChannel smschannel  = null;

  public SMSSCCapiReceiver(CapiChannel capichannel){
    this.capichannel=capichannel;
  }

  public void send(SMSDataUnit msg)throws IOException{
    smschannel.send(msg);
  }

  public void received(SMSDataUnit msg)throws IOException{
    if(msg instanceof SMSSubmit){
      System.out.println(msg);    

      SMSAddress da   = (SMSAddress)msg.get("TP-DA"); 
      byte[]     data = msg.getUserData();
                   
    }else if(msg instanceof SMSStatusReport){                       
      System.out.println(msg);                       
    }
  }

  public void run(){
    try{
      smschannel=new SMSSCChannel(
        capichannel.getPCMOutputStream(),
        capichannel.getPCMInputStream()
      );
      send(new SMSDLLEstablished());                            // mobile station (MS) called us; SC acknowledges

      InputStream in=smschannel.getInputStream();

      int len;byte[] bytes=new byte[256];
      while((len=in.read(bytes))!=-1){
        System.out.println(new String(bytes,0,len));
      }            
    }catch(Exception e){
      System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
    }finally{
      try{
        if(smschannel!=null){smschannel.close();}
        if(capichannel!=null){capichannel.close();}
      }catch(Exception e){
        System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
      }
    }
  }
}