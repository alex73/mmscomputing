package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class HandsetInd extends FacilityInd{

  // + handset off-hook; - handset on-hook
  // 0..9 * # A..D;

  private String digits;

  public HandsetInd(Rider r){
    super(r);
    digits=r.readString();
  }

  public String getDigits(){return digits;}

  public String toString(){
    String s=super.toString();
    s+="digits : "+digits+"\n\t";
    return s;
  }
}

