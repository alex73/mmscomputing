package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class FacilityInd extends CapiIndMsg{

  protected int selector;

  public FacilityInd(Rider r){
    super(r);
    selector=r.readWord();
    r.structBegin();                                  // Facility Indication Parameter
  }

  public int getSelector(){return selector;}

  public String toString(){
    String s=super.toString();
    s+="selector = "+selector+"\n";
    return s;
  }

  static public MsgIn create(Rider r){
    int selector = r.readWord();
    r.structBegin();                                  // Facility Indication Parameter
    switch(selector){
    case CAPI_FACILITY_HANDSET:            return new HandsetInd(r);
    case CAPI_FACILITY_DTMF:               return new DTMFInd(r);
    case CAPI_FACILITY_V42bis:             return new V42InfoInd(r);
    case CAPI_FACILITY_SUPSERVICE:         return SupServiceInd.create(r);
    case CAPI_FACILITY_PWRMGMT:            return new PwrMgmtInd(r);
    case CAPI_FACILITY_INTERCONNECT:       return new InterconnectInd(r);
    case CAPI_FACILITY_ECHOCANCELLATION:   return EchoCancellerInd.create(r);
    }
    throw new IllegalArgumentException();
  }
}

