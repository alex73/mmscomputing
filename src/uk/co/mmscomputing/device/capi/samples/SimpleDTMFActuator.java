package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.exception.*;

import uk.co.mmscomputing.device.capi.facility.*;

public class SimpleDTMFActuator implements MetadataListener, Runnable{

  CapiServerApplication appl;
  String  dtmfcode;
  String  cmd="";

  //  phoneno without leading 0s

  public SimpleDTMFActuator(String dtmfcode, String cmd)throws IOException{
    this.dtmfcode=dtmfcode;                               // dtmf tone sequence to listen for
    this.cmd=cmd;                                         // command/script to run
  }

  public void run(){
    try{
      CapiMetadata md=new CapiMetadata();

      md.useMaxLogicalConnections(1);                     // need only one connection
      md.useController(1);                                // use first controller
      md.useALaw();                                       // set some defaults
      md.use64kBit();                                
      md.acceptAllCalls();

      md.addListener(this);                               // want to listen; 

      appl=new CapiServerApplication(md);
      appl.start();                                       // start capi thread

      while(true){
        CapiChannel channel=appl.accept();
        try{
          channel.getInputStream().close();                 // waste input data
///*
          HandsetReq req=new HandsetReq(channel.getApplID(),channel.getLineID());
          channel.put(req);                                 // not necessary for dtmf; just testing
//*/
/*
          SupServiceReq ssr;

          ssr=new SupServiceReq.GetSupportedServicesReq(channel.getApplID(),channel.getLineID());
          channel.put(ssr);                                 // not necessary for dtmf; just testing
*/
          System.err.println("START DTMF wait for signal = "+dtmfcode);
          channel.startDTMF();                              // tell channel to listen for DTMFs
          String dtmf=channel.getDTMFDigits(dtmfcode.length(),20000);// wait for 'length' DTMF tones within 20secs
          System.err.println("DTMF '"+dtmf+"' ["+dtmfcode+"]");
          if(dtmfcode.equals(dtmf)){
/*

Do what you need to do here.

i.e. run some script

*/
            runCommand(cmd);
            channel.sendDTMF("0099009900");                 // send some confirmation
            new Semaphore(0,true).tryAcquire(2000,TimeUnit.MILLISECONDS);// wait a sec for confirmation to be sent
          }else{
            System.err.println("Do not process command "+cmd+"\n\tReceived "+dtmf);
            channel.sendDTMF("0000000000");
            new Semaphore(0,true).tryAcquire(2000,TimeUnit.MILLISECONDS);// wait a sec for confirmation to be sent
          }
          channel.stopDTMF();
        }catch(InterruptedException ie){
          System.err.println(ie);
        }finally{
          System.err.println("STOP DTMF");
          channel.close();                                  // initiate disconnect
        }
      }
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof DisconnectInd){
      System.err.println("\nDisconnect\n\n");
    }else if(data instanceof CapiMetadata.Indication){
      handleIndication((CapiMetadata.Indication)data);
    }else if(data instanceof CapiException){
      System.err.println(data);
    }else if(data instanceof Exception){
//      System.err.println(data);
      ((Exception)data).printStackTrace();
    }else if(data instanceof DataB3Ind){
    }else if(data instanceof DataB3Conf){
    }else{
      System.err.println(data);
    }
  }

  public void handleIndication(CapiMetadata.Indication mdi){

    System.out.println("local  no = "+mdi.localno);
    System.out.println("remote no = "+mdi.remoteno);

    mdi.setAccept(0);                                    // pick up
  }

  public void runCommand(String cmd){
    System.err.println("Start Cmd : "+cmd);
    try{
      Process p=Runtime.getRuntime().exec(cmd);

      final BufferedReader stderr=new BufferedReader(new InputStreamReader(p.getErrorStream()));
      BufferedReader stdout=new BufferedReader(new InputStreamReader(p.getInputStream()));

      new Thread(){
        public void run(){
          try{
            String s;
            while((s=stderr.readLine())!=null){
              System.err.println("stderr: "+s);
            }
          }catch(IOException e){
            System.err.println(e);
          }
        }
      }.start();
      String s;
      while((s=stdout.readLine())!=null){
        System.err.println("stdout: "+s);
      }    
    }catch(IOException e){
      System.err.println(e);
    }
    System.err.println("Finished cmd : "+cmd);
  }

  public void checkInput()throws IOException{
    BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
    String inputline;
    while((inputline=is.readLine())!=null){
//      System.out.println("input: "+inputline);
      if(inputline.equals("quit")){                      // type 'quit' on command line to quit:)
        appl.close();
        break;
      }
    }
    is.close();
  }

  public static void main(String[] argv){
    String dtmfcode="1234";
    if(argv.length>0){dtmfcode=argv[0];}
    String command="java -version";
    if(argv.length>1){command=argv[1];}
    System.err.println("\nStart SimpleDTMFActuator\n\tdtmfcode : "+dtmfcode+"\n\tcommand : "+command);
    System.err.println("\n\tType 'quit' on command line to quit :)\n");
    try{
      SimpleDTMFActuator vb=new SimpleDTMFActuator(dtmfcode,command);
      Thread t=new Thread(vb);
      t.start();
      vb.checkInput();                                   // wait for 'quit' command
      t.interrupt();
      System.err.println("End SimpleDTMFActuator.");
    }catch(Exception e){
      System.err.println(e);
    }    
  }
}

