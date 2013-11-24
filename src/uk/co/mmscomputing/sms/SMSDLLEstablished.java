package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDLLEstablished extends SMSDataUnit{

  public SMSDLLEstablished(){}

  public void readFrom(InputStream in)throws IOException{
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_EST);
    out.flush();
  }

  public String toString(){
    String s=super.toString();
    s+="Data Link Layer connection established.\n";
    return s;
  }
}