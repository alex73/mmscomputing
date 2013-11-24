package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSNibbleInputStream extends FilterInputStream{

  // [1] p.40 9.1.2.3 Semi-octet representation

  // 1010="*", 1011="#", 1100="a", 1101="b", 1110="c"

  private int     nibble;
  private boolean haveNibble;

  public SMSNibbleInputStream(InputStream in){
    super(in);
    nibble=0;
    haveNibble=false;
  }

  private int convert(int nibble){
    nibble&=0x000F;
    if(nibble<=0x09){
      return nibble+0x30;
    }
    switch(nibble){
    case 10: return '*';
    case 11: return '#';
    case 12: return 'a';
    case 13: return 'b';
    case 14: return 'c';
    }
    return -1;                           // 15: 0000 1111 filler nibble at end
  }

  public int read()throws IOException{
    if(haveNibble){
      haveNibble=false;
      return nibble;
    }
    int b=super.read();
    if(b==-1){return -1;}

    nibble=convert(b>>4);
    haveNibble=true;

    return convert(b);
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
      byte[]               bytes={(byte)0x70,(byte)0x59,(byte)0x57,(byte)0x20,(byte)0x12,(byte)0xF6};
      InputStream          src  = new ByteArrayInputStream(bytes);
      SMSNibbleInputStream in   = new SMSNibbleInputStream(src);

/*
      int b=in.read();
      while(b!=-1){
        System.out.println("\n\t0x"+Integer.toHexString(b)+"\n\t"+Integer.toBinaryString(b)+"b\n\t"+((char)b)+"\n");
        b=in.read();
      }
*/
      bytes=new byte[20];
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
// [3] GSM 44.008
