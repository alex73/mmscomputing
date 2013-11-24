package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSTransportProtocolDataUnit extends SMSDataUnit{

  // Transport Protocol Data Unit

  private   byte[]              data = new byte[0];

  public SMSTransportProtocolDataUnit(){}

  public byte[] getUserData(){return data;}
  public void   setUserData(byte[] data){this.data=data;}

  public void readUserDataFrom(InputStream in)throws IOException{
    boolean udhi = getBoolean("TP-UDHI");         // bit 6      TP-User-Data-Header-Indicator (TP-UDHI)
    int     udl  = read(in);                      // TP-User-Data-Length (TP-UDL)

    if(udhi){                                     // we have optional features
      int   udhl = read(in);                      // TP-User-Data-Header-Length (TP-UDHL)
      byte[] udh = new byte[udhl];                // TP-User Data (TP-UDH)

System.err.println("TP-UDHL = "+udhl);
          
      for(int i=0;i<udhl;i++){
        System.err.println("["+i+"]=0x"+Integer.toHexString(read(in)));
      }

//      if(udhl!=in.read(data)){                    // waste data
//      }
      
      udl -= udhl;
    }

    data         = new byte[udl];                 // TP-User Data (TP-UD)

//  short message can be 7, 8 or 16 bit
//  support only 7 bit yet.

    if(udl!=new SMS7BitInputStream(in).read(data)){
    }
  }

  public void writeUserDataTo(OutputStream out)throws IOException{
/*
    out.write(data.length+4);

    out.write(3);                                 // TP-UDHL
                                                  // Information Data Element
    out.write(0x0B);                              // sound
    out.write(1);                                 // length of data
    out.write(3);                                 // "ta da"

    out=new SMS7BitOutputStream(out);
    out.write(data);
    out.flush();
*/
///*
    out.write(data.length);
    out=new SMS7BitOutputStream(out);
    out.write(data);
    out.flush();
//*/
  }

  public String toString(){
    String s=super.toString();
    if(data.length>0){    s+="\ndata        = "+new String(data)+"\n\n";}
    return s;
  }
}

// [1] ETSI TS 123 040 (2004-09)
