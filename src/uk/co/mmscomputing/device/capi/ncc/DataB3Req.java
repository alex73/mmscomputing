package uk.co.mmscomputing.device.capi.ncc;

import uk.co.mmscomputing.device.capi.*;

public class DataB3Req extends MsgOut{

  private int    nptr=0;
  private int    handle;
  private int    nlen;
  private int    flags;
  private long   n64ptr=0;
  private byte[] payload;

  static private int getStructSize(int nlen){
    if(jcapi.ptrSize==4){                               // local 32 bit
      return 10;
    }else if(jcapi.ptrSize==8){                         // local 64 bit
      return 18;
    }else{                                              // send via network
      return 18+nlen;
    }
  }

  public DataB3Req(int appid, int ncci, int handle, int flags, byte[] payload,int nlen){
    super(getStructSize(nlen),appid,CAPI_DATA_B3,CAPI_REQ,ncci);

    if(jcapi.ptrSize==4){
      nptr=jcapi.acquireNative32bitDataPtr(payload);    // need to keep native pointer valid until DataB3Conf

      writeDWord(nptr);			                            //  0: space for 32 bit pointer
      writeWord(nlen);                                  //	4: payload len
      writeWord(handle);                                //	6: referenced in DATA_B3_CONF
      writeWord(flags);                                 //  8: 
                    			                              // 10: 22 bytes up until here
    }else if(jcapi.ptrSize==8){
      n64ptr=jcapi.acquireNative64bitDataPtr(payload);  // need to keep native pointer valid until DataB3Conf

      writeDWord(0);		  	                            //  0: space for 32 bit pointer
      writeWord(nlen);                                  //	4: payload len
      writeWord(handle);                                //	6: referenced in DATA_B3_CONF
      writeWord(flags);                                 //  8: 
      writeQWord(n64ptr);			                          // 10: space for 64 bit pointer
                     			                              // 18: 30 bytes up until here
    }else{                                              //  does not work yet
      writeDWord(0);		  	                            //  0: space for 32 bit pointer
      writeWord(nlen);                                  //	4: payload len
      writeWord(handle);                                //	6: referenced in DATA_B3_CONF
      writeWord(flags);                                 //  8: 
      writeQWord(0);			                              // 10: space for 64 bit pointer

      writeData(payload,0,nlen);                        // 18: 30 bytes up until here
    }

    this.handle  =handle;
    this.nlen    =nlen;
    this.flags   =flags;
    this.payload =payload;
  }

  public void release(){                                // call when DataB3Conf received
    if(nptr!=0){
      jcapi.releaseNative32bitDataPtr(payload,nptr);
      nptr=0;
    }else if(n64ptr!=0){
      jcapi.releaseNative64bitDataPtr(payload,n64ptr);
      n64ptr=0;
    }
  }

  protected void finalize()throws Throwable{release();}

  public String toString(){
    String s=super.toString();
    if(n64ptr!=0){
      s+="n64ptr   = 0x"+Long.toHexString(n64ptr)+"\n";
    }else{
      s+="nptr     = 0x"+Integer.toHexString(nptr)+"\n";
    }
    s+="nlen     = "+nlen+"\n";
    s+="handle   = "+handle+"\n";
    s+="flags    = 0"+Integer.toBinaryString(flags)+"b\n";
    return s;
  }

  public int getHandle(){ return handle;}
  public byte[] getData(){return payload;}
}