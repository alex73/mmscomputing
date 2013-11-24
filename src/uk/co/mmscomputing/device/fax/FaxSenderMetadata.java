package uk.co.mmscomputing.device.fax;

import uk.co.mmscomputing.concurrent.Semaphore;

public class FaxSenderMetadata{

  static public class Type{}
  static public Type	INFO=new Type();
  static public Type	EXCEPTION=new Type();
  static public Type	STATE=new Type();
  static public Type	GETFILE=new Type();

  private int           state=0;
  private String        info="";
  private Exception     exception=null;
  private String        header="Fax Header";
  private String        localno="Local No";
  private String        remoteno="Remote No";
  private String        file="";
  private int           timeout=3*60;

  public  boolean       progressmonitor=false;

  private Semaphore     blocker=null;

  public void      setState(int state){ this.state=state;}
  public int       getState(){ return state;}
  public String    getStateStr(){ return "State "+state;}
  public boolean   isState(int state){ return this.state==state;}

  public void      setInfo(String info){ this.info=info;}  
  public String    getInfo(){ return info;}  

  public void      setException(Exception ex){ this.exception=ex;}  
  public Exception getException(){ return exception;}  

  public void      setHeader(String header){ this.header=header;}  
  public String    getHeader(){ return header;}  

  public void      setLocalNo(String no){ this.localno=no;}  
  public String    getLocalNo(){ return localno;}  

  public void      setRemoteNo(String no){ this.remoteno=no;}  
  public String    getRemoteNo(){ return remoteno;}  

  public void      setFile(String file){ this.file=file;}  
  public String    getFile(){ return file;}  

//  public void      setTimeOut(int sec){ this.timeout=sec;}  

  public void      setTimeOut(int sec){
                     this.timeout=sec;
                     blocker=new Semaphore(0,true);
                   }  

  public void      setTimeOut(int sec,Semaphore semaphore){
                     this.timeout=sec;
                     blocker=semaphore;
                   }  

  public int       getTimeOut(){ return timeout;}  


  public void      setBlocker(Semaphore b){ this.blocker=b;}  
  public Semaphore getBlocker(){ return blocker;}  
}