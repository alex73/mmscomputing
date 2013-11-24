package uk.co.mmscomputing.device.capi;

import java.io.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.sound.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;
import uk.co.mmscomputing.device.capi.facility.*;

abstract public class CapiChannel implements CapiConstants{

  static public final AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);
  static public final AudioFormat alawformat= new AudioFormat(AudioFormat.Encoding.ALAW,8000,8,1,1,8000,false);
  static public final AudioFormat ulawformat= new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);

  protected boolean          isopen           = false;
  private   int              connectState     = 0;       // 0 = not connected yet; 1 = connected; 2 = disconnected
  protected boolean          pickedUp         = false;
  protected boolean          changedProtocol  = false;

  protected CapiApplication  appl     = null;
  protected int              applid   = -1;
  protected int              lineid   = -1;

  protected BProtocol        protocol = null;
  protected CapiPlugin       plugin   = null;

  private   String           localno  = "";
  private   String           remoteno = "";
  protected int              speechcoding   = -1;

  private   Semaphore        pickupBlocker = null;       // see CapiCallApplication, CapiServerApplication

  public CapiChannel(CapiApplication appl,int lineid){
    this.appl=appl;
    this.applid=appl.getApplID();
    this.lineid=lineid;
  }

  public boolean isOpen(){return isopen;}
  public void close()throws IOException{};                                 

  synchronized protected boolean checkIsOpen(){
    if(isopen==false){return false;}
    isopen=false;
    return true;
  }

  public boolean isConnected(){return (connectState==1);}

  synchronized protected boolean checkIsNotConnected(){
    if(connectState==0){
      connectState=1;
      isopen=true;
      return true;
    }
    return false;
  }

  public boolean isDisconnected(){return (connectState==2);}

  synchronized protected boolean checkIsNotDisconnected(){
    if(connectState==2){return false;}
    connectState=2;
    isopen=false;
    return true;
  }

  void setPickupBlocker(Semaphore s){pickupBlocker=s;}   // the calling thread waits on 'pickupBlocker' until plc releases blocker
  Semaphore getPickupBlocker(){return pickupBlocker;}
  void releasePickupBlocker(){
    if(pickupBlocker!=null){pickupBlocker.release();}
  };

  abstract public CapiInputStream getInputStream()throws IOException;
  abstract public CapiOutputStream getOutputStream()throws IOException;

  public PCMInputStream getPCMInputStream()throws IOException{
    return new PCMInputStream(getInputStream(),isALaw());
  }

  public PCMOutputStream getPCMOutputStream()throws IOException{
    return new PCMOutputStream(getOutputStream(),isALaw());
  }

  public AudioInputStream getAudioInputStream()throws IOException{
    return new AudioInputStream(getPCMInputStream(),pcmformat,AudioSystem.NOT_SPECIFIED);
  }

  public int getApplID(){return applid;}
  public int getLineID(){return lineid;}
  public void put(MsgOut msg)throws CapiException{
    appl.put(msg);
  }

  public int getCtrlId(){return lineid&0x0000007F;}

  public BProtocol getProtocol(){return protocol;}
  public void setProtocol(BProtocol protocol){this.protocol=protocol;}
  public CapiPlugin getPlugin(){return plugin;}
  public void setPlugin(CapiPlugin plugin){this.plugin=plugin;}
  public String getLocalNo(){ return localno;}
  public void setLocalNo(String no){ localno=no;}

  public String getRemoteNo(){ return remoteno;}
  public void setRemoteNo(String no){remoteno = no;}


  public void setSpeechCoding(int sc){ speechcoding=sc;}
  public void setALawSpeechCoding(){ speechcoding=LAYER1USERINFO_ALAW;}
  public void setuLawSpeechCoding(){ speechcoding=LAYER1USERINFO_uLAW;}
  public int  getSpeechCoding(){ return speechcoding;}

  public boolean isALaw()throws CapiException{ 
    switch(speechcoding){
    case LAYER1USERINFO_ALAW: return true;
    case LAYER1USERINFO_uLAW: return false;
    default: throw new CapiInformation(getClass().getName()+".isALaw:\n\tThis Channel does not seem to carry sound data !");
    }
  }

  public boolean isuLaw()throws CapiException{ 
    return !isALaw();
  }

         void    setPickedUp(boolean pu){pickedUp=pu;}
  public boolean getPickedUp(){return pickedUp;}

         void    setChangedProtocol(boolean value){changedProtocol=value;}
  public boolean getChangedProtocol(){return changedProtocol;}

  public void writeInputTo(OutputStream out)throws IOException{
    InputStream  in  = getInputStream();

    int    count;
    byte[] buffer = new byte[DefaultB3DataBlockSize];

    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.flush();
  }

  public void writePCMInputTo(OutputStream out)throws IOException{
    InputStream  in  = getPCMInputStream();

    int    count;
    byte[] buffer = new byte[DefaultPCMBlockSize];

    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.flush();
  }

  public void writeInputTo(SourceDataLine out)throws IOException{
    InputStream  in  = getPCMInputStream();

    int    count;
    byte[] buffer = new byte[DefaultPCMBlockSize];

    out.start();
    in.skip(in.available());                  // discard data we couldn't process in time
    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.drain();
    out.stop();
  }

  public void writeToOutput(InputStream in)throws IOException{
    OutputStream  out  = getOutputStream();

    int    count;
    byte[] buffer = new byte[DefaultB3DataBlockSize];

    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.flush();
  }

  public void writeToPCMOutput(InputStream in)throws IOException{
    OutputStream out = getPCMOutputStream();

    int    count;
    byte[] buffer = new byte[DefaultPCMBlockSize];

    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.flush();
  }

  public void writeToOutput(TargetDataLine in)throws IOException{
    OutputStream out = getPCMOutputStream();

    int count;
    byte[] buf=new byte[DefaultPCMBlockSize];

    in.start();
    while(isOpen()&&((count=in.read(buf,0,buf.length))!=-1)){
      out.write(buf,0,count);
    }
    out.flush();
  }

  public void writeToOutput(AudioInputStream in)throws IOException{
    if(!((AudioInputStream)in).getFormat().equals(pcmformat)){
      in=AudioSystem.getAudioInputStream(pcmformat,(AudioInputStream)in);
    }
    OutputStream out = getPCMOutputStream();

    int    count;
    byte[] buffer = new byte[DefaultPCMBlockSize];

    while(isOpen()&&((count=in.read(buffer))!=-1)){
      out.write(buffer,0,count);
    }
    out.flush();
  }

// ---- DTMF ----

  private boolean isdtmfenabled=false;

  public boolean isDTMFEnabled(){return isdtmfenabled;}

  public void startDTMF()throws IOException{
    isdtmfenabled=(isopen)?true:false;
    if(isopen){appl.put(DTMFReq.getStartReq(applid,lineid));}
  }

  public void stopDTMF()throws IOException{
    isdtmfenabled=false;
    if(isopen){appl.put(DTMFReq.getStopReq(applid,lineid));}
  }

  public void sendDTMF(String digits)throws IOException{
    if(isopen){appl.put(DTMFReq.getSendReq(applid,lineid,digits));}
  }

  public void receivedDTMFConf(DTMFConf msg){            
    if(msg.getInfo()!=0){        // if error with dtmf request
      isdtmfenabled=false;       // assume controller does not support dtmf
    }
  }

  private Semaphore     blocker = null;
  private StringBuffer  dtmf    = null;

  public synchronized void receivedDTMFInd(DTMFInd msg){   // capi thread
    /*
    dtmf 0..9,*,#,A..D
    cng-fax tone ='X'  1.1kHz
    ced-fax tone ='Y'  2.1kHz
    */

    String digits=msg.getDigits();                         // System.out.println("Received digits: "+digits);
    if(blocker!=null){                                     // if a thread is waiting
      dtmf.append(digits);                                 // append digits to buffer
      blocker.release();                                   // tell appl thread
    }
  }

  public String getDTMFDigits(int count, int timeout)
      throws InterruptedException
  {                                                         // appl thread 
    dtmf=new StringBuffer();                                // wait for 'count' digits within 'timeout' time
    blocker=new Semaphore(1-count,true);
    blocker.tryAcquire(timeout,TimeUnit.MILLISECONDS);
    blocker=null;
    return dtmf.toString();
  }

// ---- END : DTMF ----

// ---- SUP SERVICE ----

  public void explicitCallTransferTo(CapiChannel channel)throws CapiException{      // ECT
    CapiPLC plc0=appl.getPLC(getLineID());
    CapiPLC plc1=appl.getPLC(channel.getLineID());
    plc0.explicitCallTransferTo(plc1);
  }

// ---- END : SUP SERVICE ----
}