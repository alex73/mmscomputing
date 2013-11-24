package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.plc.*;

public class SpeechSend implements MetadataListener{

  CapiCallApplication appl=null;

  public SpeechSend(String remoteno, String filename)throws IOException,UnsupportedAudioFileException{

    InputStream in=new FileInputStream(filename);

    CapiMetadata md=new CapiMetadata();

    md.useMaxLogicalConnections(1);                     // need only one connection
    md.useController(1);                                // use first controller
    md.useALaw();                                       // set some defaults
    md.use64kBit();                                

    md.addListener(this);                               // want to listen; 

    appl=new CapiCallApplication(md);
    appl.start();                                       // start capi thread

    try{
      System.err.println("Try connecting to "+remoteno+". Will wait for 100 sec.");

      CapiChannel channel=appl.connect(remoteno,100000);// send connect request and wait for connection (max 100 sec.)
      channel.getInputStream().close();                 // waste input data
      System.err.println("Connected to "+remoteno);

/*
        String  dtmfcode="0123";

        channel.startDTMF();
        String dtmf=channel.getDTMFDigits(dtmfcode.length(),60000);// wait for 'length' DTMF tones within 60secs
        System.err.println("DTMF "+dtmf+" ["+dtmfcode+"]");
        if(dtmfcode.equals(dtmf)){
          System.out.println("\n\n\nSuccess "+dtmf+"\n\n\n");
        }else{
          System.out.println("\n\n\nOps "+dtmf+"\n\n\n");
        }
*/
      try{
        System.err.println("Try sending data to "+remoteno);

        if(filename.endsWith(".raw")){                  // if raw sound data
          channel.writeToOutput(in);                    // write from in ==> channel
        }else{                                          // if not raw assume audio file
          channel.writeToOutput(                        // write from in ==> channel
              AudioSystem.getAudioInputStream(in)
          );
        }
      }catch(Exception e){
        System.err.println(e.getMessage());
      }
      channel.close();
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
    in.close();

    appl.close();
  }

  public void update(Object data, Metadata metadata){   // called from capi thread
    if(data instanceof DisconnectInd){                  // disconnected -> close application
      System.err.println("End SpeechSend.");
    }else if(data instanceof Exception){
      System.err.println(data);
//      System.err.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
    }else{
      System.err.println(data);
    }
  }

  public static void main(String[] argv){
    System.err.println("Start SpeechSend.");
    try{
      String no="**20";
      if(argv.length>0){no=argv[0];}
      String file="uk/co/mmscomputing/sounds/startmsg.wav";
//      String file="uk/co/mmscomputing/device/capi/samples/capture.raw";
      if(argv.length>1){file=argv[1];}
      new SpeechSend(no,file);
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }
}