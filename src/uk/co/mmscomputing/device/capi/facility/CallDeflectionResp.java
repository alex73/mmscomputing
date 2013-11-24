package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.StructOut;

public class CallDeflectionResp extends SupServiceResp{

  public CallDeflectionResp(int appid, int lineid){
    super(appid,lineid,CAPI_SERVICES_CD,StructOut.empty);
  }
}
