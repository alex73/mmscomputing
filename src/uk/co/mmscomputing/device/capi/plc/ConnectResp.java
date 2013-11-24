package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.*;

public class ConnectResp extends MsgOut{

  public ConnectResp(
    int appid,
    int plci,
    int reject,                     //  flags
    StructOut bprotocol,
    StructOut connectedNumber,
    StructOut connectedSubAddress,
    StructOut llc,                  //  low layer compatibility
    StructOut addInfo
  ){
    super(
       2
      +bprotocol.getLength()
      +connectedNumber.getLength()
      +connectedSubAddress.getLength()
      +llc.getLength()
      +addInfo.getLength(),
      appid,CAPI_CONNECT,CAPI_RESP,plci
    );
    writeWord(reject);
    writeStruct(bprotocol);
    writeStruct(connectedNumber);
    writeStruct(connectedSubAddress);
    writeStruct(llc);
    writeStruct(addInfo);
  }

  public ConnectResp(
    int appid,
    int plci,
    int reject,                     //  flags
    StructOut bprotocol,
    String connectedNumber,
    String connectedSubAddress,
    StructOut llc,                  //  low layer compatibility
    StructOut addInfo
  ){
    this(
      appid,
      plci,
      reject,
      bprotocol,
      (connectedNumber.equals(""))?StructOut.empty:new CallingPartyNumber.Out(connectedNumber),
      (connectedSubAddress.equals(""))?StructOut.empty:new CallingPartySubAddress.Out(connectedSubAddress),
      llc,
      addInfo
    );
  }

  public ConnectResp(
    int appid,
    int plci,
    int reject                      //  flags
  ){
    this(
      appid,
      plci,
      reject,
      StructOut.empty,
      StructOut.empty,
      StructOut.empty,
      StructOut.empty,
      StructOut.empty
    );
  }
}

