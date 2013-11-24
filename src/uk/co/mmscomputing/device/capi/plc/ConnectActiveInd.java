package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.CallingPartyNumber;
import uk.co.mmscomputing.device.capi.q931.CallingPartySubAddress;
import uk.co.mmscomputing.device.capi.q931.LowLayerCompatibility;

public class ConnectActiveInd extends CapiIndMsg{

  private String                connectedNumber;
  private String                connectedSubAddress;
  private LowLayerCompatibility llc=null;

  public ConnectActiveInd(Rider r){
    super(r);
    try{
      connectedNumber=new CallingPartyNumber(r).getNumber();
      connectedSubAddress=new CallingPartySubAddress(r).getNumber();
      llc=new LowLayerCompatibility(r);
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public String toString(){
    String s=super.toString();
    s+="\nConnected Number     : "+connectedNumber+"\n\t";
    s+="\nConnected Sub Number : "+connectedSubAddress+"\n\t";
    s+="\n"+llc.toString()+"\n\t";
    return s;
  }
}

