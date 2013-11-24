package uk.co.mmscomputing.device.capi.man.eicon;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class EiconReq extends ManufacturerReq implements ManufacturerConstants,EiconConstants{

  public EiconReq(int appid, int cntl,int cmd,StructOut data){
    super(
        2+data.getLength(),
        appid,
        cntl,
        CAPI_MANUID_EICON                 // manufacturer id:  ""
    );
    writeWord(cmd);
    writeStruct(data);
  }
}