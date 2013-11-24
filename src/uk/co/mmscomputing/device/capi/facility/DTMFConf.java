package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class DTMFConf extends FacilityConf{

  private int dtmfinfo;

  public DTMFConf(Rider r){
    super(r);
    if(info!=0){return;}
    dtmfinfo=r.readWord();
  }

  public int getInfo(){return dtmfinfo;}

  public String getInformation(){
    switch(dtmfinfo){
    case 0: return "Sending of DTMF info successfully initiated.";
    case 1: return "Incorrect DTMF digit.";
    case 2: return "Unknown DTMF request.";
    }
    return "Unknown info value!!!";
  }

  public String toString(){
    String s=super.toString();
    s+="dtmfinfo = "+dtmfinfo+" - "+getInformation()+"\n";
    return s;
  }

}

