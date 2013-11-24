package uk.co.mmscomputing.device.capi.facility;

/*
  HOLD (Call Hold, ETS 300 139)
  TP (Terminal Portability, ETS 300 053)
  CF (Call Forwarding, ETS 300 199-201)
  CD (Call Deflection, ETS 300 202)
  ECT (Explicit Call Transfer, ETS 300 367)
  3PTY (Three-Party-Conference, ETS 300 186)
  MCID (Malicious Call Identification, ETS 300 128)
  CCBS (Completion of Calls to Busy Subscriber, ETS 300 359-1 excluding Section 10)
  MWI (Message Waiting Indication, ETS 300 650)
  CCNR (Completion of Calls on No Reply, ETS 301 065)
  CONF (Conference call, ETS 300 185-1)
*/

import javax.swing.tree.*;
import uk.co.mmscomputing.device.capi.*;

public class SupServiceConf extends FacilityConf{

  protected int function;

  public SupServiceConf(Rider r){
    super(r);
    function=r.readWord();                            // supplementary services function
    r.structBegin();                                  // supplementary service-specific parameter
  }

  static public MsgIn create(Rider r){
    int function=r.readWord();                        // supplementary services function
    r.structBegin();                                  // supplementary service-specific parameter

    switch(function){
    case CAPI_SERVICES_GETSUPPORTEDSERVICES:       return new SupServiceConf.GetSupportedServicesConf(r);
    case CAPI_SERVICES_LISTEN:                     return new SupServiceConf.ListenConf(r);
    case CAPI_SERVICES_HOLD:                       return new SupServiceConf.HoldConf(r);
    case CAPI_SERVICES_RETRIEVE:                   return new SupServiceConf.RetrieveConf(r);

    case CAPI_SERVICES_ECT:                        return new SupServiceConf.ECTConf(r);

    case CAPI_SERVICES_CF_ACTIVATE:              return new CallForwardConf.ActivateConf(r);
    case CAPI_SERVICES_CF_DEACTIVATE:            return new CallForwardConf.DeactivateConf(r);
    case CAPI_SERVICES_CF_INTERROGATEPARAMETERS: return new CallForwardConf.InterrogateParametersConf(r);
    case CAPI_SERVICES_CF_INTERROGATENUMBERS:    return new CallForwardConf.InterrogateNumbersConf(r);

    case CAPI_SERVICES_CD:                       return new CallDeflectionConf(r);
    }
    throw new IllegalArgumentException();
  }

  public int getFunctionNo(){ return function;}

  public String toString(){
    String s=super.toString();
    s+="function : "+function+"\n\t";
    return s;
  }

  static public class GetSupportedServicesConf extends SupServiceConf{

    static final private String[] serviceStrings={
      "Hold/Retrieve",
      "Terminal Portability",
      "Explicit Call Transfer",
      "Three-Party-Conference",
      "Call-Forwarding",
      "Call-Deflection",
      "Malicious Call Identification",
      "Completion of Calls to Busy Subscriber",
      "Message Waiting Indication",
      "Completion of Calls on No Reply",
      "Conference call"
    };

    private int services;

    public GetSupportedServicesConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();                              // success
        services=r.readDWord();
      }
    }

    public int getSupportedServices(){                return services;}

    public boolean isSupported(int bit){              return (services&(1<<bit))!=0;}

    public boolean isHoldRetrieveSupported(){         return isSupported(0);}    // (includes functions 0x0002, 0x0003, 0x8000, 0x8001)
    public boolean isTerminalPortabilitySupported(){  return isSupported(1);}    // (includes functions 0x0004, 0x0005, 0x8002, 0x8003)
    public boolean isECTSupported(){                  return isSupported(2);}    // (includes functions 0x0006, 0x8009, 0x800A)
    public boolean is3PTYSupported(){                 return isSupported(3);}    // (includes functions 0x0007, 0x0008, 0x800B, 0x800C)
    public boolean isCallForwardingSupported(){       return isSupported(4);}    // (includes functions 0x0009...0x000C, 0x8004... 0x8008)
    public boolean isCallDeflectionSupported(){       return isSupported(5);}    // (includes function  0x000D)
    public boolean isMCIDSupported(){                 return isSupported(6);}    // (includes function  0x000E)
    public boolean isCCBSSupported(){                 return isSupported(7);}    // (includes functions 0x000F...0x0012, 0x800D... 0x8013)
    public boolean isMWISupported(){                  return isSupported(8);}    // (includes functions 0x0013, 0x0014, 0x8014)
    public boolean isCCNRSupported(){                 return isSupported(9);}    // (includes functions 0x0015, 0x0016, 0x8015)
    public boolean isCONFSupported(){                 return isSupported(10);}   // (includes functions 0x0017...0x001C, 0x8016, 0x8017)

    public String toString(){
      String s=super.toString();
      s+="Supported Services = 0"+Integer.toBinaryString(services)+"b\n";
      for(int i=0;i<serviceStrings.length;i++){
        if(isSupported(i)){
          s+="Bit["+i+"] - "+serviceStrings[i]+"\n";
        }
      }
      return s;
    }

    public DefaultMutableTreeNode toTree(){
      DefaultMutableTreeNode category = new DefaultMutableTreeNode("Supported Services = 0"+Integer.toBinaryString(services)+"b");

      for(int i=0;i<serviceStrings.length;i++){
        if(isSupported(i)){ 
          category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+serviceStrings[i]));
        }
      }
      return category;
    }
  }

  static public class ListenConf extends SupServiceConf{

    public ListenConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();                                       // Supplementary Service Info
      }
    }
  }

  static public class HoldConf extends SupServiceConf{
    public HoldConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();                                       // Supplementary Service Info
      }
    }
  }

  static public class RetrieveConf extends SupServiceConf{
    public RetrieveConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();                                       // Supplementary Service Info
      }
    }
  }

  static public class ECTConf extends SupServiceConf{
    public ECTConf(Rider r){
      super(r);
      if(info==0){
        info=r.readWord();                                       // Supplementary Service Info
      }
    }
  }

}

