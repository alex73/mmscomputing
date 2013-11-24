package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class InterconnectInd extends FacilityInd{

  public static final int ConnectActive=1;
  public static final int Disconnect   =2;

  protected int function;
  protected int participant;
  protected int serviceReason;

  public InterconnectInd(Rider r){
    super(r);
    function=r.readWord();
    r.structBegin();                         // Line Interconnect Identication parameter
    participant=r.readDWord();
    serviceReason=(function==Disconnect)?r.readWord():-1;
  }

  public String toString(){
    String s=super.toString();
    s+="function : "+((function==ConnectActive)?"ConnectActive":"Disconnect")+"\n\t";
    s+="participant : 0x"+Integer.toHexString(participant)+"\n\t";
    if(serviceReason!=-1){
      switch(serviceReason){
      case 0x0000: s+="User Initiated\n\t";break;
      case 0x3800: s+="PLCI has no B-Channel\n\t";break;
      case 0x3801: s+="Lines not compatible\n\t";break;
      case 0x3802: s+="PLCI(s) is(are) not in any or not in the same interconnection.\n\t";break;
      default:     s+="Unknown service reason\n\t";break;
      }
    }
    return s;
  }
}

