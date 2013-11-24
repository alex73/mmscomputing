package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class Q931Date extends Q931{

//  ITU Recommendation Q.931 (03/93)  p88  4.5.15.

  public String   date="";

  public Q931Date(Rider r){
    r.setLength();
    try{
      int y=r.read();                          // Octet 3
      date += " "+y;
      int m=r.read();                          // Octet 4
      date += "-"+m;
      int d=r.read();                          // Octet 5
      date += "-"+d;
      int h=r.read();                          // Octet 6
      date += " "+h;
      int min=r.read();                          // Octet 7
      date += ":"+min;
      int s=r.read();                          // Octet 8
      date += ":"+s;
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
  }

  public String toString(){
    String s="Date - \n";
    s+=" Date = "+date+"\n";
    return s;
  }

}
