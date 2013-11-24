package uk.co.mmscomputing.device.capi.man.avm;

public interface AVMConstants{
  static public final int CAPI_AVM_DTRACE          = 1;
  static public final int CAPI_AVM_GETBCHANNELINFO = 4;

  static public final String[] dProtocolStrings={
    "DSS1 (Euro-ISDN)",
    "CT1",
    "VN3",
    "NI1",
    "AUSTEL",
    "ESS",
    "MAX",
    "1TR6",
    "UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN"
  };

  static public final String[] lineStrings={
    "Point to point",
    "Point to multipoint",
    "Fixed, no D channel",
    "Fixed, with D channel",
    "UNKNOWN","UNKNOWN","UNKNOWN","UNKNOWN"
  };
}

