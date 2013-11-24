package uk.co.mmscomputing.sound;

import java.io.*;

public class CompressInputStream extends FilterInputStream{

  /*
    Convert mono PCM byte stream into A-Law u-Law byte stream

    static AudioFormat alawformat= new AudioFormat(AudioFormat.Encoding.ALAW,8000,8,1,1,8000,false);
    static AudioFormat ulawformat= new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);

    PCM 8000.0 Hz, 16 bit, mono, SIGNED, little-endian
    static AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);

  */

  static private Compressor alawcompressor=new ALawCompressor();
  static private Compressor ulawcompressor=new uLawCompressor();

  private Compressor compressor=null;

  public CompressInputStream(InputStream in, boolean useALaw)throws IOException{
    super(in);
    compressor=(useALaw)?alawcompressor:ulawcompressor; 
  }

  public int read()throws IOException{
    throw new IOException(getClass().getName()+".read() :\n\tDo not support simple read().");
  }

  public int read(byte[] b)throws IOException{
    return read(b,0,b.length);
  }

  public int read(byte[] b, int off, int len)throws IOException{
    int     i,sample;
    byte[]  inb;

    inb=new byte[len<<1];          // get 16bit PCM data
    len=in.read(inb);
    if(len==-1){return -1;};

    i=0;
    while(i<len){
      sample   = (inb[i++]&0x00FF);
      sample  |= (inb[i++]<<8);
      b[off++]=(byte)compressor.compress((short)sample);
    }
    return len>>1;
  }
}

abstract class Compressor{
  protected abstract int compress(short sample);    
}

/*
	Mathematical Tools in Signal Processing with C++ and Java Simulations
		by	Willi-Hans Steeb
			International School for Scientific Computing
*/

class ALawCompressor extends Compressor{

  static final int cClip = 32635;

  static final int[] ALawCompressTable ={
    1,1,2,2,3,3,3,3,
    4,4,4,4,4,4,4,4,
    5,5,5,5,5,5,5,5,
    5,5,5,5,5,5,5,5,
    6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7
  };
  
  protected int compress(short sample){
    int sign;
    int exponent;
    int mantissa;
    int compressedByte;

    sign = ((~sample) >> 8) & 0x80;
    if(sign==0){ sample *= -1;}
    if(sample > cClip){ sample = cClip; }
    if(sample >= 256){
      exponent = ALawCompressTable[(sample >> 8) & 0x007F];
      mantissa = (sample >> (exponent + 3) ) & 0x0F;
      compressedByte = 0x007F & ((exponent << 4) | mantissa);
    }else{
      compressedByte = 0x007F & (sample >> 4);
    }
    compressedByte ^= (sign ^ 0x55);
    return compressedByte;
  }
}

class uLawCompressor extends Compressor{

  static final int cClip = 32635;
  static final int cBias = 0x84;

  int[] uLawCompressTable ={
    0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
    4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
    5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
    5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7
  };

  protected int compress(short sample){
    int sign;
    int exponent;
    int mantissa;
    int compressedByte;

    sign = (sample >> 8) & 0x80;
    if(sign!=0){ sample *= -1;}
    if(sample > cClip){ sample = cClip; }
    sample += cBias;

    exponent = uLawCompressTable[(sample >> 7) & 0x00FF];
    mantissa = (sample >> (exponent + 3)) & 0x0F;
    compressedByte = ~(sign | (exponent << 4) | mantissa);
    return compressedByte&0x000000FF;
  }
}