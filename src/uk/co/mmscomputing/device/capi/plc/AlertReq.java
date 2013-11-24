package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.*;

public class AlertReq extends MsgOut{

  public AlertReq(int appid, int plci){
    super(1,appid,CAPI_ALERT,CAPI_REQ,plci);
    writeStruct();                              //  no additional info
  }

  public AlertReq(int appid, int plci,AdditionalInfo.Out addinfo){
    super(addinfo.getLength(),appid,CAPI_ALERT,CAPI_REQ,plci);
    writeStruct(addinfo);                       // additional info
  }
}