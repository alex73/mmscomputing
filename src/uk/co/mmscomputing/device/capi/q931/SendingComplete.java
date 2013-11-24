package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class SendingComplete extends Q931{

//  ITU Recommendation Q.931 (03/93)  p113  4.5.27.
//  optionally indicate completion of called part number

  public SendingComplete(Rider r){
    r.setLength();
    try{
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
  }
}
