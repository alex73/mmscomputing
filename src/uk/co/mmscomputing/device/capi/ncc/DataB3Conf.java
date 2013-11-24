package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DataB3Conf extends CapiConfMsg{

  protected int handle;

  public DataB3Conf(Rider r){
    super(r);
    try{
      handle=r.readWord();
      info  =r.readWord();       
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public int getHandle(){return handle;}

  public String toString(){
    String s=super.toString();
    s+="handle = 0x"+Integer.toHexString(handle)+"\n\t";
    return s;
  }
}

