package uk.co.mmscomputing.device.capi.parameter;

import uk.co.mmscomputing.device.capi.*;

public class FacilityPartyNumber{

  // COMMON-ISDN-API Version 2.0 - Part III 4rd Edition
  // p.29

  static final public int ToFPN_Unknown           = 0;
  static final public int ToFPN_PublicPartyNumber = 1;
/*
  static public class In{

    private int     tofpn,tonanpi,pasi;
    private String  digits;

    public In(Rider r){   // bytes has to contain only the data for this struct!
      try{
        this.tofpn   = r.read();
        this.tonanpi = r.read();
        this.pasi    = r.read();
        this.digits  = r.readString();
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }      
    }

    public String getNumber(){return digits;}

    public String toString(){
      String s=super.toString();
      s+="Type of facility party number                    ="+tofpn+"\n";
      s+="Type of number and numbering plan identification ="+tonanpi+"\n";
      s+="Presentation and screening indicator             ="+pasi+"\n";
      s+="Digits                                           ="+digits+"\n";
      return s;
    }
  }
*/
  static public class Out extends StructOut{

    private int     tofpn,tonanpi,pasi;
    private String  digits;

    public Out(int tofpn,int tonanpi,int pasi,String digits){
      super(3+digits.length());

      this.tofpn   = tofpn;
      this.tonanpi = tonanpi;
      this.pasi    = pasi;
      this.digits  = digits;

      writeByte(tofpn);                   // Type of facility party number
      writeByte(tonanpi);                 // Type of number and numbering plan identification (byte 0 of calling party number)
      writeByte(pasi);                    // Presentation and screening indicator (byte 1 of calling party number)
      writeData(digits.getBytes(),0,digits.length());

      System.out.println(toString(getBytes()));
    }

    public Out(String digits){
      this(ToFPN_PublicPartyNumber,0x00,0x80,digits);      // public;default plan;allow presentation no screening
    }

    public String toString(){
      String s=super.toString();
      s+="Type of facility party number                    ="+tofpn+"\n";
      s+="Type of number and numbering plan identification ="+tonanpi+"\n";
      s+="Presentation and screening indicator             ="+pasi+"\n";
      s+="Digits                                           ="+digits+"\n";
      return s;
    }
  }
}


