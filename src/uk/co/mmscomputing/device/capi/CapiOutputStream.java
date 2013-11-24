package uk.co.mmscomputing.device.capi;

import java.io.*;
import java.nio.channels.ClosedChannelException;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.ncc.DataB3Req;
import uk.co.mmscomputing.device.capi.ncc.DataB3Conf;
import uk.co.mmscomputing.device.capi.exception.CapiException;
import uk.co.mmscomputing.device.capi.exception.CapiIOException;

public class CapiOutputStream extends OutputStream implements CapiConstants{

  final private static int   maxBuffers = 8;

  private byte[]             buf;
  private int                count      = 0;

  private CapiNCC            ncc;
  private boolean            isopen;

  private	Semaphore          blocker    = new Semaphore(maxBuffers-1,true);
  private int                current    = 0;
  private byte[][]           buffers    = new byte[maxBuffers][DefaultB3DataBlockSize];
  private DataB3Req[]        dataB3Reqs = new DataB3Req[maxBuffers];

  private   int              unconfirmed      = 0;
  private   boolean          isdisconnected   = false;
  private   Semaphore        ds               = new Semaphore(0,true);

  // ---- Capi Thread Methods ----

  CapiOutputStream(CapiNCC ncc){
    this.ncc=ncc;
    this.isopen=true;
    this.buf=buffers[0];
  }

  synchronized protected boolean checkIsOpen(){
    if(isopen==false){return false;}
    isopen=false;
    return true;
  }

  private void releaseDataB3Reqs(int handle){
    synchronized(dataB3Reqs){
      if(dataB3Reqs[handle]!=null){
        dataB3Reqs[handle].release();                      // release native byte buffer
        dataB3Reqs[handle]=null;
        unconfirmed--;
      }
    }
  }

  private void releaseDataB3Reqs(){
    for(int handle=0;handle<dataB3Reqs.length;handle++){
      releaseDataB3Reqs(handle);                           // release native byte buffer
    }
  }

  void received(DataB3Conf msg)throws CapiException{
    releaseDataB3Reqs(msg.getHandle());                    // release native byte buffer
    blocker.release();                                     // release threads that wait on Outputstream Write methods
    if(!isopen&&(unconfirmed==0)){                         // if stream is to be closed and all req have been confirmed
      isdisconnected=true;                                 // we are truly disconnected
      ds.release();                                        // release threads that wait at close()
    }
  }

  boolean isDisconnected(){return isdisconnected;}

  synchronized void disconnect()throws CapiException{      // called from NCC -> close output stream    
    isdisconnected=true;                                   // passive disconnect; disconnected by peer
    isopen=false;                                          // we cannot send any data and do not receive any DataB3Conf anymore
    blocker.release();                                     // release output stream thread if blocked
    releaseDataB3Reqs();                                   // release native byte buffers
    ds.release();                                          // release threads that wait at close()
  }  

  // ---- OutputStream Thread Methods ----

  boolean isOpen(){return isopen;}

  private void writeBuffer(int count)throws IOException{
    try{
      if(isdisconnected){return;}
      blocker.acquire();                                   // wait until capi can accept next DataB3Req
      if(isdisconnected){blocker.release();return;}

      synchronized(dataB3Reqs){
        dataB3Reqs[current]=ncc.write(current,buf,count);  // send DataB3Req
        current=(current+1)%maxBuffers;                    // use next buffer
        buf=buffers[current];
        unconfirmed++;
      }
    }catch(InterruptedException ie){
      disconnect();
    }
  }

  public void write(int b)throws IOException{
    if(!isopen){throw new ClosedChannelException();}
    if(count==DefaultB3DataBlockSize){                     // collect bytes until buffer is full and then send.
      writeBuffer(count);count=0;
    }
    buf[count++]=(byte)b;
  }

  public void write(byte[] b, int off, int len)throws IOException{
//    if(!isopen){throw new ClosedChannelException();}
    if(b==null){
      throw new NullPointerException(getClass().getName()+".write(byte[] b, int off, int len):\n\tb is null");
    }
    if((off<0)||(len<0)||(b.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".write(byte[] b, int off, int len):\n\tindex off or len out of bounds.");
    }
    if(count>0){                                           // flush data we might have accumulated with simple write
      writeBuffer(count);count=0;
    }
    while(len>DefaultB3DataBlockSize){                     // chop data into chunks and send off
      System.arraycopy(b,off,buf,0,DefaultB3DataBlockSize);
      writeBuffer(DefaultB3DataBlockSize);
      len-=DefaultB3DataBlockSize;
      off+=DefaultB3DataBlockSize;
    }
    if(len>0){
      System.arraycopy(b,off,buf,0,len);
      writeBuffer(len);
    }
  }

  public void flush()throws IOException{
    if(!isopen){throw new ClosedChannelException();}
    if(count>0){
      writeBuffer(count);                                // flush data in buf
      count=0;
    }
  }

  public void close()throws IOException{
//    System.err.println("\n\n\nUnconfirmed DataB3Reqs = "+unconfirmed+"\n\n\n");
    if(checkIsOpen()){
      writeBuffer(count);                                // make sure there is at least one pending DataB3Req
      try{ds.acquire();}catch(InterruptedException ie){  // wait for DisconnectB3Ind
        ie.printStackTrace();
      }
      ds.release();                                      // release other threads
      ncc.closedOutput();                                // signal NCC output stream is closed
    }else{
      try{ds.acquire();}catch(InterruptedException ie){  // wait for DisconnectB3Ind
        ie.printStackTrace();
      }
      ds.release();                                      // release other threads
    }
  }

  protected void finalize()throws Throwable{
//    System.err.println(getClass().getName()+".finalize:\n\t"+toString());
    close();super.finalize();
  }
}