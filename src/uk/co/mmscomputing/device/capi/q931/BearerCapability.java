package uk.co.mmscomputing.device.capi.q931;

import uk.co.mmscomputing.device.capi.*;

public class BearerCapability extends Q931{

//  ITU Recommendation Q.931 (03/93)  p64  4.5.5.
//  uk.co.mmscomputing.device.capi.plc.ConnectInd
//  maximum length 12 octets

  public int codingStandard            =-1;
  public int infoTransferCap           =-1;    // 0x00 = Speech; 0x10 = 3.1.kHz audio
  public int transferMode              =-1;
  public int infoTransferRate          =-1;    // 0x10 = 64kbit/s; 0x11 = 128kbits/s; 0x18 = multi rate
  public int rateMultiplier            =-1;
  public int userInfoLayer1Protocol    =-1;    // 0x02=G.711 u-law; 0x03=G.711 A-Law
  public int userInfoLayer2Protocol    =-1;    // 
  public int userInfoLayer3Protocol    =-1;    // 

  public BearerCapability(Rider r){                   // decode
    r.setLength();
    try{
      int o=r.read();                                 // octet 3
      codingStandard=getBits(o,7,6);
      if(codingStandard!=CCITT){throw new IllegalArgumentException();}
      infoTransferCap=getBits(o,5,1);           
      o=r.read();                                     // octet 4
      transferMode=getBits(o,7,6);
      infoTransferRate=getBits(o,5,1);          
      if(infoTransferRate==0x18){                     // multi rate
        rateMultiplier=getBits(r.read(),7,1);         // octet 4.1
      }
      o=r.read();                                     // octet 5
      if(getBits(o,7,6)!=0x01){
        System.err.println(getClass().getName()+":\n\tExpect Layer 1 Information.");return;
      }
      userInfoLayer1Protocol=getBits(o,5,1);    
      if(isExt(o)){
        o=r.read();                                   // octet 5a
        if(isExt(o)){
          o=r.read();                                 // octet 5b
          if(isExt(o)){
            o=r.read();                               // octet 5c
            if(isExt(o)){
              o=r.read();                             // octet 5d
            }
          }
        }
      }

      o=r.read();                                     // octet 6
      if(getBits(o,7,6)!=0x02){
        System.err.println(getClass().getName()+":\n\tExpect Layer 2 Information.");return;
      }
      userInfoLayer2Protocol=getBits(o,5,1);    

      o=r.read();                                     // octet 7
      if(getBits(o,7,6)!=0x03){
        System.err.println(getClass().getName()+":\n\tExpect Layer 3 Information.");return;
      }
      userInfoLayer3Protocol=getBits(o,5,1);    

    }catch(IndexOutOfBoundsException ioobe){
//      ioobe.printStackTrace();
    }finally{
      r.skip();
    }
  }

  public BearerCapability(byte[] data){             // decode
    this(new Rider(data));
  }

  public int getSpeechCoding(){
    return userInfoLayer1Protocol;
  }

  public String toString(){
    String s="Bearer Capability - ";
    s+="\n    Transfer Capability = "+InformationTransferCapabilityStrs[infoTransferCap];
    s+="\n    Transfer Rate = "+InformationTransferRateStrs[infoTransferRate];

    switch(userInfoLayer1Protocol){
    case 2:  s+="\n    G.711 u-law";break;
    case 3:  s+="\n    G.711 A-law";break;
    }
    return s;
  }
/*
  public static void main(String[] argv){
    try{
      System.err.println(InformationTransferCapabilityStrs[0]);
      System.err.println(InformationTransferCapabilityStrs[0x8]);
      System.err.println(InformationTransferCapabilityStrs[0x9]);
      System.err.println(InformationTransferCapabilityStrs[0x10]);
      System.err.println(InformationTransferCapabilityStrs[0x11]);
      System.err.println(InformationTransferCapabilityStrs[0x18]);
      System.err.println(InformationTransferCapabilityStrs[31]);


      System.err.println(InformationTransferRateStrs[0x10]);
      System.err.println(InformationTransferRateStrs[0x11]);
      System.err.println(InformationTransferRateStrs[0x13]);
      System.err.println(InformationTransferRateStrs[0x15]);
      System.err.println(InformationTransferRateStrs[0x17]);
      System.err.println(InformationTransferRateStrs[0x18]);
      System.err.println(InformationTransferRateStrs[31]);



      System.err.println(UserInformationLayer1ProtocolStrs[1]);
      System.err.println(UserInformationLayer1ProtocolStrs[2]);
      System.err.println(UserInformationLayer1ProtocolStrs[3]);
      System.err.println(UserInformationLayer1ProtocolStrs[4]);
      System.err.println(UserInformationLayer1ProtocolStrs[5]);
      System.err.println(UserInformationLayer1ProtocolStrs[7]);
      System.err.println(UserInformationLayer1ProtocolStrs[8]);
      System.err.println(UserInformationLayer1ProtocolStrs[9]);
      System.err.println(UserInformationLayer1ProtocolStrs[31]);
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }
*/
}