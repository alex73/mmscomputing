package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.*;
import javax.swing.*;

import uk.co.mmscomputing.device.capi.*;

abstract public class FaxSender implements FaxConstants{

  static protected boolean installed=false;
  
  public abstract boolean isAPIInstalled();

  protected Vector              listeners=new Vector();           // list of fax sender event listeners
  protected FaxSenderMetadata   md=new FaxSenderMetadata();       // information structure

  public abstract void send()throws IOException;

  public void setMetadata(Properties properties){
    new FaxSenderPanel(this,md,properties).display();
  }

  public void addListener(FaxSenderListener listener){
    listeners.add(listener);
  }

  public void fireListenerUpdate(FaxSenderMetadata.Type type){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      FaxSenderListener listener=(FaxSenderListener)e.nextElement();
      listener.update(type,md);
    }
  }

  public JComponent getGUI(Properties properties){
    return new FaxPanel(properties,this);
  }

  static public FaxSender getDevice(){
    String osname=System.getProperty("os.name");
    if(osname.startsWith("Linux")){
      FaxSender faxSender=CapiFaxSender.getDevice();
      if(faxSender.isAPIInstalled()){
        return faxSender;
      }
    }else if(osname.startsWith("Windows")){
      FaxSender faxSender=CapiFaxSender.getDevice();
      if(faxSender.isAPIInstalled()){
        return faxSender;
      }
    }else if(osname.startsWith("Mac")){
    }
    return null;
  }
}


