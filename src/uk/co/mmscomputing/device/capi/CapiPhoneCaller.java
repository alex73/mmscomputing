package uk.co.mmscomputing.device.capi;

import java.io.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.sound.*;
import uk.co.mmscomputing.device.phone.*;

public class CapiPhoneCaller extends PhoneCaller{

  private boolean active=true;

  public CapiPhoneCaller(){
  }

  public void call()throws IOException{

    final SourceDataLine speaker;
    final TargetDataLine microphone;

    try{
      microphone=SoundMixerEnumerator.getInputLine(pcmformat,CapiConstants.DefaultPCMBlockSize);
      try{
        speaker=SoundMixerEnumerator.getOutputLine(pcmformat,CapiConstants.DefaultPCMBlockSize);
      }catch(LineUnavailableException e){
        microphone.close();
        throw new IOException(getClass().getName()+".call:\n\t"+e.getMessage());
      }
    }catch(LineUnavailableException e){
      throw new IOException(getClass().getName()+".call:\n\t"+e.getMessage());
    }

    md.setState(phoneRinging);fireListenerUpdate(md.STATE);  // ask application to set remote no

    String rno    = md.getRemoteNo();
    int    time   = md.getTimeOut();

//    if(md.progressmonitor){in=new ProgressMonitorInputStream(null,"Fax: sending ... to "+rno+"\n"+md.getFile(),in);}

    System.out.println("calling rno="+rno);
    System.err.println("calling rno="+rno);

    try{
      CapiSystem  capisystem = CapiSystem.getSystem();  // use capi system
      CapiChannel channel    =                          // try to connect
          capisystem.getCaller().connect(rno,time*1000);

      md.setState(phoneConnected);fireListenerUpdate(md.STATE);

      md.setInfo("Connected ...");
      fireListenerUpdate(md.INFO);
      try{
        active=true;

        int    count;
        byte[] buffer = new byte[CapiConstants.DefaultPCMBlockSize];

        final OutputStream cout = channel.getPCMOutputStream();
        final InputStream  cin  = channel.getPCMInputStream();

        final OutputStream out = md.getOutputStream();
        final InputStream  in  = md.getInputStream();

        new Thread(){
          public void run(){
            int    count;
            byte[] buffer = new byte[CapiConstants.DefaultPCMBlockSize];
            try{
              speaker.flush();
              speaker.start();
              speaker.flush();
              while(active&&(count=cin.read(buffer))!=-1){       // from telecom network
                speaker.write(buffer,0,count);
              }
              active=false;
              cin.close();
              speaker.flush();
              speaker.stop();
              speaker.flush();
              speaker.close();
            }catch(Exception e){
              System.out.println("9\b"+getClass().getName()+".call:\n\t"+e);
              e.printStackTrace();
            }
          }
        }.start();

        microphone.flush();
        microphone.start();
        microphone.flush();
        while(active&&((count=microphone.read(buffer,0,CapiConstants.DefaultPCMBlockSize))!=-1)){
          cout.write(buffer,0,count);                    // to telecom network 
        }
        active=false;
        cout.close();

        microphone.flush();
        microphone.stop();
        microphone.flush();
        microphone.close();
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".call:\n\t"+e);
        e.printStackTrace();
      }finally{
        channel.close();                                // initiate disconnect
      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".call:\n\tCall could not been established.\n\t"+e);
    }
    md.setState(phoneDisconnected);fireListenerUpdate(md.STATE);
  }

  public boolean isAPIInstalled(){return true;/*capi.isInstalled();*/}

  static public PhoneCaller getDevice(){
    try{
      return new CapiPhoneCaller();
    }catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }
}
