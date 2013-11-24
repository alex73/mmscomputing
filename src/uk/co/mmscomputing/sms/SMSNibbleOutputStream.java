package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSNibbleOutputStream extends FilterOutputStream{

  // [1] p.40 9.1.2.3 Semi-octet representation

  // 1010="*", 1011="#", 1100="a", 1101="b", 1110="c"

  private int     nibble;
  private boolean haveNibble;

  public SMSNibbleOutputStream(OutputStream out){
    super(out);
    nibble=0;
    haveNibble=false;
  }

  private int convert(int b){
    if((0x30<=b)&&(b<=0x39)){
      return b-0x30;
    }
    switch(b){
    case '*': return 10;
    case '#': return 11;
    case 'a': return 12;
    case 'b': return 13;
    case 'c': return 14;
    }
    return 15;                           // 15: 1111 filler nibble at end
  }

  public void write(int b)throws IOException{
    if(haveNibble){
      b = convert(b)<<4;
      out.write(b|nibble);
      haveNibble=false;
    }else{
      nibble  = convert(b);
      haveNibble=true;
    }
  }

  public void write(byte[] b)throws IOException{
    write(b,0,b.length);
  }

  public void write(byte[] b,int off,int len)throws IOException{
    for(int i=0;i<len;i++){write(b[off+i]);}
  }

  public void flush()throws IOException{
    if(haveNibble){
      write('f');
      haveNibble=false;
    }
//  super.flush();                           // DON'T flush SMSOutputStream here !
  }

  public static void main(String[] argv){
    try{
      String s="0044123456789";

      ByteArrayOutputStream baos=new ByteArrayOutputStream();
      SMSNibbleOutputStream out=new SMSNibbleOutputStream(baos);
      out.write(s.getBytes());
      out.flush();

      ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
      SMSNibbleInputStream in=new SMSNibbleInputStream(bais);

      byte[] bytes=new byte[20];
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
