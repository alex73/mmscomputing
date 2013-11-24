package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DisconnectB3Resp extends MsgOut{
  public DisconnectB3Resp(int appid, int ncci){
    super(0,appid,CAPI_DISCONNECT_B3,CAPI_RESP,ncci);
  }
}