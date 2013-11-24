package uk.co.mmscomputing.device.phone;

import java.util.*;

public class PhoneAnswerer implements PhoneConstants{

  protected Vector listeners;                   // list of phone answerer event listeners

  public PhoneAnswerer(){
    listeners=new Vector();
  }

  public void addListener(PhoneAnswererListener listener){
    listeners.add(listener);
  }

  public void fireListenerUpdate(PhoneAnswererMetadata.Type type,PhoneAnswererMetadata md){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      PhoneAnswererListener listener=(PhoneAnswererListener)e.nextElement();
      listener.update(type,md);
    }
  }
}