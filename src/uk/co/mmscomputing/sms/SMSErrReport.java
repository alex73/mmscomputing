package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSErrReport extends SMSTransportProtocolDataUnit{

  public SMSErrReport(){}

  public int  getErrorCode(){ return getInt("TP-FCS");}
  public void setErrorCode(int fcs){ put("TP-FCS",new Integer(fcs));}

  public String toString(){
    String s=super.toString();
    s+="error       = "+getErrorString(getErrorCode())+"\n";
    return s;
  }

  public String getErrorString(int fcs){
    switch(fcs){
                                                                        // 00 - 7F Reserved
    case 0x0080: return "Telematic interworking not supported";         // 80 - 8F TP-PID errors
    case 0x0081: return "Short message Type 0 not supported";
    case 0x0082: return "Cannot replace short message";
    case 0x008F: return "Unspecified TP-PID error";

    case 0x0090: return "Data coding scheme (alphabet) not supported";  // 90 - 9F TP-DCS errors
    case 0x0091: return "Message class not supported";
    case 0x009F: return "Unspecified TP-DCS error";

    case 0x00A0: return "Command cannot be actioned";                   // A0 - AF TP-Command Errors
    case 0x00A1: return "Command unsupported";
    case 0x00AF: return "Unspecified TP-Command error";

    case 0x00B0: return "TPDU not supported";

    case 0x00C0: return "SC busy";
    case 0x00C1: return "No SC subscription";
    case 0x00C2: return "SC system failure";
    case 0x00C3: return "Invalid SME address";
    case 0x00C4: return "Destination SME barred";
    case 0x00C5: return "SM Rejected-Duplicate SM";
    case 0x00C6: return "TP-VPF not supported";
    case 0x00C7: return "TP-VP not supported";

    case 0x00D0: return "(U)SIM SMS storage full";
    case 0x00D1: return "No SMS storage capability in (U)SIM";
    case 0x00D2: return "Error in MS";
    case 0x00D3: return "Memory Capacity Exceeded";
    case 0x00D4: return "(U)SIM Application Toolkit Busy";
    case 0x00D5: return "(U)SIM data download error";
                                                                        // E0 - FE Values specific to an application
    default: return "Unspecified error cause";
    }
  }
}

// [1] ETSI TS 123 040 (2004-09)
