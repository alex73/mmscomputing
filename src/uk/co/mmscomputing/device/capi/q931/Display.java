package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class Display extends Q931{

//  ITU Recommendation Q.931 (03/93)  p89  4.5.16.

  public String   digits="";                          // IA5

  public Display(Rider r){
    r.setLength();
    try{
      digits=r.readString();                          // Octet 3
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
  }

  public String toString(){
    String s="Display - \n";
    s+=" Digits = "+digits+"\n";
    return s;
  }

}
