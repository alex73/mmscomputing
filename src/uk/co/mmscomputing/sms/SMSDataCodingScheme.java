package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDataCodingScheme extends SMSTransportProtocolParameter{

  private int flags;
  private int msggroup=-1;
  private int msgcoding=-1;
  private int msgclass=-1;
  private boolean msgcompression=false;                  // Compression algorithm described in 3GPP TS 23.042

  public SMSDataCodingScheme(int flags){
    this.flags=flags&0x00FF;

    msggroup=(flags>>6)&0x03;
    switch(msggroup){
    case 0: group0(flags);break;
    case 1: group1(flags);break;
    case 2: group2(flags);break;
    case 3: group3(flags);break;
    }
  }

  public void group0(int flags){                         // General Data Coding indication
    msgcompression=(((flags>>5)&0x01)==1);               // Bit 5    	Message compression [1] 3.9 p.25
    if(!msgcompression){                                 // if compressed ignore bits 2..3
      msgcoding=((flags>>2)&0x03);                       // Bit 2..3	Message coding
    }
    if(((flags>>4)&0x01)==1){                            // Bit 4
      msgclass =( flags & 0x03);                         // Bit 0..1	Message class
    }
  }  

  public void group1(int flags){                         // Message Marked for Automatic Deletion Group
    msgcompression=(((flags>>5)&0x01)==1);               // Bit 5    	Message compression [1] 3.9 p.25
    if(!msgcompression){                                 // if compressed ignore bits 2..3
      msgcoding=((flags>>2)&0x03);                       // Bit 2..3	Message coding
    }
    if(((flags>>4)&0x01)==1){                            // Bit 4
      msgclass =( flags & 0x03);                         // Bit 0..1	Message class
    }
  }  

  public void group2(int flags){                         // Reserved coding groups
  }  

  public void group3(int flags){
    msggroup=((flags>>4)&0x0F);
    switch((flags>>4)&0x03){
    case 0: 
      break;
    case 1: 
      break;
    case 2: 
      break;
    case 3:                                              // Data coding/message class
                                                         // Bit 3     is reserved, set to 0.
      msgcoding=((flags>>2)&0x01);                       // Bit 2	    Message coding
      msgclass =( flags    &0x03);                       // Bit 0..1	Message class
      break;
    }
  }  

  public int getCoding(){return msgcoding;}

  public void writeTo(OutputStream out)throws IOException{
    out.write(flags);
  }

  public String toString(){
    String s=getClass().getName()+":\n";
    s+="flags       = "+Integer.toBinaryString(flags)+"b\n";

    switch(msggroup){
    case 0: s+="General Data Coding indication\n";break;
    case 1: s+="Message Marked for Automatic Deletion Group\n";break;
    case 2: s+="Reserved coding groups\n";break;

    case 0x0C: s+="Message Waiting Indication Group: Discard Message\n";break;
    case 0x0D: s+="Message Waiting Indication Group: Store Message\n";break;
    case 0x0E: s+="Message Waiting Indication Group: Store Message\n";break;
    case 0x0F: s+="Data coding/message class\n";break;
    }
    switch(msgcoding){
    case 0: s+="GSM 7 bit default alphabet\n";break;
    case 1: s+="8 bit data\n";break;
    case 2: s+="UCS2 (16bit)\n";break;
    case 3: s+="Reserved\n";break;
    }
    switch(msgclass){
    case 0: s+="Class 0: Display message immediately on ME screen.\n";break;
    case 1: s+="Class 1: Default meaning: ME-specific\n";break;
    case 2: s+="Class 2: (U)SIM-specific message\n";break;
    case 3: s+="Class 3: Default meaning: TE specific (see 3GPP TS 27.005)\n";break;
    }
    if(msgcompression){
      s+="Use compression algorithm defined in 3GPP TS 23.042\n";
    }
    return s;
  }

  public static void main(String[] argv){
    try{
      System.out.println(new SMSDataCodingScheme(Integer.parseInt(argv[0],16)).toString());
    }catch(Exception e){
      System.out.println(e);
    }
  }
}

// [2] 3GPP TS 23.038 V7.0.0 (2006-03)


