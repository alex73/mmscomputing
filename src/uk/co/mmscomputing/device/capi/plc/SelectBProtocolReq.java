package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class SelectBProtocolReq extends MsgOut{
  public SelectBProtocolReq(int appid, int plci,StructOut bProtocol){
    super(bProtocol.getLength(),appid,CAPI_SELECT_B_PROTOCOL,CAPI_REQ,plci&0x0000FFFF);
    writeStruct(bProtocol);
//    System.out.println(getClass().getName()+": lineid="+Integer.toHexString(lineid));
  }
}