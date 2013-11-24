package uk.co.mmscomputing.sound.provider;

import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;

class MicrophoneThread extends Thread{

          boolean                              isrunning=false;
  private javax.sound.sampled.TargetDataLine   microphone=null;
  private DataLine[]                           targets=null;

  public MicrophoneThread(DataLine[] targets)throws LineUnavailableException{
    this.targets=targets;

    AudioFormat format=targets[0].getFormat();
    int bufsize=targets[0].getBufferSize();
    microphone=getMicrophone(format,bufsize);
  }  

  private javax.sound.sampled.TargetDataLine getMicrophone(AudioFormat format,int bufsize)
    throws LineUnavailableException
  {
    javax.sound.sampled.DataLine.Info  info;
    javax.sound.sampled.Mixer.Info[]   infos;
    javax.sound.sampled.Mixer          mixer;
    javax.sound.sampled.TargetDataLine microphone;

    infos=AudioSystem.getMixerInfo();
    info=new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class,format);
    for(int i=0;i<infos.length;i++){
      mixer=AudioSystem.getMixer(infos[i]);
      if(!(mixer instanceof uk.co.mmscomputing.sound.provider.Mixer) ){
//        System.err.println("MICROPHONE : "+mixer.getMixerInfo().getName());
        try{
          microphone=(javax.sound.sampled.TargetDataLine)mixer.getLine(info);    
          microphone.open(format,bufsize);
          return microphone;
        }catch(IllegalArgumentException iae){
        }catch(LineUnavailableException lue){
        }
      }
    }
    throw new LineUnavailableException(getClass().getName()+".run() : \n\tNo microphone available.");
  }

  public void run(){
    byte[] buf;int i,len;
    try{
      buf=new byte[microphone.getBufferSize()];
      isrunning=true;
      microphone.start();
      while(isrunning){
        while(microphone.available()>buf.length){
          microphone.read(buf,0,buf.length);
        }
        len=microphone.read(buf,0,buf.length);            // read mic data
        for(i=0;i<targets.length;i++){
          ((TargetDataLine)targets[i]).offer(buf);        // write (non-blocking) mic data to lines.
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      microphone.stop();microphone.flush();microphone.close();
    }
  }
}


