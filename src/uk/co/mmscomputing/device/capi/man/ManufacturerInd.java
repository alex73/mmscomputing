package uk.co.mmscomputing.device.capi.man;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.avm.*;
import uk.co.mmscomputing.device.capi.man.eicon.*;

public class ManufacturerInd extends CapiIndMsg implements ManufacturerConstants{

  public int    manuid;

  public ManufacturerInd(Rider r){
    super(r);
    manuid  = r.readDWord();           // unique manufacturer id
  }

  public int getManufacturerId(){ return manuid;}

  public String toString(){
    String s=super.toString();
    s+="manu  id : 0x"+Integer.toHexString(manuid)+" "
     +(char)((manuid)&0x00FF)
     +(char)((manuid>>8)&0x00FF)
     +(char)((manuid>>16)&0x00FF)
     +(char)((manuid>>24)&0x00FF)
     +"\n"
    ;
    return s;
  }

  static public MsgIn create(Rider r){
    int manuid  = r.readDWord();       // unique manufacturer id
    switch(manuid){
    case CAPI_MANUID_AVM:      return AVMInd.create(r);
    }
    System.err.println("uk.co.mmscomputing.device.capi.man.create:\n\tUnknown ManufacturerInd Message.\n\t"+r.toString());
    return new ManufacturerInd(r);
  }
}

