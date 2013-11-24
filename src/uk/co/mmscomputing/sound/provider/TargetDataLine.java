package uk.co.mmscomputing.sound.provider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import uk.co.mmscomputing.concurrent.ArrayBlockingQueue;

class TargetDataLine extends DataLine implements javax.sound.sampled.TargetDataLine{

  private ArrayBlockingQueue queue=new ArrayBlockingQueue(1);

  TargetDataLine(){
    super();
  }

//  TargetDataLine Interface

  public void open()throws LineUnavailableException{
    open(Mixer.format);
  }

  public void open(AudioFormat format)throws LineUnavailableException{
    open(format,getBufferSize());
  }

  public void open(AudioFormat format, int bufferSize)throws LineUnavailableException{
    super.open();
    if(!format.matches(Mixer.format)){
      throw new IllegalArgumentException(
        getClass().getName()+".open(AudioFormat format, int bufferSize) :\n\t."
        +"format does not match Mixer format"
      );
    }
    if(bufferSize<getBufferSize()){          // make sure there is space for whole queue buffer
      throw new IllegalArgumentException(
        getClass().getName()+".open(AudioFormat format, int bufferSize) :\n\t."
        +"parameter bufferSize too small ["+bufferSize+" < "+getBufferSize()+"]"
      );
    }
  }

  public void drain(){
//    if(!isOpen()){ return; }
    flush();
  }

  public void flush(){		                   //	discard buffers
    queue.clear();
  }

  public void start(){
    flush();
    super.start();                           // activate
  }

  public void close(){
    super.close();                           // close
    flush();
    queue.offer(new byte[0]);                // release blocked read
  }

  public int read(byte[] buf, int off, int len){
    if(!isOpen()){ 
      throw new IllegalStateException(getClass().getName()+".read(byte[] buf, int off, int len): Line is not open.");
    }
    if(buf==null){ 
      throw new NullPointerException(getClass().getName()+".read(byte[] buf, int off, int len): b is null");
    }
    if((off<0)||(len<0)||(buf.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".read(byte[] buf, int off, int len): index off or len out of bounds.");
    }
    if(len<getBufferSize()){                 // make sure there is space for whole queue buffer
      throw new IllegalArgumentException(
        getClass().getName()+".read(byte[] buf, int off, int len) :\n\t"
        +"parameter len smaller then audio output buffer size ["+len+" < "+getBufferSize()+"]"
      );
    }
    try{ 
      byte[] b=(byte[])queue.take();         // 
      len=b.length;
      System.arraycopy(b,0,buf,off,len);
      return len;
    }catch(InterruptedException ie){
      return -1;
    }
  }

//  TargetDataLine

  // Called from Mixer microphone thread.
  // buf contains default sound input data (microphone)

  boolean offer(byte[] buf){
    if(queue.isFull()){return false;}                // avoid synchronized methods as far as possible
    return queue.offer(buf);                         // offer only, don't want microphone thread to block!
  }
}




