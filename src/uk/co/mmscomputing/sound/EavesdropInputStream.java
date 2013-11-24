package uk.co.mmscomputing.sound;

import java.io.*;
import javax.sound.sampled.*;

public class EavesdropInputStream extends FilterInputStream{
  private SourceDataLine aout     = null;
  private boolean        doListen = true;

  public EavesdropInputStream(InputStream in,AudioFormat format,int bufSize)throws LineUnavailableException{
    super(in);aout=SoundMixerEnumerator.getOutputLine(format,bufSize);aout.start();
  }

  public EavesdropInputStream(InputStream in,AudioFormat format)throws LineUnavailableException{
    this(in,format,512);
  }

  public EavesdropInputStream(AudioInputStream in,int bufSize)throws LineUnavailableException{
    this(in,in.getFormat(),bufSize);
  }

  public EavesdropInputStream(AudioInputStream in)throws LineUnavailableException{
    this(in,512);
  }

  public void setListen(boolean listen){doListen=listen;}

  public int read()throws IOException{
    throw new IOException(getClass().getName()+".read() :\n\tDo not support simple 'int read()'.");
  }

  public int read(byte[] b, int off, int len)throws IOException{
    len=in.read(b,off,len);
    if(len==-1) {return -1;}
    if(doListen){aout.write(b,off,len);}
    return len;
  }

  public void flush(){aout.flush();}                       // discard pending sound output data  

  public void close()throws IOException{
    in.close();aout.stop();aout.flush();aout.close();
  }
}

