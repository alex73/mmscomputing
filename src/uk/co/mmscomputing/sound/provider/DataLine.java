package uk.co.mmscomputing.sound.provider;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.sound.*;

public abstract class DataLine implements javax.sound.sampled.DataLine{

  protected DataLine.Info     info = null;

  protected boolean isopen=false;
  protected boolean isactive=false;
  protected boolean isrunning=false;
  protected long    position=0;

  private   Vector  listeners=null;


  public DataLine(){
    info=new DataLine.Info(getClass(),Mixer.format,Mixer.bufSize);
    listeners=new Vector();
  }

// Line interface

  public Line.Info getLineInfo(){return info;}

  public void open()throws LineUnavailableException{
    if(isopen){ throw new IllegalStateException();}
    isopen=true;
    fireListenerUpdate(LineEvent.Type.OPEN);
  }

  public boolean   isOpen(){ return isopen;}

  public void close(){
    isrunning=false;
    isactive=false;
    isopen=false;
    fireListenerUpdate(LineEvent.Type.CLOSE);
  }

  public Control[] getControls(){			//	no controls available
    return new Control[0];
  }

  public boolean isControlSupported(Control.Type control){
    return false;
  }

  public Control getControl(Control.Type control){
    throw new IllegalArgumentException();
  }

  public void addLineListener(LineListener listener){
    listeners.add(listener);
  }

  public void removeLineListener(LineListener listener){
    listeners.remove(listener);
  }

// DataLine Interface

  public void drain(){
    if(!isOpen()){ return; }
  }

  public void flush(){		//	discard buffers
  }

  public void start(){
    if(isactive){ return;}
    isactive=true;
    fireListenerUpdate(LineEvent.Type.START);
  }

  public void stop(){
    if(!isactive){ return;}
    isactive=false;
    fireListenerUpdate(LineEvent.Type.STOP);
  }

  public AudioFormat getFormat(){return Mixer.format;}
  public int available(){return 0;}

  public int getBufferSize(){return Mixer.bufSize;}
  public int getFramePosition(){return (int)position;}
  public long getLongFramePosition(){return position;}
  public float getLevel(){return 1.0f;}

  public long getMicrosecondPosition(){return position;}
  public boolean isActive(){return isactive;}
  public boolean isRunning(){return isrunning;}

  protected void fireListenerUpdate(LineEvent.Type type){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      LineListener listener=(LineListener)e.nextElement();
      listener.update(new LineEvent(this,type,position));
    }
  }

// DataLine

  protected SpeakerThread    speaker=null;

  void setSpeakerThread(SpeakerThread speaker){this.speaker=speaker;}
}