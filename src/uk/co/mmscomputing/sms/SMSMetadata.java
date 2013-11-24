package uk.co.mmscomputing.sms;

public class SMSMetadata{

  static public class Type{}
  static public Type	INFO=new Type();
  static public Type	EXCEPTION=new Type();
  static public Type	STATE=new Type();

  private int           state=0;
  private String        info="";
  private Exception     exception=null;
  private String        localno="Local No";
  private String        remoteno="Remote No";
  private boolean       accept=false;

  private SMSPluginHandler sh;

  public           SMSMetadata(){}

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

  public void             setHandler(SMSPluginHandler sh){ this.sh=sh;}  
  public SMSPluginHandler getHandler(){ return sh;}  

  public void      setAccept(boolean accept){ this.accept=accept;}  
  public boolean   getAccept(){ return accept;}  

}