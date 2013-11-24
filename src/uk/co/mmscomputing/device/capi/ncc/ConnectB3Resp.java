package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectB3Resp extends MsgOut{

  public ConnectB3Resp(int appid, int ncci, int reject, StructOut ncpi){
    super(2+ncpi.getLength(),appid,CAPI_CONNECT_B3,CAPI_RESP,ncci);
    writeWord(reject);			//	0= accept, 1..n=reject
    writeStruct(ncpi);		  //	ncpi struct
  }

  public ConnectB3Resp(int appid, int ncci, int reject){
    super(3,appid,CAPI_CONNECT_B3,CAPI_RESP,ncci);
    writeWord(reject);			//	0= accept, 1..n=reject
    writeStruct();		      //	ncpi struct
  }

  public ConnectB3Resp(int appid, int ncci){
    this(appid,ncci,ACCEPT);
  }
}