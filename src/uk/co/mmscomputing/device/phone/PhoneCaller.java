package uk.co.mmscomputing.device.phone;

import java.io.*;
import java.util.*;
import javax.swing.*;

import uk.co.mmscomputing.device.capi.*;

abstract public class PhoneCaller implements PhoneConstants{

  static protected boolean installed=false;
  
  public abstract boolean isAPIInstalled();

  protected Vector              listeners=new Vector();           // list of fax sender event listeners
  protected PhoneCallerMetadata md=new PhoneCallerMetadata();     // information structure

  public abstract void call()throws IOException;

  public void setMetadata(Properties properties){
    new PhoneCallerPanel(this,md,properties).display();
  }

  public void addListener(PhoneCallerListener listener){
    listeners.add(listener);
  }

  public void fireListenerUpdate(PhoneCallerMetadata.Type type){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      PhoneCallerListener listener=(PhoneCallerListener)e.nextElement();
      listener.update(type,md);
    }
  }

  public JComponent getGUI(Properties properties){
    return new PhoneCallerGUI(properties,this);
  }

  static public PhoneCaller getDevice(){
    String osname=System.getProperty("os.name");
    if(osname.startsWith("Linux")){
      PhoneCaller phoneCaller=CapiPhoneCaller.getDevice();
      if(phoneCaller.isAPIInstalled()){
        return phoneCaller;
      }
    }else if(osname.startsWith("Windows")){
      PhoneCaller phoneCaller=CapiPhoneCaller.getDevice();
      if(phoneCaller.isAPIInstalled()){
        return phoneCaller;
      }
    }else if(osname.startsWith("Mac")){
    }
    return null;
  }
}


