package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectConf extends CapiConfMsg{
  public ConnectConf(Rider r){
    super(r);
    try{
      info=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

