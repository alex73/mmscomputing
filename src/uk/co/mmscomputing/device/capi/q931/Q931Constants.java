package uk.co.mmscomputing.device.capi.q931;

public interface Q931Constants{

  static public final int CCITT = 0;

  static public final String[] TypeOfNumberStrs = {
    "Unknown","International","National","Network Specific",
    "Subscriber","Reserved","Abbreviated","Reserved"
  };

  static public final String[] NumberingPlanStrs = {
    "Unknown", "ISDN/telephony (E.164)","Reserved","Data (X.121)",
    "Telex (F.69)","Reserved","Reserved","Reserved",
    "National Standard","Private" ,"Reserved","Reserved",
    "Reserved","Reserved","Reserved","Reserved"
  };

  static public final String[] InformationTransferCapabilityStrs = {
    "Speech","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
    "Unrestricted digital Information","Restricted digital Information","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
    "3.1kHz audio","Unrestricted digital Information with tones/announcements","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
    "Video","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown"
  };

  static public final String[] InformationTransferRateStrs = {
    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
    "64kbit/s","2 x 64kbit/s","Unknown","384kbit/s","Unknown","1536kbit/s","Unknown","1920kbit/s",
    "Multirate","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",
  };

  static public final String[] UserInformationLayer1ProtocolStrs = {
    "Reserved","Rate Adaption V.110 and X.30","G.711 u-law","G.711 A-law","G.721 32 kbit/s ADPCM and I.460","H.221 and H.242","Reserved","Non CCITT Rate Adaption",
    "Rate Adaption V.120","Rate Adaption X.31 HDLC flag stuffing","Reserved","Reserved","Reserved","Reserved","Reserved","Reserved",
    "Reserved","Reserved","Reserved","Reserved","Reserved","Reserved","Reserved","Reserved",
    "Reserved","Reserved","Reserved","Reserved","Reserved","Reserved","Reserved","Reserved",
  };

  static public final String[] MessageTypeStrs = {
    "ESC","ALERTING","CALL PROCEEDING","PROGRESS","Unknown","SETUP","Unknown","CONNECT",                    // 0x00
    "Unknown","Unknown","Unknown","Unknown","Unknown","SETUP ACKNOWLEDGE","Unknown","CONNECT ACKNOWLEDGE",  // 0x08

    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x10
    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x18

    "USER INFORMATION","SUSPEND REJECT","RESUME REJECT","Unknown","Unknown","SUSPEND","RESUME","Unknown",   // 0x20
    "Unknown","Unknown","Unknown","Unknown","Unknown","SUSPEND ACKNOWLEDGE","RESUME ACKNOWLEDGE","Unknown", // 0x28

    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x30
    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x38

    "Unknown","Unknown","Unknown","Unknown","Unknown","DISCONNECT","RESTART","Unknown",                     // 0x40
    "Unknown","Unknown","Unknown","Unknown","Unknown","RELEASE","RESTART ACKNOWLEDGE","Unknown",            // 0x48

    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x50
    "Unknown","Unknown","RELEASE COMPLETE","Unknown","Unknown","Unknown","Unknown","Unknown",               // 0x58

    "SEGMENT","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","Unknown",                        // 0x60
    "Unknown","Unknown","Unknown","Unknown","Unknown","Unknown","NOTIFY","Unknown",                         // 0x68

    "Unknown","Unknown","Unknown","Unknown","Unknown","STATUS ENQUIRY","Unknown","Unknown",                 // 0x70
    "Unknown","CONGESTION CONTROL","Unknown","INFORMATION","Unknown","STATUS","Unknown","Unknown",          // 0x78
  };

}