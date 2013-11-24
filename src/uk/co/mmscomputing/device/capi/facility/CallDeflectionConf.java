package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class CallDeflectionConf extends SupServiceConf{

  public CallDeflectionConf(Rider r){
    super(r);
    info=r.readWord();                                // Supplementary Service Info
  }
}

