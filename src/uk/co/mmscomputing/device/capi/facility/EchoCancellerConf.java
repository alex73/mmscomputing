package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

// Eicon SDK: eicon/sdk/Doc/CxEcho.pdf

public class EchoCancellerConf extends FacilityConf{

  protected int function;

  public EchoCancellerConf(Rider r){
    super(r);
    function=r.readWord();                              // function
    r.structBegin();                                    // function-specific parameter
  }

  static public MsgIn create(Rider r){
    int function=r.readWord();                          // function
    r.structBegin();                                    // function-specific parameter
    switch(function){
    case 0:  return new GetSupportedServicesConf(r);
    case 1:  return new EnableConf(r);
    case 2:  return new DisableConf(r);
    }
    return new EchoCancellerConf(r);
  }

  public int getFunctionNo(){ return function;}

  public String toString(){
    String s=super.toString();
    s+="function : "+function+"\n\t";
    return s;
  }

  static public class GetSupportedServicesConf extends EchoCancellerConf{

    private int options;
    private int maxTailLength,maxPreDelay;   // in [ms]

    public GetSupportedServicesConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();
        if(info==0){
          options       = r.readWord();
          maxTailLength = r.readWord();
          maxPreDelay   = r.readWord();
        }
      }
    }

    public int getOptions(){return options;}
    public int getMaxTailLength(){return maxTailLength;}
    public int getMaxPreDelay(){return maxPreDelay;}

    public String toString(){
      String s=super.toString();
      s+="options          : 0"+Integer.toBinaryString(options)+"b\n\t";
      s+="maxTailLength    : "+maxTailLength+"\n\t";
      s+="maxPreDelay      : "+maxPreDelay+"\n\t";
      return s;
    }
  }

  static public class EnableConf extends EchoCancellerConf{

    public EnableConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();
      }
    }
  }

  static public class DisableConf extends EchoCancellerConf{

    public DisableConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();
      }
    }
  }
}