package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class HandsetReq extends FacilityReq{
  public HandsetReq(int appid, int ncci){
    super(appid,ncci,CAPI_FACILITY_HANDSET,StructOut.empty);
  }
}