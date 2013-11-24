package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

// Eicon SDK: eicon/sdk/Doc/CxEcho.pdf

public class EchoCancellerInd extends FacilityInd{

  protected int function;

  public EchoCancellerInd(Rider r){
    super(r);
    function=r.readWord();                              // function
    r.structBegin();                                    // function-specific parameter
  }

  static public MsgIn create(Rider r){
    int function=r.readWord();                          // function
    r.structBegin();                                    // function-specific parameter
    switch(function){
    case 1:  return new BypassInd(r);
    }
    return new EchoCancellerInd(r);
  }

  public int getFunctionNo(){ return function;}

  public String toString(){
    String s=super.toString();
    s+="function : "+function+"\n\t";
    return s;
  }

  static public class BypassInd extends EchoCancellerConf{

    private int bypassevent;

    public BypassInd(Rider r){
      super(r);
      bypassevent=r.readWord();
    }

    public String toString(){
      String s=super.toString();
      switch(bypassevent){
      case 1: s+="echo canceller bypass due to continuous 2100Hz\n\t"; break;
      case 2: s+="echo canceller bypass due to phase reversed 2100Hz\n\t"; break;
      case 3: s+="echo canceller bypass released\n\t"; break;
      }
      return s;
    }
  }
}