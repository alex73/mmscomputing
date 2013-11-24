package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.NCPI;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;

public class DisconnectB3Ind extends CapiIndMsg{

  private int    reasonB3 = 0;                // capi part 1 p.123
  private byte[] ncpi;

  public DisconnectB3Ind(Rider r){
    super(r);
    try{
      reasonB3=r.readWord();
      r.structBegin();                         // ncpi struct
      ncpi=r.getBytes();                       //	network control protocol information,protocol dependent
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public NCPI getNCPI(BProtocol protocol){   // Network Control Protocol Information
    if(protocol==null){return null;}
    return NCPI.create(protocol.B3Protocol,new Rider(ncpi));
  }

  public int getReason(){return reasonB3;}

  public String toString(){
    String s=super.toString();
    s+="reasonB3 = 0x"+Integer.toHexString(reasonB3)+"\n\t";
    return s;
  }
}


