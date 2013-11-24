package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class DTMFInd extends FacilityInd{

  // FAX : X [cng=1.1kHz] Y [ced=2.1khz]
  // 0..9 * # A..D;

  private String digits;

  public DTMFInd(Rider r){
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

