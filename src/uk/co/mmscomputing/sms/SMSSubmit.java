package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSSubmit extends SMSTransportProtocolDataUnit{

  // Send Short Message SM from Mobil Station to Service Center (MS to SC)

  public SMSSubmit(int tpmr,String tpda,String tpud){
    put("TP-MR",new Integer(tpmr));                       // TP-Message-Reference (TP-MR)
    put("TP-DA",new SMSAddress(tpda));                    // TP-Destination-Address (TP-DA)
    put("TP-PID",new Integer(0));                       // TP-Protocol-Identifier (TP-PID)

//  Following does not work: BT-Text -> or T-Mobile [Siemens MT50]
//    put("TP-PID",new Integer(0x5F));                      // TP-Protocol-Identifier (TP-PID)[create return call path]


    put("TP-DCS",new SMSDataCodingScheme(0x00F1));      // TP-Data-Coding-Scheme (TP-DCS) [2]: GSM 7 bit default alphabet
//    put("TP-DCS",new SMSDataCodingScheme(0x00F0));        // TP-Data-Coding-Scheme (TP-DCS) [2]: [send message straight to mobile screen]

    setUserData(tpud.getBytes());
  }

  public SMSSubmit(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-RD",new Boolean(((flags >> 2)&0x01)!=0));     // bit 2      TP-Reject-Duplicates (TP-RD)
    int vpf=(flags>>3)&0x03;
    put("TP-VPF",new Integer(vpf));                       // bit 3,4    TP-Validity-Period-Format (TP-VPF)
    put("TP-SRR",new Boolean(((flags >> 5)&0x01)!=0));    // bit 5      TP-Status-Report-Request (TP-SRR)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)
    put("TP-RP",new Boolean(((flags >> 7)&0x01)!=0));     // bit 7      TP-Reply-Path (TP-RP)
    put("TP-MR",new Integer(readInt(in)));                // TP-Message-Reference (TP-MR)
    put("TP-DA",new SMSAddress(in));                      // TP-Destination-Address (TP-DA)
    put("TP-PID",new Integer(readOctet(in)));             // TP-Protocol-Identifier (TP-PID)
    put("TP-DCS",new SMSDataCodingScheme(readOctet(in))); // TP-Data-Coding-Scheme (TP-DCS) [2]
    if(vpf!=0){
      put("TP-VP",new SMSValidityPeriod(vpf,in));         // TP-Validity-Period (TP-VP)
    }
    readUserDataFrom(in);
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_DATA);
    out.write(0x01);                                      // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
//    out.write(0x01 | (1<<6));                             // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    out.write(getInt("TP-MR"));                           // TP-Message-Reference (TP-MR)
    ((SMSAddress)get("TP-DA")).writeTo(out);              // TP-Destination-Address (TP-DA) 2..12
    out.write(getOctet("TP-PID"));                        // TP-Protocol-Identifier (TP-PID)
    ((SMSDataCodingScheme)get("TP-DCS")).writeTo(out);    // TP-Data-Coding-Scheme (TP-DCS) [2]
                                                          // TP-Validity-Period (TP-VP)
    writeUserDataTo(out);
    out.flush();                                          // send data unit
  }
}

// [1] ETSI TS 123 040 (2004-09)
// [2] 3GPP TS 23.038 V7.0.0 (2006-03)

