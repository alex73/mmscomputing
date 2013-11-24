package uk.co.mmscomputing.sms;

import java.io.*;
import java.util.*;

public class SMSTimeStamp extends SMSTransportProtocolParameter{

  // [1] p.58 9.2.3.11 TP-Service-Centre-Time-Stamp (TP-SCTS)

  private int    year,month,day,hour,min,sec,zone;

  public SMSTimeStamp(){
    Calendar date=Calendar.getInstance();

    year  = date.get(Calendar.YEAR)%100;
    month = date.get(Calendar.MONTH)+1;
    day   = date.get(Calendar.DAY_OF_MONTH);

    hour  = date.get(Calendar.HOUR_OF_DAY);
    min   = date.get(Calendar.MINUTE);
    sec   = date.get(Calendar.SECOND);

    zone  = date.get(Calendar.ZONE_OFFSET)/(1000*60*60);
  }

  public SMSTimeStamp(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    year     = readUnsignedNibble(in);
    month    = readUnsignedNibble(in);
    day      = readUnsignedNibble(in);

    hour     = readUnsignedNibble(in);
    min      = readUnsignedNibble(in);
    sec      = readUnsignedNibble(in);

    zone     = readSignedNibble(in);
  }

  public void writeTo(OutputStream out)throws IOException{
    writeUnsignedNibble(out,year);
    writeUnsignedNibble(out,month);
    writeUnsignedNibble(out,day);

    writeUnsignedNibble(out,hour);
    writeUnsignedNibble(out,min);
    writeUnsignedNibble(out,sec);

    writeSignedNibble(out,zone);
  }

  static public int readUnsignedNibble(InputStream in)throws IOException{
    int b=in.read()&0x00FF;
    int l=(b>>4)&0x0F;
    int h=(b&0x0F)*10;
    return h+l;
  }

  static public int readSignedNibble(InputStream in)throws IOException{
    int b=in.read()&0x00FF;
    int l=(b>>4)&0x0F;
    int h=(b&0x07)*10;
    return ((b&0x08)==0)?h+l:-(h+l);
  }

  static public void writeUnsignedNibble(OutputStream out,int b)throws IOException{
    int l= (b%10)&0x0F;
    int h= (b/10)&0x0F;
    out.write((l<<4)|h);
  }

  static public void writeSignedNibble(OutputStream out,int b)throws IOException{
    boolean sign=(b<0);
    if(sign){b=-b;}
    int l= (b%10)&0x0F;
    int h= (b/10)&0x07;
    if(sign){h|=0x08;}
    out.write((l<<4)|h);
  }

  public String toString(){
    return ""+year+"-"+month+"-"+day+" "+hour+":"+min+":"+sec+" GMT "+zone;
  }
}

// [1] ETSI TS 123 040 (2004-09)
