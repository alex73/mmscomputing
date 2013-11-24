package uk.co.mmscomputing.device.capi.man.avm;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class AVMGetBChannelInfoConf extends AVMConf implements AVMConstants{

  // avm/kisdnwatch-01.00.08/kavmmon/ccapiinfo.h and ccapiinfo.cpp

  // Manu AVM!, Class 0, Function 4: GetBChannelInfo
  //
  // byte index    | type |    meaning                  | comment
  // 0..1          | word | registered applications     | including this application (kisdnwatch)
  // 2             | bool | isdn line active            | cable unplugged = 0; cable plugged in = 1; (my guess)
  // 3             | bool | d channel active            | as soon as something goes on = 1 (my guess)
  // 4             | ?    | ?                           | seems to be zero all the time
  // 5 .. n        | bool | b channel [0] .. [n] active | channel down = 0; channel up = 1; (kisdnwatch)

  private int       applicationCount,bchannelCount;
  private boolean   isdnline,dchannel;
  private boolean[] bchannels;
  
  public AVMGetBChannelInfoConf(Rider r){
    super(r);
    r.structBegin();

//    System.out.println(r.toString());

    applicationCount=r.read();r.read();
    isdnline=(r.read()==1);
    dchannel=(r.read()==1);
    r.read();
    bchannelCount=r.available();
    bchannels=new boolean[bchannelCount];
    for(int i=0;i<bchannelCount;i++){
      bchannels[i]=(r.read()==1);
    }
    r.structEnd();
  }

  public int     getRegisteredApplicationCount(){return applicationCount;}
  public boolean isISDNLineActive(){return isdnline;}
  public boolean isDChannelActive(){return dchannel;}
  public int     getBChannelCount(){return bchannelCount;}
  public boolean isBChannelActive(int channel){return bchannels[channel];}

  public String toString(){
    String s="AVM B-Channel Info \n";
    return s;
  }
}
