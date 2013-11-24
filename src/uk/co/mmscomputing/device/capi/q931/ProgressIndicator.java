package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class ProgressIndicator extends Q931{

//  ITU Recommendation Q.931 (03/93)  p80  4.5.23.
//  uk.co.mmscomputing.device.capi.plc.InfoInd

  public int codingStandard=0;
  public int location=0;
  public int progressDescription=0;

  public ProgressIndicator(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      codingStandard = getBits(o,7,6);
      location       = getBits(o,4,1);           
      if(codingStandard!=CCITT){throw new IllegalArgumentException();}
      o=r.read();                                     // octet 4
      progressDescription=getBits(o,7,1);
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public ProgressIndicator(byte[] data){
    this(new Rider(data));
  }

  public String toString(){
    String s="Progress Indicator - ";

    switch(location){
    case 0:  s+="\n    Location: User";break;
    case 1:  s+="\n    Location: Private network serving the local user";break;
    case 2:  s+="\n    Location: Public network serving the local user";break;
    case 3:  s+="\n    Location: Transit network";break;
    case 4:  s+="\n    Location: Public network serving the remote user";break;
    case 5:  s+="\n    Location: Private network serving the remote user";break;
    case 6:  s+="\n    Location: Network beyond interworking point";break;
    }
    switch(progressDescription){
    case 1:  s+="\n    Progress Description: Call is not end-to-end ISDN";break;
    case 2:  s+="\n    Progress Description: Destination address is non ISDN";break;
    case 3:  s+="\n    Progress Description: Origination address is non ISDN";break;
    case 4:  s+="\n    Progress Description: Call has returned to the ISDN";break;
    case 5:  s+="\n    Progress Description: Interworking has occurred and has resulted in a telecommunication service change.";break;
    case 8:  s+="\n    Progress Description: Inband information or appropriate pattern now available";break;
    }
    return s;
  }
}