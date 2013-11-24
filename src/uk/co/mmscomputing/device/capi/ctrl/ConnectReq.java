package uk.co.mmscomputing.device.capi.ctrl;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.q931.*;

public class ConnectReq extends MsgOut{

  private String    calledPartyNumber;
  private String    callingPartyNumber;

  public ConnectReq(
    int appid, 
    int ctrl,
    int cip,
    StructOut called,
    StructOut calling,
    StructOut calledSub,
    StructOut callingSub,
    StructOut bprotocol,
    StructOut bc,
    StructOut llc,
    StructOut hlc,
    StructOut addInfo
  ){
    super(
       2
      +called.getLength()
      +calling.getLength()
      +calledSub.getLength()
      +callingSub.getLength()
      +bprotocol.getLength()
      +bc.getLength()
      +llc.getLength()
      +hlc.getLength()
      +addInfo.getLength(),
      appid,CAPI_CONNECT,CAPI_REQ,ctrl
    );
    writeWord(cip);			      //	compatibility Information Profile (CIP Value)
    writeStruct(called);
    writeStruct(calling);
    writeStruct(calledSub);
    writeStruct(callingSub);
    writeStruct(bprotocol);		//	B protocol to be used
    writeStruct(bc);			    //	Bearer Capability
    writeStruct(llc);			    //	Low Layer Compatibility
    writeStruct(hlc);			    //	High Layer Compatibility
    writeStruct(addInfo);			//	additional info
  }

  public ConnectReq(
    int appid, 
    int ctrl,
    int cip,
    String called,
    String calling,
    String calledSub,
    String callingSub,
    StructOut bprotocol,
    StructOut bc,
    StructOut llc,
    StructOut hlc,
    StructOut addInfo
  ){
    this(
      appid,
      ctrl,
      cip,
      (called.equals(""))?StructOut.empty:new CalledPartyNumber.Out(called),
      (calling.equals(""))?StructOut.empty:new CallingPartyNumber.Out(calling),
      (calledSub.equals(""))?StructOut.empty:new CalledPartySubAddress.Out(calledSub),
      (callingSub.equals(""))?StructOut.empty:new CallingPartySubAddress.Out(callingSub),
      bprotocol,
      bc,
      llc,
      hlc,
      addInfo
    );
    calledPartyNumber=called;
    callingPartyNumber=calling;
  }

  public ConnectReq(
    int appid, 
    int ctrl,
    int cip,
    String called,
    String calling,
    String calledSub,
    String callingSub,
    StructOut bprotocol
  ){
    this(
      appid,
      ctrl,
      cip,
      called,
      calling,
      calledSub,
      callingSub,
      bprotocol,
      StructOut.empty,
      StructOut.empty,
      StructOut.empty,
      StructOut.empty 
    );
  }

  public String getLocalNo(){ return callingPartyNumber;}
  public String getRemoteNo(){ return calledPartyNumber;}

  public String toString(){
    String s=super.toString()+"\n";
    s+="called  = "+calledPartyNumber+"\n";
    s+="calling = "+callingPartyNumber+"\n";
    return s;
  }
}

