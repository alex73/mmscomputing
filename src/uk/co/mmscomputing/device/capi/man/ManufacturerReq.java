package uk.co.mmscomputing.device.capi.man;

import uk.co.mmscomputing.device.capi.*;

public class ManufacturerReq extends MsgOut implements ManufacturerConstants{
  public ManufacturerReq(int len,int appid,int ctrl,int manuid){
    super(4+len,appid,CAPI_MANUFACTURER,CAPI_REQ,ctrl);
    writeDWord(manuid);
  }
}