package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ConnectB3Conf extends CapiConfMsg{
  public ConnectB3Conf(Rider r){
    super(r);
    try{
      info=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

