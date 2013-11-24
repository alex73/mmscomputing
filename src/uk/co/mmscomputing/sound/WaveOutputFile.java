package uk.co.mmscomputing.sound;

import java.io.*;
import javax.sound.sampled.*;

public class WaveOutputFile extends RandomAccessFile{

  byte[] h=new byte[58];

  public WaveOutputFile(File file,AudioFormat format)throws FileNotFoundException,IOException{
    super(file,"rw");
    write(getHeader(format));
  }

  public WaveOutputFile(String file,AudioFormat format)throws FileNotFoundException,IOException{
    super(file,"rw");
    write(getHeader(format));
  }

  private byte[] getHeader(AudioFormat format){
    /*
      riff chunk = [format chunk, fact chunk, data chunk]
      We don't know the length of the data chunk yet.

      Need fact chunk for compressed data.
    */

    // start riff chunk

    int    i=0;
    h[i++]='R'; h[i++]='I'; h[i++]='F'; h[i++]='F';   // 0 : 'RIFF'
    h[i++]= 50; h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ;   // 4 : length of riff chunk [8..(data size+58)]
    h[i++]='W'; h[i++]='A'; h[i++]='V'; h[i++]='E';   // 8 : 'WAVE'

    // start format chunk

    h[i++]='f'; h[i++]='m'; h[i++]='t'; h[i++]=' ';   // 12: 'fmt '
    h[i++]= 18; h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ;   // 16 : length of format chunk = 0x12 = 18 [20..38]

    AudioFormat.Encoding encoding=format.getEncoding();
    if(encoding.equals(AudioFormat.Encoding.PCM_SIGNED)){
      h[i++]= 1 ; h[i++]= 0 ;                         // 20 : PCM = 1
    }else if(encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED)){
      throw new IllegalArgumentException(getClass().getName()
        +".getHeader(AudioFormat format)\n\tDo not support PCM unsigned data"
      );
    }else if(encoding.equals(AudioFormat.Encoding.ALAW)){
      h[i++]= 6 ; h[i++]= 0 ;                         // 20 : A-Law = 6
    }else if(encoding.equals(AudioFormat.Encoding.ULAW)){
      h[i++]= 7 ; h[i++]= 0 ;                         // 20 : u-Law = 7
    }else{
      throw new IllegalArgumentException(getClass().getName()
        +".getHeader(AudioFormat format)\n\tDo not support encoding ["+encoding+"]"
      );
    }
    int channels=format.getChannels();
    if(channels==1){
      h[i++]= 1 ; h[i++]= 0 ;                         // 22 : mono = 1
    }else if(channels==2){
      h[i++]= 2 ; h[i++]= 0 ;                         // 22 : stereo = 2
    }else{
      throw new IllegalArgumentException(getClass().getName()
        +".getHeader(AudioFormat format)\n\tDo not support "+channels+" channels"
      );
    }
    int sr = (int)format.getFrameRate();
    h[i++]=(byte)( sr      & 0x000000FF);             // 24 : sample rate
    h[i++]=(byte)((sr>>8)  & 0x000000FF);
    h[i++]=(byte)((sr>>16) & 0x000000FF);
    h[i++]=(byte)((sr>>24) & 0x000000FF);

    int ss = format.getFrameSize();
    int bs = sr * ss;
    h[i++]=(byte)( bs      & 0x000000FF);             // 28 : bytes per second
    h[i++]=(byte)((bs>>8)  & 0x000000FF);
    h[i++]=(byte)((bs>>16) & 0x000000FF);
    h[i++]=(byte)((bs>>24) & 0x000000FF);

    h[i++]=(byte)( ss      & 0x000000FF);             // 32 : bytes per sample
    h[i++]=(byte)((ss>>8)  & 0x000000FF);                   

    int ssib = format.getSampleSizeInBits();
    h[i++]=(byte)( ssib    & 0x000000FF);             // 34 : bits per sample
    h[i++]=(byte)((ssib>>8)& 0x000000FF);                   

    h[i++]= 0 ; h[i++]= 0 ;                           // 36 : number of bytes of additional compressor-specific information

    // end   format chunk                             // 38
    // start fact chunk                               // 38

    h[i++]='f'; h[i++]='a'; h[i++]='c'; h[i++]='t';   // 38: 'fact'
    h[i++]= 4 ; h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ;   // 42 : length of fact chunk = 0x04 = 4 [46..50]
    h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ;   // 46 : data size

    // end   fact chunk                               // 50
    // start data chunk                               // 50

    h[i++]='d'; h[i++]='a'; h[i++]='t'; h[i++]='a';   // 50: 'data'
    h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ; h[i++]= 0 ;   // 54 : length of data chunk [58..(data size+58)]

    // data size bytes

    // end   data chunk                               // data size + 58
    // end   riff chunk                               // data size + 58
    return h;
  }

  public void close()throws IOException{
    long size = length();
    if(size>=0x7FFFFFFFl){
      throw new IOException(getClass().getName()+".close()\n\tData size ["+size+"] is too big for WAV file.");
    }
    int i=4;
    int len=(int)size-8;                     
    h[i++]=(byte)( len      & 0x000000FF);            // 4 : length of riff chunk [8..(data size+58)]
    h[i++]=(byte)((len>>8)  & 0x000000FF);
    h[i++]=(byte)((len>>16) & 0x000000FF);
    h[i++]=(byte)((len>>24) & 0x000000FF);

    i=46;
    len=(int)size-58;                                 // data size
    h[i++]=(byte)( len      & 0x000000FF);            // 46 : 'fact' data size
    h[i++]=(byte)((len>>8)  & 0x000000FF);
    h[i++]=(byte)((len>>16) & 0x000000FF);
    h[i++]=(byte)((len>>24) & 0x000000FF);
    
    i=54;
    h[i++]=(byte)( len      & 0x000000FF);            // 54 : length of data chunk [58..(data size+58)]
    h[i++]=(byte)((len>>8)  & 0x000000FF);
    h[i++]=(byte)((len>>16) & 0x000000FF);
    h[i++]=(byte)((len>>24) & 0x000000FF);

    seek(0);
    write(h);                                         // write header at beginning of file

    super.close();
  }
}

