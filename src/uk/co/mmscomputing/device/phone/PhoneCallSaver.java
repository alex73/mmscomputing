package uk.co.mmscomputing.device.phone;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.sound.WaveOutputFile;

public class PhoneCallSaver extends Thread implements PhoneConstants,PhoneCallHandler{

  protected Properties    properties;

  protected boolean       isrunning;
  private   Player        player=null;

  private   String        filename=null;

  private   InputStream   pin;

  public PhoneCallSaver(){}

  public String getFile(){return filename;}
  public void init(Properties properties){this.properties=properties;}

  public void run(String local,String remote,final InputStream pin,final OutputStream pout){

    // called by a PhoneAnswerer in own thread

    this.pin=pin;

    WaveOutputFile fout=null;
    try{
      isrunning=true;

      player=new Player(pout);                        // play start msg, wait a bit, play end msg
      player.start();

      filename = createFilePath(local,remote);      // save phone input as wav file
      fout     = new WaveOutputFile(new File(filename),pcmformat);

      write(pin,fout);

      player.stopPlaying();
      stopRunning();
 
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
      e.printStackTrace();
    }finally{
      try{
        pin.close();
        pout.close();
        if(fout!=null){
          fout.close();
        }
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\tCould not close an i/o stream.\n\t"+e);
      }
    }
  }

  public void stopRunning(){
    isrunning=false;
  }

  public void stopPlaying(){
    if(player!=null){
      player.stopPlaying();
    }
  }

  static private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS-");
  static private int msgno=0;

  private String createFilePath(String localno,String remoteno){
    File file,parent=new File(properties.getProperty(phoneFileDirID));
    String rno=remoteno.replaceAll("\\*\\*","");    // PBX: internal dialling
    String destfile=sdf.format(new Date())+(msgno++)+"_"+localno+"_"+rno+".wav";
    try{
       parent.mkdirs();
       file=new File(parent.getAbsolutePath(),destfile);
    }catch(Exception e){ 
       System.out.println("9\b"+getClass().getName()+".createFilePath:\n\tCould not create directory:\n\t"
           +parent.getAbsolutePath());
       file=new File(destfile);
    }
    return file.getAbsolutePath();
  }

  protected void write(InputStream pin,WaveOutputFile fout)throws IOException{
    int    count;byte[] buffer = new byte[DefaultPhonePCMBlockSize];
    while(isrunning){
      count=pin.read(buffer);
      if(count==-1){break;}
      fout.write(buffer,0,count);
    }
  }

  private class Player extends Thread{

    private boolean      isplaying;
    private Semaphore    recordBlocker;
    private OutputStream pout;
    
    public Player(OutputStream pout){
      this.pout=pout;
      isplaying=true;
      recordBlocker = new Semaphore(0,true);
    }

    public void stopPlaying(){  
      if(isplaying){
        isplaying=false;
        recordBlocker.release();
      }
    }

    public void run(){
      try{
        String startmsg=properties.getProperty(phoneStartMsgID);
        String endmsg=properties.getProperty(phoneEndMsgID);
        int    time;
        try{ time=Integer.parseInt(properties.getProperty(phoneTimeToRecordID));
        }catch(Exception e){ time=60;
        }
        if((startmsg!=null)&&!startmsg.equals("")){
          play(startmsg,pout);                                          // play start message
        }
        if(isplaying){
          for(int i=0;i<time;i++){                                      // block so that input thread can record x secs before we disconnect 
            if(!isrunning){break;}
            recordBlocker.tryAcquire(1000,TimeUnit.MILLISECONDS);
          }
          if(isplaying){
            if((endmsg!=null)&&!endmsg.equals("")){
              play(endmsg,pout);                                        // play end message
            }
          }
        }
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\t"+e);
      }finally{
        if(isplaying){
          isplaying=false;
          stopRunning();
        }
      }
    }

    private void play(String file,OutputStream pout){
      int              count;
      AudioInputStream in    = null;
      try{
        in = AudioSystem.getAudioInputStream(new JarFile(file));        // get sound file
        in=AudioSystem.getAudioInputStream(pcmformat,in);               // convert to pcm if necessary
        byte[] buffer = new byte[DefaultPhonePCMBlockSize];
        while(isrunning&&isplaying&&(count=in.read(buffer))!=-1){       // read sound data and,
          pout.write(buffer,0,count);                                   // write to phone output
        }
      }catch(IOException e){
        System.out.println("3\b"+getClass().getName()+".play\n\t"+e);
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".play\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          if(in!=null){in.close();}
        }catch(IOException e){
          System.out.println("3\b"+getClass().getName()+".play\n\t"+e);
        }
      }
    }
  }
}

