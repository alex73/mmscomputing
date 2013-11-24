package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;
import uk.co.mmscomputing.device.capi.parameter.*;
import uk.co.mmscomputing.device.capi.facility.*;

public class AnswerPhone implements MetadataListener, Runnable{

  static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

  private CapiMetadata md;

  private File voicedir,faxdir;
  private CapiServerApplication server = null;

  public AnswerPhone()throws IOException{
    File userhome=new File(System.getProperty("user.home"),"mmsc");

    voicedir = new File(userhome,"phone");
    voicedir.mkdirs();

    faxdir = new File(userhome,"fax"+File.separator+"received");
    faxdir.mkdirs();

//    CapiEnumerator e=new CapiEnumerator();
//    System.out.println("CapiEnumerator:\n\n"+e.toString()+"\n\n");

    md=new CapiMetadata();

    md.useMaxLogicalConnections(CapiEnumerator.getNoOfBChannels());
    md.useALaw();                               // set some defaults
    md.use64kBit();
    md.acceptAllCalls();
    md.useController(2);

    md.useLocalNo("0123456789");
    md.useFaxHeader("my mmsc fax machine :) ");
    md.putInt("pickupTime",3000);
    md.putInt("recordTime",20000);
    md.putInt("timeout",30000);
    md.putString("startmsg","uk/co/mmscomputing/sounds/startmsg.wav");
    md.putString("endmsg","uk/co/mmscomputing/sounds/endmsg.wav");

    md.addListener(this);

    System.out.println("create server...");

    server=new CapiServerApplication(md);
    new Thread(server).start();
  }

  private void send(final CapiCallApplication server,final String phoneno,final String srcfile){
    new Thread(){
      public void run(){
        try{
          int timeout=md.getInt("timeout");
          CapiChannel channel=server.connect(phoneno,timeout);
          AudioInputStream in = AudioSystem.getAudioInputStream(new File(srcfile));
          channel.writeToOutput(in);
          in.close();
          channel.close();                            // initiate disconnect
          System.out.println("CLOSED SEND WRITE THREAD");
        }catch(Exception ioe){
          ioe.printStackTrace();
        }
      }
    }.start();
  }

  private void answer(final CapiChannel channel,final File destfile){
    final Thread[] t=new Thread[2];

    new Thread(){                                       // send start msg
      public void run(){
        Thread.currentThread().setName(getClass().getName()+".startmsg.0x"+Integer.toHexString(channel.getLineID()));

        AudioInputStream in=null;
        try{
          File startmsg=new JarFile(md.getString("startmsg"));
          in=AudioSystem.getAudioInputStream(startmsg);
          channel.writeToOutput(in);
        }catch(Exception e){
          e.printStackTrace();
          try{
            channel.close();
          }catch(Exception ee){
            ee.printStackTrace();
          }
        }finally{
          try{
            if(in!=null){in.close();}
            System.err.println("STOPPED STARTMSG 0x"+Integer.toHexString(channel.getLineID()));
          }catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }.start();

    t[1]=new CapiFaxDetector(channel);t[1].start();     // if we receive 3 fax cng-tones then assume analogue group 3 fax

    t[0]=new Thread(){
      public void run(){                              

        Thread.currentThread().setName(getClass().getName()+".wait_endmsg.0x"+Integer.toHexString(channel.getLineID()));

        AudioInputStream in=null;
        try{
          sleep(md.getInt("recordTime"));             // record 'recordTime' secs (includes time of startmsg)

          System.err.println("STOPPED SLEEPING 0x"+Integer.toHexString(channel.getLineID()));

          File endmsg=new JarFile(md.getString("endmsg"));
          in=AudioSystem.getAudioInputStream(endmsg);
          channel.writeToOutput(in);
        }catch(InterruptedException ie){
          System.err.println("INTERRUPTED CHANNEL 0x"+Integer.toHexString(channel.getLineID()));
        }catch(Exception e){
          e.printStackTrace();
        }finally{
          try{
            if(in!=null){in.close();}
            System.err.println("ANSWERPHONE CLOSE CHANNEL 0x"+Integer.toHexString(channel.getLineID()));
            channel.close();                            // initiate disconnect
            System.err.println("ANSWERPHONE CLOSED CHANNEL 0x"+Integer.toHexString(channel.getLineID()));
          }catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    };t[0].start();

    new Thread(){                                       // read capi input as pcm audio data
      public void run(){                                // and save as pcm wav file
        AudioInputStream in=null;

        Thread.currentThread().setName(getClass().getName()+".record.0x"+Integer.toHexString(channel.getLineID()));

        try{
          in=channel.getAudioInputStream();
          AudioSystem.write(in,AudioFileFormat.Type.WAVE,destfile);
          in.close();
          System.err.println("STOPPED RECORDING 0x"+Integer.toHexString(channel.getLineID()));
          t[0].interrupt();t[1].interrupt();
        }catch(Exception e){
          e.printStackTrace();
        }finally{
          try{
            if(in!=null){in.close();}
            channel.close();                            // initiate disconnect
          }catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }.start();
  }

  private void receivefax(final CapiChannel channel,final File destfile){
    new Thread(){                                     // read capi input as byte data
      public void run(){                              // and save as binary (sff) file
        try{
          channel.writeInputTo(new FileOutputStream(destfile));
        }catch(Exception ioe){
          ioe.printStackTrace();
        }
      }
    }.start();
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof DTMFInd){
      System.out.println(getClass().getName()+".update : \n\tRECEIVED DTMF DIGITS : "+((DTMFInd)data).getDigits());      
    }else if(data instanceof NCPI){
      System.out.println(getClass().getName()+".update : \n\tRECEIVED NCPI\n"+data);      
    }else if(data instanceof CapiMetadata.Indication){
      handleIndication((CapiMetadata.Indication)data);
    }else if(data instanceof CapiException){
      System.out.println(data);
    }else if(data instanceof Exception){
      System.out.println(data);
//      System.out.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
//    }else if(data instanceof String){
    }else{
      System.err.println(data);
    }
  }

  public void handleIndication(CapiMetadata.Indication mdi){

    System.out.println("local  no = "+mdi.localno);
    System.out.println("remote no = "+mdi.remoteno);

    mdi.setAccept(md.getInt("pickupTime"));
  }

  public void run(){

    Thread.currentThread().setName(getClass().getName());

    System.out.println("Started running...");

    try{
      while(true){
        System.out.println("wait for call...");
        CapiChannel channel=server.accept();
        if(channel==null){break;}
        BProtocol protocol=channel.getProtocol();

        String rno=channel.getRemoteNo();
        rno=rno.replaceAll("\\*\\*","");    // PBX: internal dialling
        String nostr=channel.getLocalNo()+"_"+rno;

        if(protocol instanceof SpeechProtocol){
          answer(channel,new File(voicedir,sdf.format(new Date())+nostr+".wav"));        
        }else{
          receivefax(channel,new File(faxdir,sdf.format(new Date())+nostr+".sff"));        
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    System.out.println("Stopped running...");
  }

  public void close(){
    server.close();
  }

  static private void RedirectSystemOut(String logfilename){
    try{
      PrintStream ps=new PrintStream(new FileOutputStream(logfilename));
//      System.setErr(ps);
      System.setOut(ps);
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

  static private void showThreads()throws InterruptedException{
    Thread thread=Thread.currentThread();
    ThreadGroup threadgroup=thread.getThreadGroup();

    int len=threadgroup.activeCount();
    Thread[] threads=new Thread[len];
    len=threadgroup.enumerate(threads);
    System.err.println("\n\n\nThread group count = "+len);
    for(int j=0;j<len;j++){
      System.err.println("Thread name = "+threads[j].getName());
      System.err.println("Thread class name = "+threads[j].getClass().getName());

      if(!threads[j].getName().startsWith("main")){
//        threads[j].interrupt();
      }
    }
  }

  public static void main(String[] args){
//    RedirectSystemOut("/var/log/mmsc-answerphone.log");
    try{
      final AnswerPhone app=new AnswerPhone();
      new Thread(app).start();

      BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
      String inputline;
      while((inputline=is.readLine())!=null){
        System.out.println("input: "+inputline);
        if(inputline.equals("quit")){                 // type 'quit' on command line to quit:)
          showThreads();
          app.close();
          break;
        }
      }
      is.close();
      System.out.println("Stopped Answerphone.");

      showThreads();

    }catch(Exception e){
      System.out.println(e);
    }    
  }
}

