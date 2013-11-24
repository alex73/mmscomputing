package uk.co.mmscomputing.sound.provider;

import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;

import uk.co.mmscomputing.concurrent.*;

class Clip extends DataLine implements javax.sound.sampled.Clip,Runnable{

  private Object[]               chunks    = null;
  private int                    curindex  = 0;
  private Semaphore              blocker   = new Semaphore(0,true);
  private int                    loopstart=0,loopend=0,loops=0;

  public Clip(){
    super();
  }

  public void open()throws LineUnavailableException{
    throw new IllegalArgumentException(
      getClass().getName()+".open() :\n\t."
      +"This method is not supported.[Hint:Need some data for a clip.]"
    );
  }

  public void open(AudioFormat format)throws LineUnavailableException{
    throw new IllegalArgumentException(
      getClass().getName()+".open(AudioFormat format) :\n\t."
      +"This method is not supported.[Hint:Need some data for a clip.]"
    );
  }

// following method not tested yet.

  public void open(AudioFormat format,byte[] b,int off,int len)throws LineUnavailableException{
    if(!Mixer.format.matches(format)){
      throw new IllegalArgumentException(getClass().getName()+".open(AudioFormat format,byte[] data,int offset,int bufferSize)\t\n"+
        "Format 'format' does not match Mixer.format [\n\t"+Mixer.format+"\n\t]"
      );
    }
    int bufSize;byte[] buf;Vector cv;

    cv=new Vector();
    bufSize=getBufferSize();

    int max=len/bufSize;
    for(int i=0;i<max;i++){
      buf=new byte[bufSize];
      System.arraycopy(b,off,buf,0,bufSize);
      cv.add(buf);
      off+=bufSize;
    }
    buf=new byte[bufSize];
    System.arraycopy(b,off,buf,0,len%bufSize);
    cv.add(buf);
    super.open();
    chunks=cv.toArray();
  }

  public void open(AudioInputStream in)throws LineUnavailableException,IOException{
    if(!Mixer.format.matches(in.getFormat())){
      throw new IllegalArgumentException(getClass().getName()+".open(AudioInputStream in)\t\n"+
        "Format of audio input stream does not match Mixer.format [\n\t"+Mixer.format+"\n\t]"
      );
    }

    int count;byte[] buf;Vector cv;
    int bufSize=getBufferSize();

    cv=new Vector();
    try{
      while(true){
        buf=new byte[bufSize];
        count=in.read(buf,0,buf.length);      
        if(count==-1){break;}
        cv.add(buf);
      }
      super.open();
    }catch(Exception e){
      cv.add(new byte[bufSize]);
      e.printStackTrace();
    }
    chunks=cv.toArray();
  }

  public long getMicrosecondLength(){
    double len=(getFrameLength()*1000000.0)/Mixer.format.getFrameRate();
    return (long)len;
  }

  public void setMicrosecondPosition(long microseconds){
    long framepos=(microseconds*getFrameLength())/getMicrosecondLength();
    setFramePosition((int)framepos);
  }

  public int getFramePosition(){
    return curindex*getBufferSize()/Mixer.format.getFrameSize();
  }

  public void setFramePosition(int frames){
    flush();
    curindex=(frames*Mixer.format.getFrameSize())/getBufferSize();
  }

  public int getFrameLength(){
    return chunks.length*getBufferSize()/Mixer.format.getFrameSize();
  }

  public void setLoopPoints(int start,int end){
    loopstart=start;loopend=end;
    if(loopend==-1){loopend=chunks.length;}
  }

  public void loop(int count){
    loops=count;
  }

  public void start(){
    if(!isrunning){ new Thread(this).start();}
    if(!isactive){
      super.start();                           // activate
      blocker.release();
    }
  }

  public void stop(){
    setLoopPoints(0,0);loop(0);
    super.stop();
    speaker.flush(this);
  }

  public void run(){
    isrunning = true;
    curindex  = 0;
    try{
      while(isrunning){
        if(!isactive){
          blocker.acquire();              
        }
        if(curindex==(loopend/getBufferSize())){
          if(loops==LOOP_CONTINUOUSLY){
            setFramePosition(loopstart);
          }else if(loops>0){
            loops--;
            setFramePosition(loopstart);
          }//else{}                                // run to end of clip
        }
        if(chunks.length<=curindex){
          stop();                                  // isactive=false
        }else{
          speaker.put(this,(byte[])chunks[curindex]);
          curindex++;
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

/*
javax.sound.sampled.LineUnavailableException: Failed to allocate clip data: Requested buffer too large.
*/


