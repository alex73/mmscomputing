package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectB3ActiveResp extends MsgOut{
  public ConnectB3ActiveResp(int appid, int ncci){
    super(0,appid,CAPI_CONNECT_B3_ACTIVE,CAPI_RESP,ncci);
  }
}