package uk.co.mmscomputing.device.sane;

import java.awt.*;

import uk.co.mmscomputing.device.scanner.*;

public class SaneIOMetadata extends ScannerIOMetadata implements SaneConstants{

  static public Type	SELECTCHANGE=new Type();
  static public Type	GETROI=new Type();
  static public Type	SETSIZE=new Type();

  private boolean     localOnly=false;
  private String      devicename="";
  private Rectangle   roi;

  private Parameters  parameters=new Parameters();

  // for progress bar

  private int     size=0;  // estimated picture size;
  private int     pos=0;   // current position in input stream

  synchronized public void setDeviceName(String devicename){this.devicename=devicename;}
  public String getDeviceName(){return devicename;}

  public void setSearchForLocalDevices(){this.localOnly=true;}
  public void setSearchForNetworkDevices(){ this.localOnly=false; }
  public boolean getSearchFlag(){ return localOnly;}

  public void setParameters(Parameters parameters){this.parameters=parameters;}
  public Parameters getParameters(){return parameters;}

  public String  getStateStr(){ return SANE_STATE[getState()];}

  public void setSize(int size){ this.size=size; }
  public int  getSize(){ return size;}
  public void setPos(int pos){ this.pos=pos; }
  public int  getPos(){ return pos;}

  public void setROI(Rectangle roi){ this.roi=roi;}
  public Rectangle getROI(){ return roi;}

  // only valid when state changes!

  private SaneDevice device=null;

         void          setDevice(SaneDevice device){this.device=device;}
  public ScannerDevice getDevice(){return device;}

  public boolean     isFinished(){return (getState()==SANE_STATE_EXIT);}
}