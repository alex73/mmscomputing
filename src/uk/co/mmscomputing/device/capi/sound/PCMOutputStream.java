package uk.co.mmscomputing.device.capi.sound;

import java.io.*;

public class PCMOutputStream extends FilterOutputStream{

  /*
    Convert mono PCM byte stream into ISDN 'raw' byte stream

    PCM 8000.0 Hz, 16 bit, mono, SIGNED, little-endian
    static AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);

  */

  static private byte[]   alawtable=new byte[65536];
  static private byte[]   ulawtable=new byte[65536];

  private byte[]   table=null;

  public PCMOutputStream(OutputStream out, boolean useALaw)throws IOException{
    super(out);
    table=(useALaw)?alawtable:ulawtable;
  }

  public void write(int b)throws IOException{
    throw new IOException(getClass().getName()+".write() :\n\tDo not support simple write(int b).");
  }

  public void write(byte[] b, int off, int len)throws IOException{
    int     sample;
    
    len>>=1;
    for(int i=0;i<len;i++){
      sample  = (b[off++]&0x00FF);
      sample |= (b[off++]&0x00FF)<<8;
      out.write(table[sample]);
    }
  }
/*
  public void write(byte[] b, int off, int len)throws IOException{
    byte[]  outb;
    int     max,sample;
    
    len>>=1;
    outb=new byte[len];
    for(int i=0;i<len;i++){
      sample  = (b[off++]&0x00FF);
      sample |= (b[off++]&0x00FF)<<8;
      outb[i]=table[sample];
    }
    out.write(outb);
  }
*/

  static{                                     // Build A-Law and mu-Law look up table.
    byte[] inverse=new byte[256];             // swap bits 0x00->0x00, 0x01->0x80, 0x02->0x40 ... 0xFF->0xFF
    int s=0;                           
    for(int t=0;t<256;t++){
      inverse[t]=(byte)s;
      int u=256;
      do{
        u>>=1;
        s^=u; 
      }while((s^u)>s);
    }
    
    ALawCompressor alawcompressor=new ALawCompressor();
    uLawCompressor ulawcompressor=new uLawCompressor();

    for(int sample=0;sample<65536;sample++){
      alawtable[sample]=inverse[alawcompressor.compress((short)sample)&0x00FF];
      ulawtable[sample]=inverse[ulawcompressor.compress((short)sample)&0x00FF];
    }
  }
}

/*
	Mathematical Tools in Signal Processing with C++ and Java Simulations
		by	Willi-Hans Steeb
			International School for Scientific Computing
*/

class ALawCompressor{

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

class uLawCompressor{

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