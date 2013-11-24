package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class LowLayerCompatibility extends Q931{

//  ITU Recommendation Q.931 (03/93)  p84  4.5.19.
//  max. length of bytes 18

//  uk.co.mmscomputing.device.capi.plc.ConnectActiveInd
//  uk.co.mmscomputing.device.capi.plc.ConnectInd

  public int codingStandard            =-1;
  public int infoTransferCap           =-1;    // 0x00 = Speech; 0x10 = 3.1.kHz audio

  public LowLayerCompatibility(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      codingStandard=getBits(o,7,6);
      if(codingStandard!=CCITT){throw new IllegalArgumentException();}
      infoTransferCap=getBits(o,5,1);           

// lots more

    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public LowLayerCompatibility(byte[] data){
    this(new Rider(data));
  }

  public String toString(){
    String s="Low Layer compatibility - ";
    return s;
  }
}
