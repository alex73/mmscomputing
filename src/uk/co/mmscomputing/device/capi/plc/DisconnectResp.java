package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class DisconnectResp extends MsgOut{
  public DisconnectResp(int appid, int plci){
    super(0,appid,CAPI_DISCONNECT,CAPI_RESP,plci&0x0000FFFF);
  }
}