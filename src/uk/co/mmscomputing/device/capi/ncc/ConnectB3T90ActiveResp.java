package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectB3T90ActiveResp extends MsgOut{
  public ConnectB3T90ActiveResp(int appid, int ncci){
    super(0,appid,CAPI_CONNECT_B3_T90_ACTIVE,CAPI_RESP,ncci);
  }
}