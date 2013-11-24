package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ResetB3Resp extends MsgOut{
  public ResetB3Resp(int appid, int ncci){
    super(0,appid,CAPI_RESET_B3,CAPI_RESP,ncci);
  }
}