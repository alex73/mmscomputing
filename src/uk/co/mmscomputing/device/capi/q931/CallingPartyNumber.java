package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class CallingPartyNumber extends Q931{

//  ITU Recommendation Q.931 (03/93)  p80  4.5.10.
//  uk.co.mmscomputing.device.capi.plc.ConnectInd

  private int      typeOfNumber=0;                     // 0x01=international, 0x02=national, 0x04=subscriber
  private int      numberingPlanIdentification=0;      // 0x01=ISDN
  private int      presentationIndicator=0;            // 0x00=allowed, 0x01=restricted, 0x20=not available
  private int      screeningIndicator=0;               // 0x00=not screened
  private String   digits="";                          // IA5

  public CallingPartyNumber(Rider r){                 
    r.setLength();
    try{
      int o=r.read();
      typeOfNumber=getBits(o,7,5);                    // Octet 3
      numberingPlanIdentification=getBits(o,4,1);     
      if(isExt(o)){
        o=r.read();
        presentationIndicator=getBits(o,7,6);         // Octet 3*
        screeningIndicator=getBits(o,2,1);           
      }
      digits=r.readString();                          // Octet 4*
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public CallingPartyNumber(byte[] data){             // decode
    this(new Rider(data));
  }

  public String getNumber(){return digits;}

  public String toString(){
    String s="Calling Party Number - \n";
    s+="Type Of Number = "+TypeOfNumberStrs[typeOfNumber]+"\n";
    s+="Numbering Plan = "+NumberingPlanStrs[numberingPlanIdentification]+"\n";
    s+="Digits         = "+digits+"\n";
    return s;
  }

  static public class Out extends StructOut{
    public Out(String digits){
      super(2+digits.length());
      writeByte(0x00);                                // don't know type of number or numbering plan use default
      writeByte(0x80);                                // allow presentation no screening
      writeData(digits.getBytes(),0,digits.length());
    }
  }
}