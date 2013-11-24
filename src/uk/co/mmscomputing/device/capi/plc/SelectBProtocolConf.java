package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class SelectBProtocolConf extends CapiConfMsg{
  public SelectBProtocolConf(Rider r){
    super(r);
    try{
      info=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

