package uk.co.mmscomputing.device.capi.protocol;

import uk.co.mmscomputing.device.capi.StructOut;

public class BProtocol extends StructOut{

  public int B1Protocol=-1;
  public int B2Protocol=-1;
  public int B3Protocol=-1;

  public BProtocol(
     int B1Protocol,int B2Protocol,int B3Protocol,
     StructOut B1Conf,StructOut B2Conf,StructOut B3Conf,StructOut globalConf
  ){
    super(6+B1Conf.getLength()+B2Conf.getLength()+B3Conf.getLength()+globalConf.getLength());

    this.B1Protocol=B1Protocol;
    this.B2Protocol=B2Protocol;
    this.B3Protocol=B3Protocol;

    writeWord(B1Protocol);    //	B1 protocol; physical layer
    writeWord(B2Protocol);    //	B2 protocol; data link layer
    writeWord(B3Protocol);    //	B3 protocol; network layer
    writeStruct(B1Conf);      //	B1 protocol config
    writeStruct(B2Conf);      //	B2 protocol config
    writeStruct(B3Conf);      //	B3 protocol config
    writeStruct(globalConf);  //  global configuration
  }
}