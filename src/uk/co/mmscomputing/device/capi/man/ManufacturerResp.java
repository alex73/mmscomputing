package uk.co.mmscomputing.device.capi.man;

import uk.co.mmscomputing.device.capi.*;

public class ManufacturerResp extends MsgOut implements ManufacturerConstants{

  public ManufacturerResp(int len,int appid, int ctrl,int manuid){
    super(4+len,appid,CAPI_MANUFACTURER,CAPI_RESP,ctrl);
    writeDWord(manuid);
  }
}