package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DataB3Ind extends CapiIndMsg{
  private   int    nptr;
  private   int    handle;
  private   int    nlen;
  private   int    flags;
  private   long   n64ptr;

  public DataB3Ind(Rider r){
    super(r);
    nptr    =r.readDWord();        // 0: 32 bit native ptr to payload;
    nlen    =r.readWord();         // 4:
    handle  =r.readWord();         // 6:
    flags   =r.readWord();         // 8:
    if(nptr==0){                  
      n64ptr  =r.readQWord();		   //10: 64 bit native ptr to payload;
    }
  }                              //18

  public int    getHandle(){            return handle;}
  public int    getPayloadPtr(){        return nptr;}
  public int    getPayloadLength(){     return nlen;}
  public byte[] getPayload(byte[] buf){
    if(nptr!=0){
      return jcapi.copyFromNative32bitDataPtr(buf,nptr,nlen);
    }else{
      return jcapi.copyFromNative64bitDataPtr(buf,n64ptr,nlen);
    }
  }

  public String toString(){
    String s=super.toString();
    if(nptr!=0){
      s+="nptr     = 0x"+Integer.toHexString(nptr)+"\n";
    }else{
      s+="n64ptr   = 0x"+Long.toHexString(n64ptr)+"\n";
    }
    s+="nlen     = "+nlen+"\n";
    s+="handle   = "+handle+"\n";
    s+="flags    = 0"+Integer.toBinaryString(flags)+"b\n";
    return s;
  }
}

