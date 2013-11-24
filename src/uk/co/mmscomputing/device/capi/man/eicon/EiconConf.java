package uk.co.mmscomputing.device.capi.man.eicon;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.man.*;

public class EiconConf extends ManufacturerConf implements EiconConstants{

  public int manucmd;

  public EiconConf(Rider r){
    super(r);
    manucmd = r.readWord();
    info    = r.readWord();
  }

  public int getCmd(){return cmd;}

  public String toString(){
    String s=super.toString();

    s+="manucmd : 0x"+Integer.toHexString(manucmd)+"\n\t";

    return s;
  }

  static public MsgIn create(Rider r){
    int manucmd = r.readWord();
    switch(manucmd){
    }
    System.err.println(r.toString());
    return new EiconConf(r);
  }
}
