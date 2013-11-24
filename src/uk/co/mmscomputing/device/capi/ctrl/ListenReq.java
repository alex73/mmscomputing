package uk.co.mmscomputing.device.capi.ctrl;

import uk.co.mmscomputing.device.capi.MsgOut;

public class ListenReq extends MsgOut{
 
  static final int ACCEPTALL          = 0x1FFF03FF;    //  0x1FFF03FF = connect to everything cip mask

  static final int INFONONE           = 0x00000000;    //  0x00000000 = no info's
  static final int INFOCAUSE          = 0x00000001;    //  0x00000001 = send all cause info's
  static final int INFOALL            = 0x000017FF;    //  0x000017FF = send all info's available

  static final int INFOEARLYB3CONNECT = 0x00000200;    //  early b3 connect bit

  public ListenReq(int appid, int ctrl, int cipmask){
    super(14,appid,CAPI_LISTEN,CAPI_REQ,ctrl);
    writeDWord(INFOALL&(~INFOEARLYB3CONNECT));         //  info mask; all possible bits set except 'early B3 connect'
    writeDWord(cipmask&ACCEPTALL);                     //  what stuff do we want to listen to
    writeDWord(0);                                     //  reserved cip mask
    writeStruct();                                     //  calling party number for external equipment
    writeStruct();                                     //  calling party sub address for external equipment
  }

  public ListenReq(int appid, int ctrl){
    this(appid,ctrl,ACCEPTALL);
  }

}