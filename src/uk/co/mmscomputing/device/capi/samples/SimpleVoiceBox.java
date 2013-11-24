package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class SimpleVoiceBox implements MetadataListener, Runnable{

  CapiServerApplication appl;
  String                phoneno;
  File                  dir;

  class WAVFilter implements FilenameFilter{
    public boolean accept(File dir, String s){
      s=dir+"/"+s;
      File f=new File(s);
      return f.isFile() && s.endsWith(".wav");
    }
  }

  /*
    phoneno without leading 0s
  */

  public SimpleVoiceBox(String phoneno, File dir)throws IOException{
    this.phoneno=phoneno;                                 // phone number to accept
    this.dir=dir;                                         // dir with .wav files
  }

  public void run(){
    try{
     jcapi.checkInstalled();

      System.out.println("running...");

      CapiMetadata md=new CapiMetadata();

      md.useMaxLogicalConnections(CapiEnumerator.getNoOfBChannels());
      md.useALaw();                                       // set some defaults
      md.use64kBit();
      md.acceptAllCalls();
      md.useController(1);

      md.addListener(this);                               // want to listen; 

      appl=new CapiServerApplication(md);
      appl.start();                                       // start capi thread

      while(true){
        System.out.println("Wait for call.");
        CapiChannel channel=appl.accept();      
        channel.getInputStream().close();                 // waste input data
        try{
          String files[]=dir.list(new WAVFilter());       // send all .wav files in directory
          Arrays.sort(files);
          for(int i=0; i<files.length; i++){
            File file=new File(dir,files[i]);
            System.err.println("Try sending file : "+file.getAbsolutePath());
            try{
              channel.writeToOutput(AudioSystem.getAudioInputStream(file));
            }catch(UnsupportedAudioFileException uafe){
              System.err.println(uafe.getMessage());
            }
          }
        }catch(Exception e){
          System.err.println(e.getMessage());
        }
        channel.close();                                // initiate disconnect
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

    if(phoneno.equals(mdi.remoteno)){                    // if caller id is trusted number
      mdi.setAccept(5000);                              // wait 10 sec. and then pick up
      System.err.println("Accept call from "+mdi.remoteno);
    }else{
      mdi.setIgnore();                                   // otherwise ignore call
      System.err.println("Ignore call from "+mdi.remoteno);
    }
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
    String no="**20";
    if(argv.length>0){no=argv[0];}

    File userhome = new File(System.getProperty("user.home"),"mmsc");
    File voicedir = new File(userhome,"phone");
    voicedir.mkdirs();

    if(argv.length>1){voicedir=new File(argv[1]);}

    System.err.println("\nStart SimpleVoiceBox\n\taccept: "+no+"\n\tsend  : "+voicedir.getAbsolutePath());
    System.err.println("\n\tType 'quit' on command line to quit:)\n");
    try{
      SimpleVoiceBox vb=new SimpleVoiceBox(no,voicedir);
      new Thread(vb).start();
      vb.checkInput();
      System.err.println("End SimpleVoiceBox.");
    }catch(Exception e){
      System.err.println(e);
    }    
  }
}

