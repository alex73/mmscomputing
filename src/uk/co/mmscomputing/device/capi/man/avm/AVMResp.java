package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class AVMResp extends ManufacturerResp implements AVMConstants{

  public AVMResp(int appid,int cntl,int classid,int funcid,StructOut data){
    super(
        8+data.getLength(),
        appid,
        cntl,
        CAPI_MANUID_AVM                   // manufacturer id:  "AVM!"     
    );
    writeDWord(classid);
    writeDWord(funcid);
    writeStruct(data);
  }
}
