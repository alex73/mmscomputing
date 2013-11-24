package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class SupServiceResp extends FacilityResp{

  public SupServiceResp(int appid, int lineid,int service,StructOut parameter){
    super(appid,lineid,CAPI_FACILITY_SUPSERVICE,getFacReqParam(service,parameter));
  }

  public SupServiceResp(int appid, int lineid,int service){
    super(appid,lineid,CAPI_FACILITY_SUPSERVICE,getFacReqParam(service,StructOut.empty));
  }

  static private StructOut getFacReqParam(int service,StructOut parameter){
    StructOut param=new StructOut(2+parameter.getLength());
    param.writeWord(service);
    param.writeStruct(parameter);				        //    System.out.println("SupServiceResp = "+toString(param.getBytes()));
    return param;					              
  }

  static public class HoldResp extends SupServiceResp{
    public HoldResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_HOLD);
    }
  }

  static public class RetrieveResp extends SupServiceResp{
    public RetrieveResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_RETRIEVE);
    }
  }

  static public class ECTResp extends SupServiceResp{
    public ECTResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_ECT);
    }
  }

}

