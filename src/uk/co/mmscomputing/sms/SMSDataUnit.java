package uk.co.mmscomputing.sms;

import java.io.*;
import java.util.*;

public class SMSDataUnit implements SMSConstants{

  protected Dictionary parameters;

  public SMSDataUnit(){
    parameters=new Hashtable();
  }

  protected int readOctet(boolean mandatory,InputStream in)throws IOException{
    int b=in.read();
    if(b==-1){
      if(mandatory){
        throw new IOException(getClass().getName()+".read():\n\tUnexpected end of stream.");
      }
      return 0x0000;
    }
    return b&0x00FF;
  }

  protected int readOctet(InputStream in)throws IOException{
    return readOctet(true,in);
  }

  protected int readInt(boolean mandatory,InputStream in)throws IOException{
    return readOctet(mandatory,in);
  }

  protected int readInt(InputStream in)throws IOException{
    return readInt(true,in);
  }

  protected int read(InputStream in)throws IOException{
    return readOctet(true,in);
  }

  protected void put(String name,Object value){
    parameters.put(name,value);
  }

  public Object get(String name){
    return parameters.get(name);
  }

  public boolean getBoolean(String name){
    Boolean b=(Boolean)parameters.get(name);
    return b.booleanValue();
  }

  public int getInt(String name){
    Integer i=(Integer)parameters.get(name);
    return i.intValue();
  }

  public byte getOctet(String name){
    Integer i=(Integer)parameters.get(name);
    return (byte)i.intValue();
  }

  public byte[] getUserData(){return new byte[0];}

  public void readFrom(InputStream in)throws IOException{
    throw new IOException(getClass().getName()+".readFrom():\n\tNot implemented yet.");
  }

  public void writeTo(OutputStream out)throws IOException{
    throw new IOException(getClass().getName()+".writeTo():\n\tNot implemented yet.");
  }

  public String toString(){
    String s = getClass().getName()+":\n";
    Enumeration e=parameters.keys();
    while(e.hasMoreElements()){
      String name=(String)e.nextElement();
      s+=name+" = "+parameters.get(name)+"\n";      
    }
    return s;
  }
}