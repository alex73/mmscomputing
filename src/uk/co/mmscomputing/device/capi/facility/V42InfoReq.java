package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class V42InfoReq extends FacilityReq{

  public V42InfoReq(int appid,int ncci,StructOut fct){
    super(appid,ncci,CAPI_FACILITY_V42bis,fct);
  }

  public V42InfoReq(int appid,int ncci,int fct){
    this(appid,ncci,getFacReqParam(fct));
  }

  public V42InfoReq(int appid, int ncci){
    this(appid,ncci,0);                 //  request compression information
  }

  static private StructOut getFacReqParam(int fct){
    StructOut param=new StructOut(2);
    param.writeWord(fct);				        
    return param;					              
  }

}