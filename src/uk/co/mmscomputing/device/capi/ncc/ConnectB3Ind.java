package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.parameter.NCPI;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;

public class ConnectB3Ind extends CapiIndMsg{

  private byte[] ncpi;

  public ConnectB3Ind(Rider r){
    super(r);
    try{
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

  public String toString(){
    String s=super.toString();
    return s;
  }
}

