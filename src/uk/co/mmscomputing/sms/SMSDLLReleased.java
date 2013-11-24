package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDLLReleased extends SMSDataUnit{

  public SMSDLLReleased(){}

  public void readFrom(InputStream in)throws IOException{
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_REL);
    out.flush();
  }

  public String toString(){
    String s=super.toString();
    s+="Data Link Layer connection released.\n";
    return s;
  }
}