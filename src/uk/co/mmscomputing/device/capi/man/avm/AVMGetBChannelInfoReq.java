package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;

public class AVMGetBChannelInfoReq extends AVMReq implements AVMConstants{

  public AVMGetBChannelInfoReq(int appid,int cntl){
    super(
        appid,
        cntl,
        0,                                 // class:
        CAPI_AVM_GETBCHANNELINFO,          // function: CAPI_GETBCHANNELINFO = 4
        new StructOut(new byte[63])
    );
  }
}
