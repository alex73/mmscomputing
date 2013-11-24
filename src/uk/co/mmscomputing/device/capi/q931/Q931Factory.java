package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

// ETSI 300 102, Q.931

public class Q931Factory implements Q931Constants{

  static public Q931 create(Rider r){
    return create(r.read(),r);                       // Information element identifier Q.931/Table 4-3 p59
  }

  static public Q931 create(int iei,Rider r){
    switch(iei){
//      case 0x0000:  return new SegmentedMessage(r);
      case 0x0004:  return new BearerCapability(r);
      case 0x0008:  return new Cause(r);
//      case 0x0010: return new CallIdentity(r);
//      case 0x0014: return new CallState(r);
      case 0x0018:  return new ChannelIdentification(r);
//      case 0x001C: return new Facility(r);
      case 0x001E:  return new ProgressIndicator(r);
//      case 0x0020: return new NetworkSpecificFacilities(r);
//      case 0x0027: return new NotificationIndicator(r);
      case 0x0028:  return new Display(r);          
      case 0x0029: return new Q931Date(r);
      case 0x002C: return new KeypadFacility(r);
//      case 0x0034: return new Signal(r);
//      case 0x0036: return new Switchhook(r);
//      case 0x0038: return new FeatureActivation(r);
//      case 0x0039: return new FeatureIndication(r);
//      case 0x0040: return new InformationRate(r);
//      case 0x0042: return new End2EndTransitDelay(r);
//      case 0x0043: return new TransitDelaySelectionAndIndication(r);
//      case 0x0044: return new PacketLayerBinaryParameters(r);
//      case 0x0045: return new PacketLayerWindowSize(r);
//      case 0x0046: return new PacketSize(r);
      case 0x006C:  return new CallingPartyNumber(r);
      case 0x006D:  return new CallingPartySubAddress(r);
      case 0x0070:  return new CalledPartyNumber(r); 
      case 0x0071:  return new CalledPartySubAddress(r); 
//      case 0x0074: return new RedirectingNumber(r);
//      case 0x0078: return new TransitNetworkSelection(r);
//      case 0x0079: return new RestartIndicator(r);
      case 0x007C:  return new LowLayerCompatibility(r);
      case 0x007D:  return new HighLayerCompatibility(r);
      case 0x007E:  return new UserUser(r);
//      case 0x007F: return new Escape for extension(r);
//      case 0x0090: return new Shift(r);
//      case 0x00A0: return new MoreData(r);
      case 0x00A1: return new SendingComplete(r);
//      case 0x00B0: return new CongestionLevel(r);
//      case 0x00D0: return new RepeatIndicator(r);
    }
    System.err.println("Unimplemented IEI = 0x"+Integer.toHexString(iei));
    r.setLength();
    try{
      System.err.println(r.toString());
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
    return new Q931();                    
  }
}
