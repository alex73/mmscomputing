package uk.co.mmscomputing.device.capi.sound;

import java.io.*;

public class LawOutputStream extends FilterOutputStream{

  static private byte[]   inverse=null;

  public LawOutputStream(OutputStream out)throws IOException{
    super(out);
  }

  public void write(int b)throws IOException{
    out.write(inverse[b&0x000000FF]);
  }

  public void write(byte[] b)throws IOException{
    int len=b.length;
    for(int i=0;i<len;i++){
      b[i]=inverse[b[i]&0x000000FF];
    }
    out.write(b);
  }

  public void write(byte[] b, int off, int len)throws IOException{
    for(int i=0;i<len;i++){
      b[off+i]=inverse[b[off+i]&0x000000FF];
    }
    out.write(b,off,len);
  }

  static{
    inverse=new byte[256];
    int s=0;
    for(int t=0;t<256;t++){
      inverse[t]=(byte)s;
      int u=256;
      do{
        u>>=1;
        s^=u; 
      }while((s^u)>s);
    }
  }
}