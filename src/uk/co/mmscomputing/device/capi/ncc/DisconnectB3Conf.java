package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DisconnectB3Conf extends CapiConfMsg{
  public DisconnectB3Conf(Rider r){
    super(r);
    try{
      info  = r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

