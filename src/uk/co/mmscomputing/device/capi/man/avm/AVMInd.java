package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class AVMInd extends ManufacturerInd implements AVMConstants{

  public int classid,funcid;

  public AVMInd(Rider r){
    super(r);
    classid = r.readDWord();       // 0: class
    funcid  = r.readDWord();       // 4: function
  }

  public int getClassId(){    return classid;}
  public int getFunctionId(){ return funcid;}

  public String toString(){
    String s=super.toString();
    s+="class id : 0x"+Integer.toHexString(classid)+"\n";
    s+="func  id : "+funcid+"\n";
    return s;
  }

  static public MsgIn create(Rider r){
    int classid = r.readDWord();         // 0: class
    if(classid==0){
      int funcid  = r.readDWord();       // 4: function
      switch(funcid){
      case CAPI_AVM_DTRACE: return new AVMDTraceInd(r);
      }
    }
    return new AVMInd(r);
  }
}
