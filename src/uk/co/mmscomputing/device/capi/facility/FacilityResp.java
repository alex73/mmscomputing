package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class FacilityResp extends MsgOut{

  public FacilityResp(int appid, int ncci,int selector){
    super(3,appid,CAPI_FACILITY,CAPI_RESP,ncci);
    writeWord(selector);
    writeStruct();					      // Handset, DTMF, V42bis, Power Managment
  }

  public FacilityResp(int appid, int ncci,int selector,StructOut facRespParam){
    super(2+facRespParam.getLength(),appid,CAPI_FACILITY,CAPI_RESP,ncci);
    writeWord(selector);
    writeStruct(facRespParam);		// Supplementary services (see CAPI Part III)
  }
}