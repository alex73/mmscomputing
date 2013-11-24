package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSTransportProtocolParameter{

  public SMSTransportProtocolParameter(){}

  protected int read(InputStream in)throws IOException{
    int b=in.read();
    if(b==-1){
      throw new IOException(getClass().getName()+".read():\n\tUnexpected end of stream.");
    }
    return b&0x00FF;
  }

  public void readFrom(InputStream in)throws IOException{
    throw new IOException(getClass().getName()+".readFrom():\n\tNot implemented yet.");
  }

  public void writeTo(OutputStream out)throws IOException{
    throw new IOException(getClass().getName()+".writeTo():\n\tNot implemented yet.");
  }

  public String toString(){
    return getClass().getName()+":\n";
  }
}