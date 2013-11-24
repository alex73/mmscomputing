package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.exception.CapiIOException;

public class CallForwardInd{

  static public class ActivateInd extends SupServiceInd{

    private int reason;

    public ActivateInd(Rider r){
      super(r);
      reason=r.readWord();                                  // Supplementary Service Info
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="Reason = "+CapiIOException.capiInfo2Str(reason);
      return s;
    }
  }

  static public class DeactivateInd extends SupServiceInd{

    private int reason;

    public DeactivateInd(Rider r){
      super(r);
      reason=r.readWord();                                  // Supplementary Service Info
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="Reason = "+CapiIOException.capiInfo2Str(reason);
      return s;
    }
  }

  static public class InterrogateParametersInd extends SupServiceInd{

    private int reason;

    public InterrogateParametersInd(Rider r){
      super(r);
      reason=r.readWord();                                  // Supplementary Service Info
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="Reason = "+CapiIOException.capiInfo2Str(reason);
      return s;
    }
  }

  static public class InterrogateNumbersInd extends SupServiceInd{

    private int reason;

    public InterrogateNumbersInd(Rider r){
      super(r);
      reason=r.readWord();                                  // Supplementary Service Info
    }

    public int getReason(){return reason;}

    public String toString(){
      String s=super.toString();
      s+="Reason = "+CapiIOException.capiInfo2Str(reason);
      return s;
    }
  }
}


