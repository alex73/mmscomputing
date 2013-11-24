package uk.co.mmscomputing.device.sane;

import uk.co.mmscomputing.device.scanner.*;

public class SaneIOException extends ScannerIOException{

  public SaneIOException(String msg){ // Need this. JNI wouldn't find IOException constructor.
    super("\n\t"+msg);
  }

  public SaneIOException(Class clazz,String method,String msgid){
    super(clazz.getName()+"."+method+":\n\t"+jsane.getResource(msgid));
  }

  public SaneIOException(Class clazz,String method,String msgid,String arg){
    super(clazz.getName()+"."+method+":\n\t"+jsane.getResource(msgid,arg));
  }

  public SaneIOException(Class clazz,String method,String msgid,String[] args){
    super(clazz.getName()+"."+method+":\n\t"+jsane.getResource(msgid,args));
  }
}