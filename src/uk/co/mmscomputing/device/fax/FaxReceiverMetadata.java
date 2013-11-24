package uk.co.mmscomputing.device.fax;

import uk.co.mmscomputing.concurrent.Semaphore;

public class FaxReceiverMetadata{

  static public class Type{}
  static public Type	INFO=new Type();
  static public Type	EXCEPTION=new Type();
  static public Type	STATE=new Type();

  private int           state=0;
  private String        info="";
  private Exception     exception=null;
  private String        localno="Local No";
  private String        remoteno="Remote No";
  private String        header="Fax Header";
  private String        file="fax.sff";
  private int           pickuptime=-1;
  private int           cntl=-1;
  private int           maxillegallinecodings=-1;
  private Semaphore     blocker=null;

  private FaxCallHandler fch;

  public                FaxReceiverMetadata(){}

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

  public void      setHeader(String header){ this.header=header;}  
  public String    getHeader(){ return header;}  

  public void      setFile(String file){ this.file=file;}  
  public String    getFile(){ return file;}  

//  public void      setPickUpTime(int sec){ this.pickuptime=sec;}  

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

  public void      setBlocker(Semaphore b){ this.blocker=b;}  
  public Semaphore getBlocker(){ return blocker;}  

  public void      setMaxIllegalLineCodings(int milc){maxillegallinecodings=milc;}
  public int       getMaxIllegalLineCodings(){return maxillegallinecodings;}

  public void           setHandler(FaxCallHandler fch){ this.fch=fch;}  
  public FaxCallHandler getHandler(){ return fch;}  
}