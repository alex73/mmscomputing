package uk.co.mmscomputing.sms;

import java.io.*;

public class SMS7BitOutputStream extends FilterOutputStream{

  private int bitbuf;
  private int bitCount;

  public SMS7BitOutputStream(OutputStream out){
    super(out);
    bitbuf=0;
    bitCount=0;
  }

  public void write(int b)throws IOException{
    bitbuf |= (b&0x007F) << bitCount;
    bitCount+=7;
    
    while(bitCount>=8){
      b = bitbuf & 0x00FF;
      bitCount -=8;
      bitbuf >>= 8;

      out.write(b);
    }
  }

  public void write(byte[] b)throws IOException{
    write(b,0,b.length);
  }

  public void write(byte[] b,int off,int len)throws IOException{
    for(int i=0;i<len;i++){write(b[off+i]);}
  }

  public void flush()throws IOException{
    if(bitCount>0){
      write(-1);
      bitCount=0;
    }
//  super.flush();                           // DON'T flush SMS..OutputStream here !
  }

  public static void main(String[] argv){
    try{
      String s="Hello Michael";

      ByteArrayOutputStream baos=new ByteArrayOutputStream();
      SMS7BitOutputStream out=new SMS7BitOutputStream(baos);
      out.write(s.getBytes());
      out.flush();

      ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
      SMS7BitInputStream in=new SMS7BitInputStream(bais);

      byte[] bytes=new byte[256];
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


