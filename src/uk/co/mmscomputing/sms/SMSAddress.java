package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSAddress extends SMSTransportProtocolParameter{

  // [1] p.40 9.1.2.5 Address fields

  private int    numberingPlan=0;                   // Numbering-plan-identification
  private int    typeOfNumber=0;                    // Type-of-number
  private byte[] number;                            // max 10 digits

  public SMSAddress(InputStream in)throws IOException{
    int nolen     = read(in);
    numberingPlan = read(in);                       // bit 7 always set
    typeOfNumber  = (numberingPlan>>4)&0x0007;      // bits 6..4
    numberingPlan&= 0x0F;                           // bits 3..0
    number        = new byte[nolen];
    if(nolen!=new SMSNibbleInputStream(in).read(number)){
      throw new IOException(getClass().getName()+"<init>:\nUnexpected EOF. Could not read whole number.");
    }
  }

  public SMSAddress(int np,int ton,byte[] n){
    numberingPlan=np;
    typeOfNumber=ton;
    number=n;
  }

  public SMSAddress(int np,int ton,String n){
    this(np,ton,n.getBytes());
  }

  public SMSAddress(String n){
    this(0x01,0x00,n.getBytes()); // use E.164/E.163 numbering plan; type of number unknown to us
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(number.length);
    out.write(0x80|(typeOfNumber<<4)|numberingPlan);  
    out=new SMSNibbleOutputStream(out);
    out.write(number);
    out.flush();
  }

  public String getTypeOfNumberString(){
    switch(typeOfNumber){
    case  0: return "Unknown";
    case  1: return "International number";
    case  2: return "National number";
    case  3: return "Network specific number";
    case  4: return "Subscriber number";
    case  5: return "Alphanumeric, (coded according to 3GPP TS 23.038 [9] GSM 7-bit default alphabet)";
    case  6: return "Abbreviated number";
    default: return "Reserved for extension";
    }
  }

  public String getNumberingPlanString(){
    switch(numberingPlan){
    case  0: return "Unknown";
    case  1: return "ISDN/telephone numbering plan (E.164/E.163)";
    case  3: return "Data numbering plan (X.121)";
    case  4: return "Telex numbering plan";
    case  5: return "Service Centre Specific plan (5)";
    case  6: return "Service Centre Specific plan (6)";
    case  8: return "National numbering plan";
    case  9: return "Private numbering plan";
    case 10: return "ERMES numbering plan (ETSI DE/PS 3 01-3)";
    default: return "Reserved for extension";
    }
  }
 
  public String getNumber(){
    return new String(number);
  }

  public String toString(){
    String s=getClass().getName()+":\n";
    s+="numbering plan = "+getNumberingPlanString()+"\n";
    s+="type of number = "+getTypeOfNumberString()+"\n";
    s+="number         = "+getNumber()+"\n";
    return s;
  }
}

// [1] ETSI TS 123 040 (2004-09)
