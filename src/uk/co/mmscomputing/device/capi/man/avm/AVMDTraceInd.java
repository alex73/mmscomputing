package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;

public class AVMDTraceInd extends AVMInd{

  private byte[] buf;

  public AVMDTraceInd(Rider r){
    super(r);
    r.structBegin();               // 8: manufacturer specific data
    buf=r.getBytes();
    r.structEnd();
  }

  public byte[] getBytes(){return buf;}

}

