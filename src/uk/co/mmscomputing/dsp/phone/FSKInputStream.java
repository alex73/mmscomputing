package uk.co.mmscomputing.dsp.phone;

import java.io.*;

public class FSKInputStream extends FilterInputStream{

  static public final int QUIET = 0;
  static public final int MARK  = 1;                // 1300Hz
  static public final int SPACE = 2;                // 2100Hz


  static private final int        max   = 7;
  static private final double[][] retab = new double[max][max];
  static private final double[][] imtab = new double[max][max];

  protected   byte[]     pcm=new byte[2];           // can only read even number of bytes at a time from PCM AudioInputStream.
  protected double[]     au=new double[max];        // buffer audio

  private     byte[]     bytes = new byte[256];     // buffer data
  private     int        len,index;

  public FSKInputStream(InputStream in){
    super(in);
  }

  protected boolean readSample()throws IOException{
    if(in.read(pcm)!=2){return false;}              // read one pcm sample
    for(int i=au.length-1;i>0;i--){au[i]=au[i-1];}  // first in first out
    au[0]=(pcm[0]&0x00FF)|(pcm[1]<<8);
    return true;
  }

  private int getFrequency(){                       // [1] p.363
    double maxmag=0,rev,imv,mag;
    int    maxfrq=0,lastfrq=max>>1;

    for(int i=0;i<=lastfrq;i++){                    // get frequency with largest spectral magnitude
      rev=0;imv=0;
      for(int k=0;k<au.length;k++){
        rev+=retab[i][k]*au[k];
        imv-=imtab[i][k]*au[k];
      }
      rev = (Math.abs(rev)<0.00001)?0.0:rev;
      imv = (Math.abs(imv)<  0.001)?0.0:imv;
      mag = rev*rev+imv*imv;
      if(mag>maxmag){
        maxmag=mag;
        maxfrq=i;
      }
    }
    if(maxmag<1e6){ return QUIET;}                  // Only noise
    if(maxfrq==3){  return QUIET;}
    return maxfrq;                                  // QUIET(0Hz),MARK(1300Hz),SPACE(2100Hz)
  }

  // 1200 baud = 1200 bits/sec; 8000 Hz;

  public int readQuiet()throws IOException{
    int count=0;
    while(true){
      if(!readSample()){
        if(count==0){return -1;}                    // EOF
        break;
      }
      if(getFrequency()==QUIET){                    // FSK modulation not up and running yet
        count++;
      }else{                                        // change of symbol
        break;
      }
    }
    return count;
  }

  public int readBits(int symbol)throws IOException{
    int freq,count=0;

    while(true){
      if(!readSample()){
        if(count==0){return -1;}                    // EOF
        break;
      }
      freq=getFrequency();
      if(freq==symbol){
        count++;
      }else if(count==0){                           // change of symbol, but too early
        count++;                                
      }else if(freq==QUIET){                        // FSK modulation not up and running yet
        break;
      }else{                                        // change of symbol
        break;
      }
    }                                               
    count++;                                        // add one for change of symbol in last readBits invocation
    return (int)(((double)count)*0.15+0.5);         // 1200/8000 bits = 0.15 bits
  }

  public boolean readMarkSignal(int minlen)throws IOException{
    int b,i,bits;
    do{
      i=readQuiet();
      if(i==-1){return false;}
      bits=readBits(MARK);                          // minlen MARK bits
      if(bits==-1){return false;}
    }while(bits<minlen);
    return true;
  }

  protected int readBuffer()throws IOException{
    int len=0;
    while(len<bytes.length){
      int bits=readBits(SPACE);                     // System.err.println("SPACE "+bits);
      if(bits==-1){return (len==0)?-1:len;}
      if(bits== 0){throw new IOException(getClass().getName()+".readBuffer:\n\tCorrupt message.Not enough bits per byte. Missing START bit.");}
      bits--;                                       // minus start bit

      int b=0;
      int i=bits;                                   // first 'bits' bit are 0

      while(true){
        bits=readBits(MARK);                        // System.err.println("MARK  "+bits);
        if(bits==-1){throw new IOException(getClass().getName()+".readBuffer:\n\tCorrupt message.Not enough bits per byte. Missing MARK bits.");}
        if((i+bits)<8){
          b|=(~(-1<<bits))<<i;
        }else if((i+bits)==9){
          bits--;                                   // minus stop bit
          b|=(~(-1<<bits))<<i;
          break;
        }else{                                      // missing start bit, assume end of FSK coding
          bits=8-i;                                 // fill rest with 1s; After last STOP(1) bit follow 1..10 MARK(1) bits
          b|=(~(-1<<bits))<<i;                      // System.err.println("\t\t\tBYTE["+len+"] = 0x"+Integer.toHexString(b)+" "+Integer.toBinaryString(b));
          bytes[len++]=(byte)b;
          return len;
        }
        i+=bits;

        bits=readBits(SPACE);                       // System.err.println("SPACE "+bits);
        if(bits==-1){throw new IOException(getClass().getName()+".readBuffer:\n\tCorrupt message.Not enough bits per byte. Missing SPACE bits.");}
        i+=bits;
        if(i>8){throw new IOException(getClass().getName()+".readBuffer:\n\tCorrupt message.Too many bits per byte. Missing STOP bit.");}
      }                                             // System.err.println("\t\t\tBYTE["+len+"] = 0x"+Integer.toHexString(b)+" "+Integer.toBinaryString(b));
      bytes[len++]=(byte)b;                         
    }
    return len;
  }
    
  public int available()throws IOException{
    return len-index;
  }

  public int read()throws IOException{
    while(!(index<len)){
      len=readBuffer();
      if(len==-1){
        index=-1;
        return -1;
      }
      index=0;
    }
    return bytes[index++]&0x00FF;
  }

  public int read(byte[] buf, int off, int len)throws IOException{
    if(buf==null){
      throw new NullPointerException(getClass().getName()+".read(byte[] buf, int off, int len): buf is null");
    }
    if((off<0)||(len<0)||(buf.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".read(byte[] buf, int off, int len): index off ["+off+"] or len ["+len+"] out of bounds ["+buf.length+"].");
    }
    for(int i=0;i<len;i++){
      int b=read();
      if(b==-1){return (i==0)?-1:i;}
      buf[off++]=(byte)b;
    }
    return len;
  }

  static{
    for(int i=0;i<max;i++){
      for(int k=0;k<max;k++){
        double tmp=2.0*Math.PI*((double)i)*((double)k)/((double)max);
        retab[i][k]=(1.0/(double)max)*Math.cos(tmp);
        imtab[i][k]=(1.0/(double)max)*Math.sin(tmp);
      }
    }
  }

  public static void main(String[] argv){
    try{
      String file="uk/co/mmscomputing/dsp/phone/fsk.raw";

      FSKInputStream in=new FSKInputStream(new FileInputStream(file));

      int b,i=0;

      if(in.readMarkSignal(55)){
        while((b=in.read())!=-1){
          System.out.println(" "+i+" , "+Integer.toHexString(b)+" , "+Integer.toBinaryString(b)+" , "+((char)b));
          i++;
        }
      }
      in.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

// [1] Paul A. Lynn, Wolfgang Fuerst; Introductory Digital Signal Processing 2nd; Wiley ISBN 0-471-97631-8
// ETSI ES 201 912 v 1.2.1 (2004-06)
// ETS 300 659-1/2         (1997-02)

