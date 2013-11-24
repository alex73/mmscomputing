package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;

public class AVMDTraceReq extends AVMReq implements AVMConstants{

//  public static final int START = 0x00000302;    // dtrace32 [default: -d3 Layer 3 only]
  public static final int START = 0x00000C80;    // dtrace32 -d2 
//  public static final int START = 0x00000F80;    // dtrace32 -d3 -d2
//  public static final int START = 0x00000F3F;    // dtrace32 -d3 -b2
  public static final int END   = 0x00000000;

  public AVMDTraceReq(int appid,int cntl,int flags){
    super(
        appid,
        cntl,
        0,                                       // class:
        CAPI_AVM_DTRACE,                         // function: CAPI_AVM_DTRACE = 1
        getFlagsStruct(flags)
    );
  }

  private static StructOut getFlagsStruct(int flags){
    StructOut data=new StructOut(4);             
    data.writeDWord(flags);                     
    return data;
  }

  static public class Start extends AVMDTraceReq{
    public Start(int appid,int cntl){super(appid,cntl,START);}
  }

  static public class Stop extends AVMDTraceReq{
    public Stop(int appid,int cntl){super(appid,cntl,END);}
  }
}
