package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class PwrMgmtConf extends FacilityConf{

  protected int awakeReqs;

  public PwrMgmtConf(Rider r){
    super(r);
    if(info!=0){return;}
    awakeReqs=r.readWord();
  }

  public String toString(){
    String s=super.toString();
    s+="awakeReqs : "+awakeReqs+"\n\t";
    return s;
  }
}

