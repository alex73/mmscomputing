package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSCommand extends SMSTransportProtocolDataUnit{

/*
  ETSI TS 123 040 (2004-09)
  9.2.3.19 TP-Command-Type (TP-CT)

Value (bit 7 .. 0)  | Command Description                                     | Status Report Request Value
-----------------------------------------------------------------------------------------------------------
    00000000        | Enquiry relating to previously submitted short message  | 1
    00000001        | Cancel Status Report Request relating to previously     | 0
                    | submitted short message                                 |
    00000010        | Delete previously submitted Short Message               | 0
    00000011        | Enable Status Report Request relating to previously     | 0
                    | submitted short message                                 |
 00000100..00011111 | Reserved unspecified                                    |
 11100000..11111111 | Values specific for each SC 1 or 0                      |
*/

  public SMSCommand(int mr,String da,int ct,int mn){    
    this(mr,da,ct,mn,new byte[0]);
  }

  public SMSCommand(int mr,String da,int ct,int mn,byte[] cd){    
    put("TP-MTI",new Integer(0x02));                      // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-SRR",new Boolean(ct==0));                     // bit 5      TP-Status-Report-Request (TP-SRR)
    put("TP-UDHI",new Boolean(false));                    // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)

    put("TP-MR",new Integer(mr));                         // TP-Message-Reference (TP-MR)
    put("TP-PID",new Integer(0));                         // TP-Protocol-Identifier (TP-PID)
    put("TP-CT",new Integer(ct));                         // TP-Command-Type (TP-CT)
    put("TP-MN",new Integer(mn));                         // TP-Message-Number (TP-MN) in SC to operate on
    put("TP-DA",new SMSAddress(da));                      // TP-Destination-Address (TP-DA)
    setUserData(cd);
  }

  public SMSCommand(InputStream in)throws IOException{
    readFrom(in);
  }

  public void readFrom(InputStream in)throws IOException{
    int flags = read(in);                             

    put("TP-MTI",new Integer(flags&0x03));                // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    put("TP-SRR",new Boolean(((flags >> 5)&0x01)==0));    // bit 5      TP-Status-Report-Request (TP-SRR)
    put("TP-UDHI",new Boolean(((flags >> 6)&0x01)!=0));   // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)

    put("TP-MR",new Integer(readInt(in)));                // TP-Message-Reference (TP-MR)
    put("TP-PID",new Integer(readOctet(in)));             // TP-Protocol-Identifier (TP-PID)
    put("TP-CT",new Integer(readOctet(in)));              // TP-Command-Type (TP-CT)
    put("TP-MN",new Integer(readInt(in)));                // TP-Message-Number (TP-MN) in SC to operate on
    put("TP-DA",new SMSAddress(in));                      // TP-Destination-Address (TP-DA)
    readUserDataFrom(in);                                 // TP-CDL, TP-CD
  }

  public void writeTo(OutputStream out)throws IOException{
    int flags = 0x02;                                     // bit 0,1    TP-Message-Type-Indicator (TP-MTI)
    if(getBoolean("TP-SRR")){flags|=(1<<5);}              // bit 5      TP-Status-Report-Request (TP-SRR)

    out.write(SMS_DLL_DATA);
    out.write(flags);                              
    out.write(getInt("TP-MR"));                           // TP-Message-Reference (TP-MR)
    out.write(getOctet("TP-PID"));                        // TP-Protocol-Identifier (TP-PID)
    out.write(getOctet("TP-CT"));                         // TP-Command-Type (TP-CT)
    out.write(getInt("TP-MN"));                           // TP-Message-Number (TP-MN)
    ((SMSAddress)get("TP-DA")).writeTo(out);              // TP-Destination-Address (TP-DA)
    writeUserDataTo(out);                                 // max 157 bytes
    out.flush();                                          // send data unit
  }
}

// [1] ETSI TS 123 040 (2004-09)


