package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class KeypadFacility extends Q931{

  // ITU Recommendation Q.931 (03/93)  p93  4.5.18.
  // uk.co.mmscomputing.device.capi.parameter.AdditionalInfo

  // Information Element Identifier 0x2C

  String info="";                                     // IA5 coding

  //  input, decode

  public KeypadFacility(Rider r){
    r.setLength();
    try{
      info=r.readString();                            // Octet 3 + 
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
  }

  public KeypadFacility(byte[] data){
    this(new Rider(data));
  }

  public String getInformation(){return info;}

  public String toString(){
    String s="Keypad Information - \n";
    if(0<info.length()){
      s+=info;
    }else{
      s+="No keypad data";
    }
    return s;
  }

  static public class Out extends StructOut{
    public Out(String ia5Info){                       //  length < 32 bytes !
      super(ia5Info.length());
      writeData(ia5Info.getBytes(),0,ia5Info.length());
    }
  }
}
