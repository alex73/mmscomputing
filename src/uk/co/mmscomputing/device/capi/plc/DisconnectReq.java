package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.*;

public class DisconnectReq extends MsgOut{

  public DisconnectReq(int appid, int plci){
    super(1,appid,CAPI_DISCONNECT,CAPI_REQ,plci&0x0000FFFF);
    writeStruct();              // no additional info
  }

  public DisconnectReq(int appid, int plci,AdditionalInfo.Out addinfo){
    super(addinfo.getLength(),appid,CAPI_DISCONNECT,CAPI_REQ,plci&0x0000FFFF);
    writeStruct(addinfo);       // additional info
//    System.out.println(getClass().getName()+": lineid="+Integer.toHexString(lineid));
  }
}