package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class AlertConf extends CapiConfMsg{
  public AlertConf(Rider r){
    super(r);
    try{
      info=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

