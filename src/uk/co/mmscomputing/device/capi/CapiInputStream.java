package uk.co.mmscomputing.device.capi;

import java.io.*;

import uk.co.mmscomputing.device.capi.ncc.DataB3Ind;
import uk.co.mmscomputing.device.capi.ncc.DataB3Resp;
import uk.co.mmscomputing.device.capi.exception.CapiException;

import uk.co.mmscomputing.concurrent.Semaphore;

public class CapiInputStream extends InputStream{

  private CapiNCC            ncc;
  private boolean            isopen=true;

  private byte[]             buf=null;                                
  private int                count=0,max=0;
  private int                available=0;

  private Semaphore          blocker=new Semaphore(0,true);
  private int                indexOut=0,indexIn=0;
  private DataB3Ind[]        dataB3Inds=new DataB3Ind[8];

  // ---- Capi Thread Methods ----

  public CapiInputStream(CapiNCC ncc){
    this.ncc=ncc;
  }

  void received(DataB3Ind msg)throws CapiException{              // called from NCC
    if(isopen){
      synchronized(dataB3Inds){
        dataB3Inds[indexIn]=msg;
        indexIn=(indexIn+1)%8;
        available+=msg.getPayloadLength();
      }
      blocker.release();
    }else{                                                       // if not open waste data
      sendDataResp(msg.appid,msg.lineid,msg.getHandle());
    }
  }

  void disconnect(){                                             // called from NCC -> close input stream
    if(isopen){
      isopen=false;
      blocker.release();                                         // release read thread, if blocked
    }
  }  

  // ---- Inputstream Thread Methods ----

  void sendDataResp(int appid,int lineid,int handle)throws CapiException{  
    ncc.sendDataResp(new DataB3Resp(appid,lineid,handle)); 
  }

  private byte[] readNextBuffer()throws IOException{             // InputStream Thread
    if(!isopen){return null;}
    try{
      blocker.acquire();                                         // wait for data
    }catch(InterruptedException ie){
      isopen=false;
    }
    if(!isopen){blocker.release();return null;}                  // still open ?

    DataB3Ind msg;

    synchronized(dataB3Inds){
      msg=dataB3Inds[indexOut];
      indexOut=(indexOut+1)%8;

      count=0;                                                   // reset counter
      max=msg.getPayloadLength();                                // number of data bytes
      available-=max;
    }
    buf=msg.getPayload(buf);                                     // get payload
    sendDataResp(msg.appid,msg.lineid,msg.getHandle());          // tell capi we are done with data buffer
    return buf;
  }

  public int read()throws IOException{
    if(count==max){
      buf=readNextBuffer();
      if(buf==null){return -1;}                                  // reached EOS
    }
    return 0x000000FF & buf[count++];
  }

  public int read(byte[] b, int off, int len)throws IOException{
    if(b==null){
      throw new NullPointerException(getClass().getName()+".read(byte[] b, int off, int len): b is null");
    }
    if((off<0)||(len<0)||(b.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".read(byte[] b, int off, int len): index off ["+off+"] or len ["+len+"] out of bounds ["+b.length+"].");
    }
    int written=0;
    while(written<len){
      if(count==max){
        buf=readNextBuffer();
        if(buf==null){ 
          return(written!=0)?written:-1;                         // written==0 => reached EOS
        }
      }
      int maxblen=len-written;
      int maxbuflen=max-count;
      int bytesToWrite=(maxblen<maxbuflen)?maxblen:maxbuflen;
      System.arraycopy(buf,count,b,off+written,bytesToWrite);
      written+=bytesToWrite;
      count+=bytesToWrite;
    }
    return written;
  }

  public synchronized int available()throws IOException{
    return available+max-count;
  }

  public boolean isOpen(){return isopen;}

  public void close()throws IOException{
    if(isopen){
      isopen=false;
      blocker.release();                                         // release read thread, if blocked
      ncc.closedInput();                                         // signal NCC input stream is closed
    }
  }  
/*
  protected void finalize()throws Throwable{
    System.err.println(getClass().getName()+".finalize");
    super.finalize();
  }
*/
}