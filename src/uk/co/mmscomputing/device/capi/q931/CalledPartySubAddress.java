package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class CalledPartySubAddress extends Q931{

//  ITU Recommendation Q.931 (03/93)  p78  4.5.9.
//  uk.co.mmscomputing.device.capi.plc.ConnectInd

  private int      typeOfSubAddress=-1;                // 0x00=NSAP 0x01=user specified
  private int      oddeven=-1;                         // 0x00=even 0x01=odd number of address signals
  private String   digits="";                          // IA5
  private int      AFI=0x50;                           // Authority and Format Identifier (as BCD)

  public CalledPartySubAddress(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      typeOfSubAddress=getBits(o,7,5);
      if(typeOfSubAddress==0x00){                     // NSAP
        AFI=r.read();                                 // octet 4
        if(AFI!=0x50){                                // Houston we've got a problem
          System.err.println("CalledPartySubAddress.decode: Unexpected AFI field = "+AFI);
        }
        digits=r.readString();                        // Octet 5 +     
      }else if(typeOfSubAddress==0x01){               // User Specified
        oddeven=getBits(o,4,4);           
      }
    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public CalledPartySubAddress(byte[] data){
    this(new Rider(data));
  }

  public String getNumber(){return digits;}

  public String toString(){
    String s=super.toString();
    s+="Type of sub address                    = "+typeOfSubAddress+"\n";
    s+="Authority and Format Identifier        = "+AFI+"\n";
    s+="Digits                                 = "+digits+"\n";
    return s;
  }

  static public class Out extends StructOut{
    public Out(String digits){
      super(2+digits.length());
      writeByte(0x80);                                // NSAP
      writeByte(0x50);                                // AFI
      writeData(digits.getBytes(),0,digits.length());
    }
  }
}