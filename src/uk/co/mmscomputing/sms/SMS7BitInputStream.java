package uk.co.mmscomputing.sms;

import java.io.*;

public class SMS7BitInputStream extends FilterInputStream{

  private int bitbuf;
  private int bitCount;

  public SMS7BitInputStream(InputStream in){
    super(in);
    bitbuf=0;
    bitCount=0;
  }

  public int read()throws IOException{
    if(bitCount<7){
      int b=super.read();
      if(b==-1){return -1;}
      bitbuf |= (b&0x00FF) << bitCount;
      bitCount+=8;
    }
    int res=bitbuf&0x007F;
    bitCount-=7;
    bitbuf>>=7;
    return res;
  }

  public int read(byte[] buf, int off, int len)throws IOException{
    if(buf==null){
      throw new NullPointerException(getClass().getName()+".read(byte[] buf, int off, int len): buf is null");
    }
    if((off<0)||(len<0)||(buf.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".read(byte[] buf, int off, int len): index off ["+off+"] or len ["+len+"] out of bounds ["+buf.length+"].");
    }
    int  b;
    int  count=0;
    while(count<len){
      b=read();
      if(b==-1){return (count==0)?-1:count;}
      buf[off++]=(byte)b;
      count++;
    }
    return count;
  }


  public static void main(String[] argv){
    try{
      byte[]             bytes={(byte)0xC8,(byte)0x32,(byte)0x9B,(byte)0xFD,
                                (byte)0x66,(byte)0x81,(byte)0xEE,(byte)0x6F,
                                (byte)0x39,(byte)0x9B,(byte)0x0C
                               };
      InputStream        src=new ByteArrayInputStream(bytes);
      SMS7BitInputStream in=new SMS7BitInputStream(src);

      bytes=new byte[256];
      int len=in.read(bytes);
      System.err.println("len = "+len);
      System.err.println(new String(bytes,0,len));
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}

// [1] ETSI TS 123 040 (2004-09)
// [2] 3GPP TS 23.038 V7.0.0 (2006-03)
