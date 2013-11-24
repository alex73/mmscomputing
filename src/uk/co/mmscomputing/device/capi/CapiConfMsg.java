package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.device.capi.exception.*;

public class CapiConfMsg extends MsgIn{

  protected int info;

  public CapiConfMsg(Rider r){super(r);}

  public int getInfo(){return info;}

  public String toString(){
    String s=super.toString();
    s+="info   = 0x"+Integer.toHexString(info)+" - "+ CapiIOException.capiInfo2Str(info)+"\n";
    return s;
  }
}

