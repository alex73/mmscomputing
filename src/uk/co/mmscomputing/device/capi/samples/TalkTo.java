package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import java.nio.channels.ClosedChannelException;

import uk.co.mmscomputing.concurrent.Semaphore;
import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.sound.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.sound.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.exception.*;

import uk.co.mmscomputing.device.capi.facility.*;

public class TalkTo implements MetadataListener, Runnable{

  static AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);
  static int         bufsize   = 512;

  CapiCallApplication   appl;
  Thread                thread;
  CapiChannel           ncc;
  String                phoneno;

  CapiPLC               plc;
  Semaphore             blocker=new Semaphore(0,true);

  public TalkTo(String phoneno)throws IOException{
    this.phoneno=phoneno;                               // phone number to accept

    CapiMetadata          md=new CapiMetadata();

    md.useMaxLogicalConnections(1);                     // need only one connection
    md.useController(1);                                // use first controller
    md.useALaw();                                       // set some defaults
    md.use64kBit();                                

    md.addListener(this);                               // want to listen; 

    appl=new CapiCallApplication(md);
    appl.start();                                       // start capi thread
  }

  public void run(){
    try{
      System.err.println("Try connecting to "+phoneno+". Will wait for 20 sec.");
      ncc=appl.connect(phoneno,20000);                  // send connect request and wait for connection (max 20 sec.)
      System.err.println("Connected to "+phoneno);

      plc=appl.getPLC(ncc.getLineID());
      plc.put(new SupServiceReq.ListenReq(plc.getApplID(),plc.getLineID()));

//      plc.put(new EchoCancellerReq.GetSupportedServicesReq(plc.getApplID(),plc.getLineID()));

      do{
        System.err.println("Start Speaker Output/Mic Input. [0x"+Integer.toHexString(ncc.getLineID())+"]");
/*
        plc.put(new EchoCancellerReq.EnableReq(plc.getApplID(),plc.getLineID(),7,0,0));

        new Thread(){
          public void run(){
            try{
              Thread.currentThread().sleep(10000);      // 10 sec canceller on
              plc.put(new EchoCancellerReq.DisableReq(plc.getApplID(),plc.getLineID()));
            }catch(Exception e){
              e.printStackTrace();
            }
            System.err.println("Disabled EchoCanceller.");
          }
        }.start();
*/
        new Thread(){
          public void run(){
            SourceDataLine out=null;
            try{
              out=SoundMixerEnumerator.getOutputLine(pcmformat,bufsize);  // get default sound output i.e. speaker
              ncc.writeInputTo(out);
            }catch(Exception e){
              e.printStackTrace();
            }finally{
              if(out!=null){out.close();}
            }
            System.err.println("Stop Speaker Output.");
          }
        }.start();

        TargetDataLine in=null;
        try{
          in=SoundMixerEnumerator.getInputLine(pcmformat,bufsize);        // get default sound input i.e. microphone
          ncc.writeToOutput(in);
        }catch(ClosedChannelException cce){
        }catch(Exception e){
          e.printStackTrace();
        }finally{
          if(in!=null){in.close();}
        }
        System.err.println("Stop Mic Input.");
        if(plc.isOnHold()){
          System.err.println("PLC on hold.");
          blocker.acquire();                                              // wait for "retrieve" input
          ncc=plc.retrieveChannel();
        }
      }while(!plc.isDisconnected());

      System.err.println("Close Channel.");

      plc.close();
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      appl.close();
    }
    System.err.println("Type 'quit' to end program.");             // to quit checkInput
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof DisconnectInd){      // disconnected -> close application
      blocker.release();
    }else if(data instanceof DisconnectReq){
    }else if(data instanceof Exception){
      System.err.println(data);
//      System.err.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
    }else{
      System.err.println(data.toString());
    }
  }

  public void checkInput()throws IOException{
    BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
    String inputline;
    while((inputline=is.readLine())!=null){
//      System.out.println("input: "+inputline);
      if(inputline.equals("hold")){                      //
        System.out.println("input: "+inputline);
        plc.hold();
      }else if(inputline.equals("retrieve")){            // 
        System.out.println("input: "+inputline);
        plc.retrieve();
        blocker.release();
      }else if(inputline.equals("quit")){                // type 'quit' on command line to quit:)
        blocker.release();
        appl.close();
        break;
      }
    }
    is.close();
  }

  public static void main(String[] argv){
    try{
      String no="**20";
      if(argv.length>0){no=argv[0];}
      System.err.println("\nStart TalkTo : "+no);
      System.err.println("\n\tType 'quit' on command line to quit:)\n");
      TalkTo phone=new TalkTo(no);
      new Thread(phone).start();
      phone.checkInput();
    }catch(ArrayIndexOutOfBoundsException aioobe){
      System.err.println("\n\nPlease supply a valid phone number as an argument.\n\n");
    }catch(Exception e){
      System.err.println(e);
    }    
    System.err.println("End TalkTo.");
    System.exit(0);
  }
}
