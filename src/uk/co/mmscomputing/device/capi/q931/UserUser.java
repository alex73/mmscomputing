package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class UserUser extends Q931{

  //  ITU Recommendation Q.931 (03/93)  p116  4.5.30.
  //  uk.co.mmscomputing.device.capi.parameter.AdditionalInfo

  // Information Element Identifier 0x7E

  static final public int IA5=4;

  private int    pd=-1;                               // protocol discriminator
  private String info="";

  //  input, decode

  public UserUser(Rider r){
    r.setLength();
    try{
      pd   =r.read();                                 // Octet 3
      info =r.readString();                           // octet 4+ 
    }catch(IndexOutOfBoundsException ioobe){
    }finally{
      r.skip();
    }
  }

  public UserUser(byte[] data){
    this(new Rider(data));
  }

  public int getProtocolDiscriminator(){return pd;}
  public String getInformation(){return info;}

  public String toString(){
    String s="User-User Information - \n";
    if(pd!=-1){
      s+="\n\tprotocol discriminator: "+pd;
      switch(pd){
      case IA5: s+="\n\t"+new String(info);
      }
    }else{
      s+="\n\tNo User-User data";
    }
    return s;
  }

  static public class Out extends StructOut{
    public Out(String ia5Info){                         // allowed length network dependent i.e. 31,131
      super(1+ia5Info.length());
      writeByte(4);                                     // Octet 3 : IA5=4
      writeData(ia5Info.getBytes(),0,ia5Info.length()); // Octet 4 + 
    }
  }
}
