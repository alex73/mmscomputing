package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.exception.CapiIOException;

public class DisconnectInd extends CapiIndMsg{

  private int errno=0;

  public DisconnectInd(Rider r){
    super(r);
    try{
      errno=r.readWord();
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public int getErrNo(){return errno;}

  public String toString(){
    String s=super.toString();
    s+="info     = 0x"+Integer.toHexString(errno)+" - "+CapiIOException.capiInfo2Str(errno)+"\n";
    return s;
  }
}

