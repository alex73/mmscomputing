package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.FacilityPartyNumber;

public class CallForwardReq{

/*

0x0009: CF Activate
0x000A: CF Deactivate
0x000B: CF Interrogate parameters
0x000C: CF Interrogate numbers


0x0000: CFU (Call Forwarding Unconditional)
0x0001: CFB (Call Forwarding Busy)
0x0002: CFNR (Call Forwarding No Reply)

*/


  static public class InterrogateParametersReq extends SupServiceReq{

    public InterrogateParametersReq(
        int appid,  int lineid,int handle, String servedUserNumber
    ){
      super(appid,lineid,CAPI_SERVICES_CF_INTERROGATEPARAMETERS,
          getFacReqParam(handle,0x00,0x00,servedUserNumber)
      );
    }

    public InterrogateParametersReq(
        int appid,  int lineid,
        int handle, int type,  int service,
        String servedUserNumber
    ){
      super(appid,lineid,CAPI_SERVICES_CF_INTERROGATEPARAMETERS,
          getFacReqParam(handle,type,service,servedUserNumber)
      );
    }

    static private StructOut getFacReqParam(
        int handle,int type,int service,
        String servedUserNumber
    ){
      StructOut ssparam;

      if(servedUserNumber.equals("")){
        ssparam=new StructOut(9);
        ssparam.writeDWord(handle);
        ssparam.writeWord(type);
        ssparam.writeWord(service);
        ssparam.writeByte(0);
      }else{
        FacilityPartyNumber.Out   fpn  = new FacilityPartyNumber.Out(servedUserNumber);

        ssparam=new StructOut(8+fpn.getLength());
        ssparam.writeDWord(handle);
        ssparam.writeWord(type);
        ssparam.writeWord(service);
        ssparam.writeStruct(fpn);              // System.out.println(toString(ssparam.getBytes()));
      }
      return ssparam;
    }
  }
}

