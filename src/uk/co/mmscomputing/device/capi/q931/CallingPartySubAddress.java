package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class CallingPartySubAddress extends Q931{

//  ITU Recommendation Q.931 (03/93)  p82  4.5.11.
//  uk.co.mmscomputing.device.capi.plc.ConnectInd

  public int      typeOfSubAddress=-1;                // 0x00=NSAP 0x01=user specified
  public int      oddeven=-1;                         // 0x00=even 0x01=odd number of address signals
  public String   digits="";                          // IA5
  public int      AFI=0x50;                           // Authority and Format Identifier (as BCD)

  public CallingPartySubAddress(Rider r){
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      typeOfSubAddress=getBits(o,7,5);
      if(typeOfSubAddress==0x00){                     // NSAP
        AFI=r.read();               
        if(AFI!=0x50){                                //  Houston we've got a problem
          System.err.println("CallingPartySubAddress.decode: Unexpected AFI field = "+AFI);
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

  public CallingPartySubAddress(byte[] data){
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