package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class ChannelIdentification extends Q931{

//  ITU Recommendation Q.931 (03/93)  p84  4.5.13.
//  uk.co.mmscomputing.device.capi.plc.InfoInd

  public int intId=0;
  public int intType=0;
  public int prefExcl=0;
  public int dChannelInd=0;
  public int channel=0;

  public ChannelIdentification(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      intId       = getBits(o,7,7);
      intType     = getBits(o,6,6);
      prefExcl    = getBits(o,4,4);
      dChannelInd = getBits(o,3,3);
      channel     = getBits(o,2,1);
      if(intId!=0){                                   // Octet 3.1
        do{
          o=r.read();
        }while(isExt(o));
        o=r.read();
      }
      if(intType!=0){                                 // Octet 3.2/3.3
      }
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public ChannelIdentification(byte[] data){          // decode
    this(new Rider(data));
  }

  public String toString(){
    String s="Channel identification - \n";
    if(dChannelInd!=0){
      s+=" D-Channel\n";
    }else if(intType==0){                             // Basic Interface
      switch(channel){
      case 0: s+=" BRI : No Channel\n";break;
      case 1: s+=" BRI : B1 Channel\n";break;
      case 2: s+=" BRI : B2 Channel\n";break;
      case 3: s+=" BRI : B1/B2 Channel\n";break;
      }
    }else{                                            // other interface i.e. primary
      switch(channel){
      case 0: s+=" PRI : No Channel\n";break;
      case 1: s+=" PRI : As indicated\n";break;
      case 2: s+=" PRI : Reserved\n";break;
      case 3: s+=" PRI : Any Channel\n";break;
      }
    }
    return s;
  }

}