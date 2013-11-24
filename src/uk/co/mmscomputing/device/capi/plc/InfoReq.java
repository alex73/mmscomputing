package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.*;
import uk.co.mmscomputing.device.capi.parameter.*;

public class InfoReq extends MsgOut{

  public InfoReq(int appid, int plci, StructOut calledPN,StructOut addInfo){
    super(calledPN.getLength()+addInfo.getLength(),appid,CAPI_INFO,CAPI_REQ,plci&0x0000FFFF);
    writeStruct(calledPN);
    writeStruct(addInfo);
  }

  public InfoReq(int appid, int plci, String calledPN){
    this(appid,plci,new CalledPartyNumber.Out(calledPN),StructOut.empty);
  }

  public InfoReq(int appid, int plci, String calledPN, AdditionalInfo.Out additionalInfo){
    this(appid,plci,new CalledPartyNumber.Out(calledPN),additionalInfo);
  }
}