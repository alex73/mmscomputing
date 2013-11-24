package uk.co.mmscomputing.device.phone;

import uk.co.mmscomputing.concurrent.Semaphore;

public class PhoneAnswererMetadata{

  static public class Type{}
  static public Type	INFO=new Type();
  static public Type	EXCEPTION=new Type();
  static public Type	STATE=new Type();

  private int           state=0;
  private String        info="";
  private Exception     exception=null;
  private String        localno="Local No";
  private String        remoteno="Remote No";
  private String        file="sound.wav";
  private int           pickuptime=-1;
  private boolean       alaw=true;
  private int           cntl=-1;
  private boolean       changedProtocol=false;
  private Semaphore     blocker=null;

  private PhoneCallHandler pch;

  public           PhoneAnswererMetadata(){}

  public void      setState(int state){ this.state=state;}
  public int       getState(){ return state;}
  public String    getStateStr(){ return "State "+state;}
  public boolean   isState(int state){ return this.state==state;}

  public void      setInfo(String info){ this.info=info;}  
  public String    getInfo(){ return info;}  

  public void      setException(Exception ex){ this.exception=ex;}  
  public Exception getException(){ return exception;}  

  public void      setLocalNo(String no){ this.localno=no;}  
  public String    getLocalNo(){ return localno;}  

  public void      setRemoteNo(String no){ this.remoteno=no;}  
  public String    getRemoteNo(){ return remoteno;}  

  public void      setFile(String file){ this.file=file;}  
  public String    getFile(){ return file;}  

  public void      setPickUpTime(int sec){
                     this.pickuptime=sec;
                     blocker=new Semaphore(0,true);
                   }  

  public void      setPickUpTime(int sec,Semaphore semaphore){
                     this.pickuptime=sec;
                     blocker=semaphore;
                   }  

  public int       getPickUpTime(){ return pickuptime;}  

  public int       getController(){return cntl;}
  public void      setController(int cntl){this.cntl=cntl;}

  public void      setALaw(boolean alaw){ this.alaw=alaw;}  
  public boolean   useALaw(){ return alaw;}  

  public void      setChangedProtocol(boolean value){ changedProtocol=value;}  
  public boolean   getChangedProtocol(){ return changedProtocol;}  

  public void      setBlocker(Semaphore b){ this.blocker=b;}  
  public Semaphore getBlocker(){ return blocker;}  

  public void                setHandler(PhoneCallHandler pch){ this.pch=pch;}  
  public PhoneCallHandler    getHandler(){ return pch;}  

}