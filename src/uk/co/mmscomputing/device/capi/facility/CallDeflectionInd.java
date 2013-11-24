package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.exception.CapiIOException;

public class CallDeflectionInd extends SupServiceInd{

  private int reason;

  public CallDeflectionInd(Rider r){
    super(r);
    reason=r.readWord();                                  // Supplementary Service Reason
  }

  public int getReason(){return reason;}

  public String toString(){
    String s=super.toString();
    s+="Reason = "+CapiIOException.capiInfo2Str(reason);
    return s;
  }
  
}

