package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class CallForwardResp{

  static public class ActivateResp extends SupServiceResp{
    public ActivateResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_CF_ACTIVATE,StructOut.empty);
    }
  }

  static public class DeactivateResp extends SupServiceResp{
    public DeactivateResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_CF_DEACTIVATE,StructOut.empty);
    }
  }

  static public class InterrogateParametersResp extends SupServiceResp{
    public InterrogateParametersResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_CF_INTERROGATEPARAMETERS,StructOut.empty);
    }
  }

  static public class InterrogateNumbersResp extends SupServiceResp{
    public InterrogateNumbersResp(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_CF_INTERROGATENUMBERS,StructOut.empty);
    }
  }
}

