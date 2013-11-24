package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class FacilityReq extends MsgOut{
  public FacilityReq(int appid, int ncci,int selector,StructOut facReqParam){
    super(2+facReqParam.getLength(),appid,CAPI_FACILITY,CAPI_REQ,ncci);
    writeWord(selector);
    writeStruct(facReqParam);
  }
}