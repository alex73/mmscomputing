package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDeliverAckReport extends SMSTransportProtocolDataUnit{

  public SMSDeliverAckReport(){
  }

  public SMSDeliverAckReport(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)

    int pi=readOctet(in);
    put("TP-PI",new Integer(pi));                         // optional TP-Parameter-Indicator (TP-PI) 
    if((pi&0x0001)!=0){                                   // if bit 0 is set
      put("TP-PID",new Integer(readOctet(in)));           // TP-Protocol-Identifier (TP-PID)
    }
    if((pi&0x0002)!=0){                                   // if bit 1 is set
      put("TP-DCS",new SMSDataCodingScheme(readOctet(in))); // TP-Data-Coding-Scheme (TP-DCS)
    }
    if((pi&0x0004)!=0){                                   // if bit 2 is set
      readUserDataFrom(in);
    }
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_ACK);
    out.write(0x00);                         // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
                                             // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)
                                             //    = 0 :   no user data headers
    out.write(0x00);                         // TP-Parameter-Indicator (TP-PI) 
                                             //    = 0 :   no additional parameters
    out.flush();
  }
}

// [1] ETSI TS 123 040 (2004-09)



