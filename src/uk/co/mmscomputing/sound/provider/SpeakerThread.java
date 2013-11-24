package uk.co.mmscomputing.sound.provider;

import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;

class SpeakerThread extends Thread{

          boolean                            isrunning=false;
  private javax.sound.sampled.SourceDataLine speaker=null;
  private DataLine[]                         sources=null;
  private ArrayBlockingQueue                 queue=new ArrayBlockingQueue(10);

  public SpeakerThread(DataLine[] sources)throws LineUnavailableException{
    this.sources=sources;

    AudioFormat format=sources[0].getFormat();
    int bufsize=sources[0].getBufferSize();

    speaker=getSpeaker(format,bufsize);                 // Get an output line from the audio system.

    for(int i=0;i<sources.length;i++){
      sources[i].setSpeakerThread(this);
    }
  }  

  private javax.sound.sampled.SourceDataLine getSpeaker(AudioFormat format,int bufsize)
    throws LineUnavailableException
  {
    javax.sound.sampled.DataLine.Info  info;
    javax.sound.sampled.Mixer.Info[]   infos;
    javax.sound.sampled.Mixer          mixer;
    javax.sound.sampled.SourceDataLine speaker;

    infos = AudioSystem.getMixerInfo();
    info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class,format);
    for(int i=0;i<infos.length;i++){
      mixer=AudioSystem.getMixer(infos[i]);
      if(!(mixer instanceof uk.co.mmscomputing.sound.provider.Mixer) ){
//        System.err.println("SPEAKER : "+mixer.getMixerInfo().getName());
        try{
          speaker=(javax.sound.sampled.SourceDataLine)mixer.getLine(info);    
          speaker.open(format,bufsize);
          return speaker;
        }catch(IllegalArgumentException iae){
        }catch(LineUnavailableException lue){
        }
      }
    }
    throw new LineUnavailableException(getClass().getName()+".run() : \n\tNo microphone available.");
  }

  void flush(DataLine source){
    for(int i=0;i<sources.length;i++){
      if(sources[i].isActive()){
        if(sources[i]==source){           // if the first active line is the source
          queue.offer(new byte[0]);       //   put empty buffer into queue to flush speaker
        }
        return;
      }
    }
  }

  void put(DataLine source,byte[] buf){
    for(int i=0;i<sources.length;i++){
      if(sources[i].isActive()){
        if(sources[i]==source){           // if the first active line is the source
          try{queue.put(buf);             //   put buffer into queue
          }catch(InterruptedException ie){}
        }
        return;
      }
    }
  }

  public void run(){
    byte[] buf;
    try{
      isrunning=true;
      speaker.flush();
      speaker.start();
      while(isrunning){
        buf=(byte[])queue.take();         // wait for sound data
        if(buf.length==0){                // from flush
          speaker.flush();
        }else{
          speaker.write(buf,0,buf.length);// write to speaker
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      speaker.stop();speaker.flush();speaker.close();
    }
  }
}