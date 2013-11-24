package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class InfoResp extends MsgOut{
  public InfoResp(int appid, int plci){
    super(0,appid,CAPI_INFO,CAPI_RESP,plci&0x0000FFFF);
  }
}