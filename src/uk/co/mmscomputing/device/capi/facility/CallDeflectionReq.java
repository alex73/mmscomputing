package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.FacilityPartyNumber;
import uk.co.mmscomputing.device.capi.q931.CalledPartySubAddress;

public class CallDeflectionReq extends SupServiceReq{

// capi20-3.pdf p.12

  public CallDeflectionReq(int appid, int lineid,String dtn){
    this(appid,lineid,true,dtn,"");
  }

  public CallDeflectionReq(int appid, int lineid,String dtn,String dtsa){
    this(appid,lineid,true,dtn,dtsa);
  }

  public CallDeflectionReq(
      int appid, int lineid,
      boolean presentationAllowed,
      String deflectedToNumber,                          // facility party number
      String deflectedToSubaddress                       // called party subaddress
  ){
    super(appid,lineid,CAPI_SERVICES_CD,
        getFacReqParam(
          presentationAllowed,
          deflectedToNumber,
          deflectedToSubaddress
        )
    );
  }

  static private StructOut getFacReqParam(
      boolean presentationAllowed,
      String deflectedToNumber,                          // facility party number
      String deflectedToSubaddress                       // called party subaddress
  ){
    FacilityPartyNumber.Out   fpn  = new FacilityPartyNumber.Out(deflectedToNumber);
    CalledPartySubAddress.Out cpsa = new CalledPartySubAddress.Out(deflectedToSubaddress);
    

    StructOut ssparam=new StructOut(2+fpn.getLength()+cpsa.getLength());
    ssparam.writeWord((presentationAllowed)?1:0);
    ssparam.writeStruct(fpn);
    ssparam.writeStruct(cpsa);

    System.out.println(toString(ssparam.getBytes()));

    return ssparam;
  }
}

