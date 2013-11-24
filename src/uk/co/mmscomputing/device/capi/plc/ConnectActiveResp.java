package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectActiveResp extends MsgOut{
  public ConnectActiveResp(int appid, int plci){
    super(0,appid,CAPI_CONNECT_ACTIVE,CAPI_RESP,plci);
  }
}