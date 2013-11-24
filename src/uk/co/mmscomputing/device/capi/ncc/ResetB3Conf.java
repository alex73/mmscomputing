package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class ResetB3Conf extends CapiConfMsg{
  public ResetB3Conf(Rider r){
    super(r);
    try{
      info  = r.readWord();       
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }
}

