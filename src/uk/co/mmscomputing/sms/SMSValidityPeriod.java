package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSValidityPeriod extends SMSTransportProtocolParameter{

  // [1] p.55 9.2.3.3  TP-Validity-Period-Format (TP-VPF)
  // [1] p.59 9.2.3.12 TP-Validity-Period (TP-VP)

  private int vpf=0;
  private int vp=0;

  public SMSValidityPeriod(){}
  public SMSValidityPeriod(int vpf){this.vpf=vpf;}

  public SMSValidityPeriod(int vpf,InputStream in)throws IOException{
    this(vpf);readFrom(in);
  }

  public int getTimeOut(){return vp;}

  public void readFrom(InputStream in)throws IOException{
    switch(vpf){
    case 0: break;                                         // TP-VP field not present
    case 1: readEnhancedFormat(in);                        // TP-VP field present - enhanced format
    case 2: readRelativeFormat(in);                        // TP-VP field present - relative format
    case 3: readAbsoluteFormat(in);                        // TP-VP field present - absolute format
    }
  }

  private void readRelativeFormat(InputStream in)throws IOException{
    vp=read(in);
    if(vp<144){                 //   0 to 143 (TP-VP + 1) x 5 minutes (i.e. 5 minutes intervals up to 12 hours)
      vp=(vp+1)*5*60;
    }else if(vp<168){           // 144 to 167 12 hours + ((TP-VP -143) x 30 minutes)
      vp=12*60*60+(vp-143)*30*60;
    }else if(vp<197){           // 168 to 196 (TP-VP - 166) x 1 day
      vp=24*60*60*(vp-166);
    }else{                      // 197 to 255 (TP-VP - 192) x 1 week
      vp=7*24*60*60*(vp-192);
    }
  }

  private void readEnhancedFormat(InputStream in)throws IOException{
    byte[] bytes=new byte[7];               // The presence of all 7 octets is mandatory
    int len=in.read(bytes);
    if(len!=7){throw new IOException(getClass().getName()+".readEnhancedFormat:\n\tUnexpected end of stream.");}
    in=new ByteArrayInputStream(bytes);
    int fi=read(in);                        // Octet 1 TP-VP functionality indicator
    while((fi&0x80)!=0){                    // If extension bit set read another functionality indicator
      fi=read(in);                          // big endian; most significant first
    }

    boolean  ss =((fi >> 6)&0x01)==1;       // bit 6      Single shot SM; SC shall make only one delivery attempt.

    switch(fi&0x07){
    case 0: break;                          // No Validity Period specified
    case 1:                                 // As in relative case
      readRelativeFormat(in);
      break;
    case 2:                                 // 1 to 255 seconds; 0 reserved
      vp = read(in);
      break;
    case 3: 
      int hour     = SMSTimeStamp.readUnsignedNibble(in);
      int min      = SMSTimeStamp.readUnsignedNibble(in);
      int sec      = SMSTimeStamp.readUnsignedNibble(in);
      vp=hour*60*60+min*60+sec;
      break;
    }
  }

  private void readAbsoluteFormat(InputStream in)throws IOException{
    SMSTimeStamp time=new SMSTimeStamp(in);
  }

  public void writeTo(OutputStream out)throws IOException{
  }

  public String toString(){
    String s=getClass().getName()+"\n";
    s+="validity period = "+vp;
    return s;
  }
}

// [1] ETSI TS 123 040 (2004-09)
