package uk.co.mmscomputing.device.capi.protocol;

import uk.co.mmscomputing.device.capi.*;

public class SpeechProtocol extends BProtocol{

  public SpeechProtocol(){
    this(CapiConstants.KBIT64);						    // b1 = 64 kbit with byte framing
  }

  public SpeechProtocol(int kbit){
																							// b2 = transparent
																							// b3 = transparent
    super(kbit,1,0,StructOut.empty,StructOut.empty,StructOut.empty,StructOut.empty);
  }
}
