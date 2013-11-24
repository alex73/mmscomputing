package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

// Eicon SDK: eicon/sdk/Doc/CxEcho.pdf

public class EchoCancellerReq extends FacilityReq{
  public EchoCancellerReq(int appid, int ncci,StructOut frp){
    super(appid,ncci,CAPI_FACILITY_ECHOCANCELLATION,frp);
  }

  static public class GetSupportedServicesReq extends EchoCancellerReq{             
    public GetSupportedServicesReq(int appid,int cntl){
     super(appid,cntl,getGetSupportedServicesStruct());
    }
  }

  static private StructOut getGetSupportedServicesStruct(){
    StructOut s=new StructOut(3);
    s.writeWord(0);                                   // function 0: GetSupportedServices
    s.writeByte(0);                                   // empty struct
    return s;
  }

  static public class EnableReq extends EchoCancellerReq{             
    public EnableReq(int appid,int cntl,int options,int maxTailLength,int maxPreDelay){
     super(appid,cntl,getEnableStruct(options,maxTailLength,maxPreDelay));
    }
  }

  static private StructOut getEnableStruct(int options,int maxTailLength,int maxPreDelay){
    StructOut ecrp=new StructOut(6);                  // echo canceller request parameter
    ecrp.writeWord(options);                          //
    ecrp.writeWord(maxTailLength);                    //
    ecrp.writeWord(maxPreDelay);                      //
    StructOut s=new StructOut(2+ecrp.getLength());
    s.writeWord(1);                                   // function 1: Enable
    s.writeStruct(ecrp);                              // echo canceller request parameter
    return s;
  }

  static public class DisableReq extends EchoCancellerReq{             
    public DisableReq(int appid,int cntl){
     super(appid,cntl,getDisableStruct());
    }
  }

  static private StructOut getDisableStruct(){
    StructOut s=new StructOut(3);
    s.writeWord(2);                                   // function 2: Disable
    s.writeByte(0);                                   // empty struct
    return s;
  }

}