package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class AVMReq extends ManufacturerReq implements ManufacturerConstants,AVMConstants{

  public AVMReq(int appid,int cntl,int classid,int funcid,StructOut data){
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

  public String toString(){
    String s=super.toString();
    return s;
  }
}
