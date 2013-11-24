package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSStatusReport extends SMSTransportProtocolDataUnit{

  public SMSStatusReport(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-MMS",new Boolean(((flags >> 2)&0x01)==0));    // bit 2      TP-More-Messages-to-Send (TP-MMS)
    put("TP-SRQ",new Boolean(((flags >> 5)&0x01)==0));    // bit 5      TP-Status-Report-Qualifier (TP-SRQ)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)

    put("TP-MR",new Integer(readInt(in)));                // TP-Message-Reference (TP-MR)
    put("TP-RA",new SMSAddress(in));                      // TP-Recipient-Address (TP-RA)
    put("TP-SCTS",new SMSTimeStamp(in));                  // TP-Service-Centre-Time-Stamp (TP-SCTS)
    put("TP-DT",new SMSTimeStamp(in));                    // TP-Discharge-Time (TP-DT)
    put("TP-ST",new Integer(readOctet(in)));              // TP-Status (TP-ST)
    int pi=readOctet(false,in);
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

}



