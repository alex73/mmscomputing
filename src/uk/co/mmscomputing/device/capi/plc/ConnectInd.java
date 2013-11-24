package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.*;
import uk.co.mmscomputing.device.capi.parameter.*;

public class ConnectInd extends CapiIndMsg{

  private int                    cip=0;
  private String                 calledPartyNumber=null;
  private String                 callingPartyNumber=null;
  private BearerCapability       bc=null;
  private LowLayerCompatibility  llc=null;
  private HighLayerCompatibility hlc=null;
  private AdditionalInfo.In      addinfo=null;

  public ConnectInd(Rider r){
    super(r);
    try{
      cip=r.readWord();
      calledPartyNumber   = new CalledPartyNumber(r).getNumber();
      callingPartyNumber  = new CallingPartyNumber(r).getNumber();
      calledPartyNumber  += new CalledPartySubAddress(r).getNumber();
      callingPartyNumber += new CallingPartySubAddress(r).getNumber();
      bc=new BearerCapability(r);
      llc=new LowLayerCompatibility(r);
      hlc=new HighLayerCompatibility(r);
      addinfo=new AdditionalInfo.In(r);
// second calling party number
//    System.out.println(addinfo.toString());
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public int getCIP(){ return cip; }
  public BearerCapability getBearerCapability(){ return bc; }
  public String getLocalNo(){ return calledPartyNumber; }
  public String getRemoteNo(){ return callingPartyNumber; }

  public String toString(){
    String s=super.toString();
    s+="cip                = "+cip+"\n";
    s+="calledPartyNumber  = "+calledPartyNumber+"\n";
    s+="callingPartyNumber = "+callingPartyNumber+"\n";
    s+="bearer capability  = "+bc.toString()+"\n";
    s+="llc                = "+llc.toString()+"\n";
    s+="hlc                = "+hlc.toString()+"\n";
    s+="additional info    = "+addinfo.toString()+"\n";
    return s;
  }
}

