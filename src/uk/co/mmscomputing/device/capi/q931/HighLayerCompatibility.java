package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class HighLayerCompatibility extends Q931{

//  ITU Recommendation Q.931 (03/93)  p90  4.5.17.
//  max. length of bytes 5

//  uk.co.mmscomputing.device.capi.plc.ConnectActiveInd
//  uk.co.mmscomputing.device.capi.plc.ConnectInd

  public int codingStandard            =-1;
  public int interpretation            =-1;           // 0x04
  public int presentation              =-1;
  public int characteristics           =-1;

  public HighLayerCompatibility(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      codingStandard =getBits(o,7,6);                  
      if(codingStandard!=CCITT){throw new IllegalArgumentException();}
      interpretation =getBits(o,5,3);           
      presentation   =getBits(o,2,1);           
      o=r.read();                                     // octet 4
      characteristics=getBits(o,7,1);           
      if(isExt(o)){
        o=r.read();                                   // octet 4a
        characteristics=getBits(o,7,1);           
      }
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public HighLayerCompatibility(byte[] data){               // decode
    this(new Rider(data));
  }

  public String toString(){
    String s="High Layer compatibility - ";
    switch(characteristics){
    case -1:    break;
    case 0x01:  s+="\n    Characteristics: Telephony";break;
    case 0x04:  s+="\n    Characteristics: Facsimile Group 2/3";break;
    case 0x21:  s+="\n    Characteristics: Facsimile Group 4 Class I";break;
    case 0x24:  s+="\n    Characteristics: Teletex service, Group 4 Classes II and III";break;
    default:    s+="\n    Characteristics: ["+Integer.toHexString(characteristics)+"]";
    }
    return s;
  }
}
