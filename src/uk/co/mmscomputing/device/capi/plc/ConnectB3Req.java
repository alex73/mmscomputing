package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectB3Req extends MsgOut{

  public ConnectB3Req(int appid, int plci){
    super(1,appid,CAPI_CONNECT_B3,CAPI_REQ,plci);
    writeStruct();
  }

  public ConnectB3Req(int appid, int plci, StructOut ncpi){
    super(ncpi.getLength(),appid,CAPI_CONNECT_B3,CAPI_REQ,plci);
    writeStruct(ncpi);
  }
}