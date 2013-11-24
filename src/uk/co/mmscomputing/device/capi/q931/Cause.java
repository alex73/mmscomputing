package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class Cause extends Q931{

//  ITU Recommendation Q.931 (03/93)  p84  4.5.12.
//  ITU Recommendation Q.850 (03/93)
//  uk.co.mmscomputing.device.capi.plc.InfoInd

  public int codingStandard=0;
  public int location=0;
  public int recommendation=0;
  public int cause=0;

  public Cause(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      codingStandard = getBits(o,7,6);
      if(codingStandard!=CCITT){throw new IllegalArgumentException();}
      location       = getBits(o,4,1);           
      if(isExt(o)){
        recommendation=r.read();                      // octet 3*
      }
      o=r.read();                                     // octet 4
      cause=getBits(o,7,1);
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public Cause(byte[] data){                          // decode
    this(new Rider(data));
  }

  public String toString(){
    String s="Cause - ";

    switch(location){
    case 0:  s+="\n    Location: User";break;
    case 1:  s+="\n    Location: Private network serving the local user";break;
    case 2:  s+="\n    Location: Public network serving the local user";break;
    case 3:  s+="\n    Location: Transit network";break;
    case 4:  s+="\n    Location: Public network serving the remote user";break;
    case 5:  s+="\n    Location: Private network serving the remote user";break;
    case 6:  s+="\n    Location: Network beyond interworking point";break;
    }
/*
    http://www.cisco.com/warp/public/129/isdn_disc_code.html
    last accessed 2004-04-09
*/
    switch(cause){
    case 1:  s+="\n    unallocated (unassigned) number";break;
    case 2:  s+="\n    no route to specifies transit network";break;
    case 3:  s+="\n    no route to destination";break;
    case 6:  s+="\n    channel unacceptable";break;
    case 7:  s+="\n    call awarded and being delivered in an established channel";break;
    case 16: s+="\n    normal call clearing";break;
    case 17: s+="\n    user busy";break;
    case 18: s+="\n    no user responding";break;
    case 19: s+="\n    no answer from user (user alerted)";break;

    case 22: s+="\n    number changed";break;
    case 26: s+="\n    non-selected user clearing";break;
    case 27: s+="\n    destination out of order";break;
    case 28: s+="\n    invalid format (address incomplete)";break;
    case 29: s+="\n    facility rejected";break;
    case 30: s+="\n    response to status enquiry";break;
    case 31: s+="\n    normal, unspecified";break;
    case 34: s+="\n    no circuit/channel available";break;
    case 38: s+="\n    network out of order";break;
    case 41: s+="\n    temporary failure";break;
    case 42: s+="\n    switching equipment congestion";break;
    case 43: s+="\n    access information discarded";break;
    case 44: s+="\n    requested circuit/channel not available";break;
    case 47: s+="\n    resource unavailable, unspecified";break;

    case 49: s+="\n    quality of service not available";break;
    case 50: s+="\n    requested facility not subscribed";break;
    case 57: s+="\n    bearer capability not authorised";break;
    case 58: s+="\n    bearer capability not presently available";break;
    case 63: s+="\n    service or option not available unspecified";break;

    case 65: s+="\n    bearer capability not implemented";break;
    case 66: s+="\n    channel type not implemented";break;
    case 69: s+="\n    requested facility not implemented";break;
    case 70: s+="\n    only restricted digital information bearer capability is available";break;
    case 79: s+="\n    service or option not implemented, unspecified";break;

    case 81: s+="\n    invalid call reference value";break;
    case 82: s+="\n    identified channel does not exist";break;
    case 83: s+="\n    a suspended call exists, but this call identity does not";break;

    case 84: s+="\n    call identity in use";break;
    case 85: s+="\n    no call suspended";break;
    case 86: s+="\n    call having the requested call identity has been cleared";break;
    case 88: s+="\n    incompatible destination";break;
    case 91: s+="\n    invalid transit network selection";break;
    case 95: s+="\n    invalid message, unspecified";break;

    case 96: s+="\n    mandatory information element is missing";break;
    case 97: s+="\n    message type non-existent or not implemented";break;
    case 98: s+="\n    message not compatible with call state or messages TYPE NON-EXISTENT OR NOT IMPLEMENTED";break;
    case 99: s+="\n    information element non-existent or not implemented";break;
    case 100:s+="\n    invalid information element contents";break;
    case 101:s+="\n    message not compatible with call start";break;
    case 102:s+="\n    recovery on timer expiry";break;
    case 111:s+="\n    protocol error, unspecified";break;

    case 127:s+="\n    interworking, unspecified";  break;
    }
    return s;
  }
}