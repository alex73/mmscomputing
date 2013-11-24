package uk.co.mmscomputing.device.fax;

import java.util.*;

public class FaxReceiver implements FaxConstants{

  protected Vector              listeners;  // list of fax receiver event listeners

  public FaxReceiver(){
    listeners=new Vector();
  }

  public void addListener(FaxReceiverListener listener){
    listeners.add(listener);
  }

  public void fireListenerUpdate(FaxReceiverMetadata.Type type,FaxReceiverMetadata md){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      FaxReceiverListener listener=(FaxReceiverListener)e.nextElement();
      listener.update(type,md);
    }
  }
}