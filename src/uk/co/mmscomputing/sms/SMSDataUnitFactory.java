package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDataUnitFactory implements SMSConstants{

  static public SMSDataUnit decode(boolean sc2ms,byte[] bytes)throws IOException{
    int type  = bytes[0]&0x007F;                                 // [2] p.15 5.3.2.1 Data Link Layer
    switch(type){                                                // DLL data unit type
    case SMS_DLL_ERROR: return new SMSDLLError(bytes[1]&0x00FF); // Data Link Layer Error
    case SMS_DLL_EST:   return new SMSDLLEstablished();          // Data Link Layer connection established
    case SMS_DLL_REL:   return new SMSDLLReleased();             // Data Link Layer connection released
    }

    int flags = bytes[1]&0x00FF;                           // [1] p.54 9.2.3.1 TP-Message-Type-Indicator (TP-MTI)

    InputStream in=new ByteArrayInputStream(bytes,1,bytes.length-1);
    if(sc2ms){                                             // Mobil Station input stream [SC->MS]
      switch(type){
      case SMS_DLL_DATA:                                   // Message carries SMS data

        switch(flags&0x03){
        case 0: return new SMSDeliver(in);                 // SMS-Deliver
        case 2: return new SMSStatusReport(in);            // SMS-STATUS-REPORT
        case 3: return new SMSDeliver(in);                 // Reserved; treat as SMSDeliver [1] p.54
        }

        break;
      case SMS_DLL_ACK:                                    // Message carries positiv acknowledgement

        switch(flags&0x03){
        case 1: return new SMSSubmitAckReport(in);         // SMS-Submit-Report
        }

      case SMS_DLL_NACK:                                   // Message carries negativ acknowledgement

        switch(flags&0x03){
        case 1: return new SMSSubmitErrReport(in);         // SMS-Submit-Report
        }

      }
    }else{                                                 // Service Center input stream [MS->SC]
      switch(type){
      case SMS_DLL_DATA:                                   // Message carries SMS data

        switch(flags&0x03){
        case 1: return new SMSSubmit(in);                  // SMS-Submit
        case 2: return new SMSCommand(in);                 // SMS-Command
        }

        break;
      case SMS_DLL_ACK:                                    // Message carries positiv acknowledgement

        switch(flags&0x03){
        case 0: return new SMSDeliverAckReport(in);        // SMS-Deliver
        }

      case SMS_DLL_NACK:                                   // Message carries negativ acknowledgement

        switch(flags&0x03){
        case 0: return new SMSDeliverErrReport(in);        // SMS-Deliver
        }

      }
    }

//      String s="uk.co.mmscomputing.sms.SMSDataUnitFactory.decode:\nUnknown SMSDataUnit!\n";
//      s+="type="+Integer.toBinaryString(type)+"b flags="+Integer.toBinaryString(flags)+"b\n";
//      System.out.println(s);

    throw new SMSException.Type();
  }
}

// [1] ETSI TS 123 040 (2004-09)
// [2] ETSI ES 201 912 V1.2.1 (2004-06)

