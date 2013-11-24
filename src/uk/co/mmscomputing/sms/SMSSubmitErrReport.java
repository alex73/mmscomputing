package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSSubmitErrReport extends SMSErrReport{

  public SMSSubmitErrReport(int err){setErrorCode(err);}

  public SMSSubmitErrReport(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)

    put("TP-FCS",new Integer(readInt(in)));               // TP-Failure-Cause (TP-FCS) 

    int pi=readOctet(in);
    put("TP-PI",new Integer(pi));                         // TP-Parameter-Indicator (TP-PI) 

    put("TP-SCTS",new SMSTimeStamp(in));                  // TP-Service-Centre-Time-Stamp (TP-SCTS)

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
    out.write(SMS_DLL_NACK);
    out.write(0x01);                                      // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
                                                          // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)
                                                          //    = 0 :   no user data headers
    out.write(getErrorCode());                            // TP-Failure-Cause (TP-FCS)
    out.write(0x00);                                      // TP-Parameter-Indicator (TP-PI) 
                                                          //    = 0 :   no additional parameters
    put("TP-SCTS",new SMSTimeStamp());                    // TP-Service-Centre-Time-Stamp (TP-SCTS)
    ((SMSTimeStamp)get("TP-SCTS")).writeTo(out);          // TP-Service-Centre-Time-Stamp (TP-SCTS)
    out.flush();
  }
}

// [1] ETSI TS 123 040 (2004-09)




