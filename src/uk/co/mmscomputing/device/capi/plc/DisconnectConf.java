package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class DisconnectConf extends CapiConfMsg{
  public DisconnectConf(Rider r){
    super(r);
    try{
      info=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

