package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.exception.CapiIOException;

public class SupServiceInd extends FacilityInd{

  protected int function;

  public SupServiceInd(Rider r){
    super(r);
    function=r.readWord();                            // supplementary services function
    r.structBegin();                                  // supplementary service-specific parameter
  }

  static public MsgIn create(Rider r){
    int function = r.readWord();                      // supplementary services function
    r.structBegin();                                  // supplementary service-specific parameter
    switch(function){

//    case CAPI_SERVICES_GETSUPPORTEDSERVICES:  n/a
//    case CAPI_SERVICES_LISTEN:                n/a

    case CAPI_SERVICES_HOLD:                     return new SupServiceInd.HoldInd(r);
    case CAPI_SERVICES_RETRIEVE:                 return new SupServiceInd.RetrieveInd(r);

    case CAPI_SERVICES_ECT:                      return new SupServiceInd.ECTInd(r);

    case CAPI_SERVICES_CF_ACTIVATE:              return new CallForwardInd.ActivateInd(r);
    case CAPI_SERVICES_CF_DEACTIVATE:            return new CallForwardInd.DeactivateInd(r);
    case CAPI_SERVICES_CF_INTERROGATEPARAMETERS: return new CallForwardInd.InterrogateParametersInd(r);
    case CAPI_SERVICES_CF_INTERROGATENUMBERS:    return new CallForwardInd.InterrogateNumbersInd(r);

    case CAPI_SERVICES_CD:                       return new CallDeflectionInd(r);
    }
    throw new IllegalArgumentException();
  }

  static public class HoldInd extends SupServiceInd{

    private int reason;

    public HoldInd(Rider r){
      super(r);reason=r.readWord();                                // Supplementary Service Reason
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="reason   = 0x"+Integer.toHexString(reason)+" - "+CapiIOException.capiInfo2Str(reason)+"\n";
      return s;
    }

  }

  static public class RetrieveInd extends SupServiceInd{

    private int reason;

    public RetrieveInd(Rider r){
      super(r);reason=r.readWord();                                // Supplementary Service Reason
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="reason   = 0x"+Integer.toHexString(reason)+" - "+CapiIOException.capiInfo2Str(reason)+"\n";
      return s;
    }
  }

  static public class ECTInd extends SupServiceInd{

    private int reason;

    public ECTInd(Rider r){
      super(r);reason=r.readWord();                                // Supplementary Service Reason
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="reason   = 0x"+Integer.toHexString(reason)+" - "+CapiIOException.capiInfo2Str(reason)+"\n";
      return s;
    }
  }

}

