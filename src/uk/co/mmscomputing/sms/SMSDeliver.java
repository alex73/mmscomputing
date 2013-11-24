package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDeliver extends SMSTransportProtocolDataUnit{

  // Transport Layer Message; Send Short Message from Service Center to Mobil Station (SM from SC to MS)
  // [1] p.43 9.2.2.1 SMS-DELIVER

  public SMSDeliver(String tpoa){
    this(tpoa,"");
  }

  public SMSDeliver(String tpoa,String msg){
    put("TP-OA",new SMSAddress(tpoa));                    // TP-Originating-Address (TP-OA)
    put("TP-PID",new Integer(0));                         // TP-Protocol-Identifier (TP-PID)
    put("TP-DCS",new SMSDataCodingScheme(0x00F1));        // TP-Data-Coding-Scheme (TP-DCS) [2]: GSM 7 bit default alphabet

    setUserData(msg.getBytes());
  }

  public SMSDeliver(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-MMS",new Boolean(((flags >> 2)&0x01)!=0));    // bit 2      TP-More-Messages-to-Send (TP-MMS)
    put("TP-SRI",new Boolean(((flags >> 5)&0x01)!=0));    // bit 5      TP-Status-Report-Indication (TP-SRI)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)
    put("TP-RP" ,new Boolean(((flags >> 7)&0x01)!=0));    // bit 7      TP-Reply-Path (TP-RP)

    put("TP-OA",new SMSAddress(in));                      // TP-Originating-Address (TP-OA)
    put("TP-PID",new Integer(readOctet(in)));             // TP-Protocol-Identifier (TP-PID)
    put("TP-DCS",new SMSDataCodingScheme(readOctet(in))); // TP-Data-Coding-Scheme (TP-DCS) [2]
    put("TP-SCTS",new SMSTimeStamp(in));                  // TP-Service-Centre-Time-Stamp (TP-SCTS)

    readUserDataFrom(in);                                 // TP-UDL,TP-UD
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_DATA);
    out.write(0x00);                                      // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    ((SMSAddress)get("TP-OA")).writeTo(out);              // TP-Originating-Address (TP-OA) 2..12
    out.write(getOctet("TP-PID"));                        // TP-Protocol-Identifier (TP-PID)
    ((SMSDataCodingScheme)get("TP-DCS")).writeTo(out);    // TP-Data-Coding-Scheme (TP-DCS) [2]
    put("TP-SCTS",new SMSTimeStamp());                    // TP-Service-Centre-Time-Stamp (TP-SCTS)
    ((SMSTimeStamp)get("TP-SCTS")).writeTo(out);          // TP-Service-Centre-Time-Stamp (TP-SCTS)
    writeUserDataTo(out);
    out.flush();                                          // send TPDU
  }
}

// [1] ETSI TS 123 040 (2004-09)
// [2] 3GPP TS 23.038 V7.0.0 (2006-03)
