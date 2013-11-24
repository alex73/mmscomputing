package uk.co.mmscomputing.device.sane;

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.sane.gui.SanePanel;

public class SaneScanner extends Scanner{

  public SaneScanner(){
    super();
    metadata=new SaneIOMetadata();
  }

  public boolean isAPIInstalled(){return jsane.isInstalled();}

  public void select()throws ScannerIOException{
    jsane.select(this);
  }

  public String[] getDeviceNames(){
    String[] names = new String[0];
    try{
      jsane.init();
      try{
        names=jsane.getDeviceManager().getDevices();
      }finally{
        jsane.exit();
      }
    }catch(Exception e){
      metadata.setException(e);
      fireListenerUpdate(metadata.EXCEPTION);
    }
    return names;
  }

  SaneDevice getSelectedDevice()throws ScannerIOException{
    return jsane.getDeviceManager().getDevice();
  }
 
  public String getSelectedDeviceName()throws ScannerIOException{
    return getSelectedDevice().getName();
  }
 
  public void select(String name)throws ScannerIOException{
    jsane.select(this,name);
  }

  public void acquire()throws ScannerIOException{
    jsane.acquire(this);
  }

  public void setCancel(boolean c)throws ScannerIOException{
    jsane.setCancel(this,c);
  }

  void negotiateOptions(SaneDevice source){     
    ((SaneIOMetadata)metadata).setDevice(source);
    fireListenerUpdate(metadata.NEGOTIATE);
    source.setCancel(metadata.getCancel());              // application might want us to abort the scan
  }

  public void setImage(BufferedImage image){             // also called from SaneAcquirePanel
    if(image!=null){
      metadata.setImage(image);                          // tell listeners we have a new image
      fireListenerUpdate(metadata.ACQUIRED);
    }
  }

  void setState(SaneDevice source){
    metadata.setState(source.getState());
    ((SaneIOMetadata)metadata).setDevice(source);
    fireListenerUpdate(metadata.STATECHANGE);
  }

  void signalException(Exception e){
    metadata.setException(e);
    fireListenerUpdate(metadata.EXCEPTION);
  }

  public JComponent getScanGUI(){
    return new SanePanel(this,4);
  }

  public JComponent getScanGUI(int mode){
    return new SanePanel(this,mode);
  }

  static public Scanner getDevice(){
    SaneScanner ss=new SaneScanner();
    return ss;
  }
}

