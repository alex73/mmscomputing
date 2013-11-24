package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class SupServiceReq extends FacilityReq{

  public SupServiceReq(int appid, int lineid,int service,StructOut parameter){
    super(appid,lineid,CAPI_FACILITY_SUPSERVICE,getFacReqParam(service,parameter));
  }

  static private StructOut getFacReqParam(int service,StructOut parameter){
    StructOut param=new StructOut(2+parameter.getLength());
    param.writeWord(service);
    param.writeStruct(parameter);				        //    System.out.println("SupServiceReq = "+toString(param.getBytes()));
    return param;					              
  }

  static public class GetSupportedServicesReq extends SupServiceReq{
    public GetSupportedServicesReq(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_GETSUPPORTEDSERVICES,StructOut.empty);
    }
  }
/*  
  static public SupServiceReq getListenReq(int appid, int lineid,int notificationMask){
    StructOut so=new StructOut(4);
    so.writeDWord(notificationMask);
    return new SupServiceReq(appid,lineid,CAPI_SERVICES_LISTEN,so);
  }

  static public SupServiceReq getListenReq(int appid, int lineid){
    return getListenReq(appid,lineid,0x035F);
  }
*/
  
  static public class ListenReq extends SupServiceReq{

    static final public String[] notificationStrings={
      "Hold / Retrieve Notifications",
      "Terminal portability Notifications",
      "ECT Notifications",
      "3PTY Notifications",
      "Call Forwarding/Deflection Notifications/Information",
      "reserved (no Notifications for Call-Deflection)",
      "reserved (no Notifications for MCID)",
      "CCBS Notifications/Information",
      "MWI Indication",
      "CCNR Notification",
      "CONF Notifications/Information"
    };

    public ListenReq(int appid, int lineid){
      this(appid,lineid,0x035F);
    }

    public ListenReq(int appid, int lineid,int notificationMask){
      super(appid,lineid,CAPI_SERVICES_LISTEN,getFacReqParam(notificationMask));
    }

    static private StructOut getFacReqParam(int notificationMask){
      StructOut ssparam=new StructOut(4);
      ssparam.writeDWord(notificationMask);
      return ssparam;
    }
  }

  static public class HoldReq extends SupServiceReq{
    public HoldReq(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_HOLD,StructOut.empty);
    }
  }

  static public class RetrieveReq extends SupServiceReq{
    public RetrieveReq(int appid, int lineid){
      super(appid,lineid,CAPI_SERVICES_RETRIEVE,StructOut.empty);
    }
  }
/*
  static public SupServiceReq getHoldReq(int appid, int lineid){
    return new SupServiceReq(appid,lineid,CAPI_SERVICES_HOLD,StructOut.empty);
  }
  
  static public SupServiceReq getRetrieveReq(int appid, int lineid){
    return new SupServiceReq(appid,lineid,CAPI_SERVICES_RETRIEVE,StructOut.empty);
  }
*/  
  // Q.931 4.5.6 p73 max default info element length is 10 => max 8 byte identifier

  static public SupServiceReq getSuspendReq(int appid, int lineid,byte[] identifier){
    // assert(identifier.length<=8)
    StructOut so=new StructOut(identifier.length);
    so.writeData(identifier,0,identifier.length);
    return new SupServiceReq(appid,lineid,CAPI_SERVICES_SUSPEND,so);
  }
  
  // Q.931 4.5.6 p73 max default info element length is 10 => max 8 byte identifier

  static public SupServiceReq getResumeReq(int appid, int lineid,byte[] identifier){
    // assert(identifier.length<=8)
    StructOut so=new StructOut(identifier.length);
    so.writeData(identifier,0,identifier.length);
    return new SupServiceReq(appid,lineid,CAPI_SERVICES_RESUME,so);
  }
  
  static public class ECTReq extends SupServiceReq{                   // Call in state P-Held

    static private StructOut getHeldPLCIParam(int plci){
      StructOut so=new StructOut(4);
      so.writeDWord(plci);
      return so;
    }

    public ECTReq(int appid, int heldplci){
      super(appid,heldplci,CAPI_SERVICES_ECT,getHeldPLCIParam(heldplci));
    }

    public ECTReq(int appid, int activeplci, int heldplci){
      super(appid,activeplci,CAPI_SERVICES_ECT,getHeldPLCIParam(heldplci));
    }
  }
/*
  static public SupServiceReq getECTReq(int appid, int plci){         // Call in state P-Held
    StructOut so=new StructOut(4);
    so.writeDWord(plci);
    return new SupServiceReq(appid,plci,CAPI_SERVICES_ECT,so);
  }
*/

  static public SupServiceReq get3PTYBeginReq(int appid, int plci){   // Call in state P-Held
    StructOut so=new StructOut(4);
    so.writeDWord(plci);
    return new SupServiceReq(appid,plci,CAPI_SERVICES_PTY3BEGIN,so);
  }
  
  static public SupServiceReq get3PTYEndReq(int appid, int plci){     // Call in state P-Held
    StructOut so=new StructOut(4);
    so.writeDWord(plci);
    return new SupServiceReq(appid,plci,CAPI_SERVICES_PTY3END,so);
  }
}