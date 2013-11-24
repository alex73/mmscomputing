package uk.co.mmscomputing.util.log;

import java.io.*;
import java.util.*;
import java.text.*;

public class LogBookStream extends OutputStream{

  /*
    write
           input:  byte 0:    debug level dl
                   byte 1:    '\b'
                   byte 2..d: date'\t'
                   byte d..n: text interpreted as level dl
                   byte n+1:  '\b'
  */

  private LogStream ls;
  private LogBook   lb;
  private boolean   eol,bs;
  private int       level;
  private ByteArrayOutputStream out;

  public LogBookStream(LogBook log){
    ls=new LogStream(this);
    lb=log;
    eol=true;bs=false;
    level=0;
    out=new ByteArrayOutputStream();
  }

  public void write(int b)throws IOException{
    try{
      if(eol){
        eol=false;
        bs=true;            // expect '\b'
        level=b-'0';
        return;
      }
      if(bs){               // ASSERT(b=='\b');
        bs=false;
        return;
      }
      if(b=='\b'){
        eol=true;bs=false;
        String s=new String(out.toByteArray());
        lb.write(level,s);
        out.reset();
        return;
      }
      out.write(b);
    }catch(Exception e){
    }catch(Error e){
    }
  }

  public void redirectSystemOut(){ls.redirectSystemOut();}
  public void redirectSystemErr(){ls.redirectSystemErr();}

}