package uk.co.mmscomputing.device.phone;

import java.io.*;

public class PhoneCallerMetadata{

  static public class Type{}
  static public Type	INFO=new Type();
  static public Type	EXCEPTION=new Type();
  static public Type	STATE=new Type();

  private int           state=0;
  private String        info="";
  private Exception     exception=null;
  private String        localno="Local No";
  private String        remoteno="Remote No";
  private int           timeout=3*60;

  private OutputStream  out;
  private InputStream   in;

  public  boolean       progressmonitor=false;

  public void      setState(int state){ this.state=state;}
  public int       getState(){ return state;}
  public String    getStateStr(){ return "State "+state;}
  public boolean   isState(int state){ return this.state==state;}

  public void          setOutputStream(OutputStream out){ this.out=out;}  
  public OutputStream  getOutputStream(){return out;}  

  public void          setInputStream(InputStream in){ this.in=in;}  
  public InputStream   getInputStream(){return in;}  

  public void      setInfo(String info){ this.info=info;}  
  public String    getInfo(){ return info;}  

  public void      setException(Exception ex){ this.exception=ex;}  
  public Exception getException(){ return exception;}  

  public void      setLocalNo(String no){ this.localno=no;}  
  public String    getLocalNo(){ return localno;}  

  public void      setRemoteNo(String no){ this.remoteno=no;}  
  public String    getRemoteNo(){ return remoteno;}  

  public void      setTimeOut(int sec){ this.timeout=sec;}  
  public int       getTimeOut(){ return timeout;}  

}