package uk.co.mmscomputing.util.log;

import java.io.*;
import java.util.*;
import java.text.*;

public class LogStream extends PrintStream{
  /*
    println
           input:  byte 0..n: text interpreted as level 0
           or      byte 0:    debug level dl
                   byte 1:    '\b'
                   byte 2..n: text interpreted as level dl

           output: byte 0:    debug level dl
                   byte 1:    '\b'
                   byte 2..d: date'\t'
                   byte d..n: text interpreted as level dl
                   byte n+1:  '\b'
  */

  SimpleDateFormat sdff=new SimpleDateFormat("yyyy-MM-dd H:m:s:S\t");
  
  public LogStream(OutputStream out){
    super(out);
  }

  public void println(String s){
    String str="";
    if(s.length()>=2){
      char s0=s.charAt(0);char s1=s.charAt(1);
      if((('0'<=s0)&&(s0<='9'))&&(s1=='\b')){
        str+=s0;str+='\b';
        str+=sdff.format(new Date());
        str+=s.substring(2);
        super.println(str);
        super.write('\b');
        return;
      }
    }
    str+='0';str+='\b';
    str+=sdff.format(new Date());
    str+=s;
    super.println(str);
    super.write('\b');
  }

  public void redirectSystemOut(){
    System.setOut(this);
  }

  public void redirectSystemErr(){
    System.setErr(this);
  }

  static public void redirectSystemOutToFile(String logfilename){
    try{
      System.setOut(new LogStream(new FileOutputStream(logfilename)));
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

  static public void redirectSystemErrToFile(String logfilename){
    try{
      System.setErr(new LogStream(new FileOutputStream(logfilename)));
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

}