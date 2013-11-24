package uk.co.mmscomputing.sound.provider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

class SourceDataLine extends DataLine implements javax.sound.sampled.SourceDataLine{

  SourceDataLine(){
    super();
  }

// SourceDataLine Interface

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
  }

  public void start(){
    super.start();
  }

  public void stop(){
    speaker.flush(this);                           // prevent crackling noise, I experienced otherwise
    super.stop();
  }
/*
  public void close(){
    super.close();                                 // close
  }
*/
  public int write(byte[] buf, int off, int len){
    if(!isactive||(buf.length==0)){return 0;}
    if(!isopen){ throw new IllegalStateException();}
    if((len<0)||(len%getFormat().getFrameSize()!=0)){
      throw new IllegalArgumentException(getClass().getName()+".write(byte[] buf, int off, int len).");
    }
    if((off<0)||(len<0)||(buf.length<(off+len))){
      throw new ArrayIndexOutOfBoundsException(getClass().getName()+".write(byte[] buf, int off, int len): index off or len out of bounds.");
    }
    byte[] b=new byte[len];                        // put a copy of buf in speaker thread ouput queue
    System.arraycopy(buf,off,b,0,len);
    speaker.put(this,b);
    return len;
  }
}




