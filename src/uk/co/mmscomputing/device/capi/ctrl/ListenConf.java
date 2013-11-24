package uk.co.mmscomputing.device.capi.ctrl;

import uk.co.mmscomputing.device.capi.Rider;
import uk.co.mmscomputing.device.capi.CapiConfMsg;

public class ListenConf extends CapiConfMsg{
  public ListenConf(Rider r){
    super(r);
    info=r.readWord();
  }
}

