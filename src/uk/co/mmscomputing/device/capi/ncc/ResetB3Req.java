package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ResetB3Req extends MsgOut{

  public ResetB3Req(int appid, int ncci, StructOut ncpi){
    super(ncpi.getLength(),appid,CAPI_RESET_B3,CAPI_REQ,ncci);
    writeStruct(ncpi);		//	network control protocol information
  }

  public ResetB3Req(int appid, int ncci){
    super(1,appid,CAPI_RESET_B3,CAPI_REQ,ncci);
    writeStruct();	    	//	network control protocol information
  }
}