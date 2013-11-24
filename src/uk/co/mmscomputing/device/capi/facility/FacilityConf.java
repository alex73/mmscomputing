package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class FacilityConf extends CapiConfMsg{

  private int    selector;

  public FacilityConf(Rider r){
    super(r);
    info    =r.readWord();       
    selector=r.readWord();
    r.structBegin();                                  // Facility Confirmation Parameter
  }

  public String toString(){
    String s=super.toString();
    s+="selector = "+selector+"\n";
    return s;
  }

  static public MsgIn create(Rider r){
    int info    =r.readWord();       
    int selector=r.readWord();
    r.structBegin();                                  // Facility Confirmation Parameter
    switch(selector){
    case CAPI_FACILITY_HANDSET:            return new HandsetConf(r);
    case CAPI_FACILITY_DTMF:               return new DTMFConf(r);
    case CAPI_FACILITY_V42bis:             return new V42InfoConf(r);
    case CAPI_FACILITY_SUPSERVICE:         return SupServiceConf.create(r);
    case CAPI_FACILITY_PWRMGMT:            return new PwrMgmtConf(r);
    case CAPI_FACILITY_INTERCONNECT:       return new InterconnectConf(r);
//    case CAPI_FACILITY_BROADBAND:    
//    case CAPI_FACILITY_CONTROLLEREVENTS:
    case CAPI_FACILITY_ECHOCANCELLATION:   return EchoCancellerConf.create(r);
    }
    throw new IllegalArgumentException();
  }
}

