package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DataB3Resp extends MsgOut{

  public DataB3Resp(int appid, int ncci, int datahandle){
    super(2,appid,CAPI_DATA_B3,CAPI_RESP,ncci);
    writeWord(datahandle);
  }

  public DataB3Resp(){
    super(2);
  }

  public void encode(int appid, int ncci, int datahandle){
    super.encode(2,appid,CAPI_DATA_B3,CAPI_RESP,ncci);
    writeWord(datahandle);
  }
}