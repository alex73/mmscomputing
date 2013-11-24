package uk.co.mmscomputing.sound.provider;

import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;

import uk.co.mmscomputing.concurrent.*;

public class Mixer implements javax.sound.sampled.Mixer{

  static final  AudioFormat       format = new AudioFormat(8000,16,1,true,false);
//  static final  AudioFormat       format = new AudioFormat(44100,16,2,true,false);             

  static final  int               bufSize=512;
  static final  int               MAXLINES=32;

  static private boolean          isopen=false;
  static private Line.Info  	    lineinfo=null;
  static private Mixer.Info 	    mixerinfo=null;

  static private DataLine[]       targets=null;               // in
  static private DataLine[]       sources=null;               // out
  static private Line.Info[]      targetInfos=null;
  static private Line.Info[]      sourceInfos=null;

  static private Control[]        controls=new Control[0];    // no controls available
  static private Vector           listeners=null;
  static private long             position=0;

  public Mixer(){
    super();
  }

// Line interface

  public Line.Info getLineInfo(){
    return lineinfo;
  }

  public void open(){
    isopen=true;
    fireListenerUpdate(LineEvent.Type.OPEN);
  }

  public void close(){
    isopen=false;
    fireListenerUpdate(LineEvent.Type.CLOSE);
  }

  public boolean isOpen(){
    return isopen;
  }

  public Control[] getControls(){			
    return controls;
  }

  public boolean isControlSupported(Control.Type control){
    for(int i=0; i<controls.length; i++){
      if(controls[i].getType().equals(control)){
        return true;
      }
    }
    return false;
  }

  public Control getControl(Control.Type control){
    for(int i=0; i<controls.length; i++){
      if(controls[i].getType().equals(control)){
        return controls[i];
      }
    }
    throw new IllegalArgumentException(getClass().getName()+".getControl:\n\tIllegal Argument.\n\t"+control);
  }

  public void addLineListener(LineListener listener){
    listeners.add(listener);
  }

  public void removeLineListener(LineListener listener){
    listeners.remove(listener);
  }

  public void fireListenerUpdate(LineEvent.Type type){
    for(Enumeration e = listeners.elements(); e.hasMoreElements() ;){
      LineListener listener=(LineListener)e.nextElement();
      listener.update(new LineEvent(this,type,position));
    }
  }

// Mixer Interface

  public javax.sound.sampled.Mixer.Info getMixerInfo(){
    return mixerinfo;
  }

  public Line.Info[] getSourceLineInfo(){
    return sourceInfos;
  }

  public Line.Info[] getTargetLineInfo(){
    return targetInfos;
  }

  public Line.Info[] getSourceLineInfo(Line.Info info){
    for(int i=0; i<sourceInfos.length; i++){
      if(info.matches(sourceInfos[i])){
        return sourceInfos;
      }
    }
    return new Line.Info[0];
  }

  public Line.Info[] getTargetLineInfo(Line.Info info){
    for(int i=0; i<targetInfos.length; i++){
      if(info.matches(targetInfos[i])){
        return targetInfos;
      }
    }
    return new Line.Info[0];
  }

  public boolean isLineSupported(Line.Info info){
    for(int i=0; i<sourceInfos.length; i++){
      if(info.matches(sourceInfos[i])){
        return true;
      }
    }
    for(int i=0; i<targetInfos.length; i++){
      if(info.matches(targetInfos[i])){
        return true;
      }
    }
    return false;
  }

  public Line getLine(Line.Info info)throws LineUnavailableException{
    for(int i=0; i<targetInfos.length; i++){
      if(info.matches(targetInfos[i])){
        DataLine line=targets[i];
        if(!line.isOpen()){
          return line;
        }
      }
    }
    for(int i=0; i<sourceInfos.length; i++){
      if(info.matches(sourceInfos[i])){
        DataLine line=sources[i];
        if(!line.isOpen()){
          return line;
        }
      }
    }
    throw new LineUnavailableException(getClass().getName()+".getLine:\n\tLine Unavailable\n\t"+info);
  }

  public int getMaxLines(Line.Info info){
    int maxlines=0;
    for(int i=0; i<targetInfos.length; i++){
      if(info.matches(targetInfos[i])){maxlines++;}
    }
    for(int i=0; i<sourceInfos.length; i++){
      if(info.matches(sourceInfos[i])){maxlines++;}
    }
    return maxlines;
  }

  public Line[] getSourceLines(){
    return sources;
  }

  public Line[] getTargetLines(){
    return targets;
  }

  public void synchronize(Line[] lines,boolean maintainSync){
    throw new IllegalArgumentException();
  }

  public void unsynchronize(Line[] lines){
    throw new IllegalArgumentException();
  }

  public boolean isSynchronizationSupported(Line[] lines,boolean maintainSync){
    return false;
  }

  static public javax.sound.sampled.Mixer.Info getMixerInfoDesc(){
    return mixerinfo;
  }

  static{
    mixerinfo=new Info(
      "uk.co.mmscomputing.sound.provider.Mixer",
      "mm's computing",
      "Virtual mixer offering multiple default input/output lines.",
      "2005-03-12"
    );
    lineinfo=new Line.Info(Mixer.class);

//    String osname=System.getProperty("os.name");java.runtime.version
    String javaversion=System.getProperty("java.runtime.version");// does not work with anything before 1.5
//    System.out.println("java.runtime.version : "+javaversion);
//    if(javaversion.startsWith("1.5")/*||javaversion.startsWith("1.4")*/){
      sources=new DataLine[MAXLINES<<1];
      sourceInfos=new Line.Info[MAXLINES<<1];
      for(int i=0;i<MAXLINES;i++){
        sources[i]=new SourceDataLine();
        sourceInfos[i]=sources[i].getLineInfo();
      }
      for(int i=MAXLINES;i<(MAXLINES<<1);i++){
        sources[i]=new Clip();
        sourceInfos[i]=sources[i].getLineInfo();
      }
      try{
        new SpeakerThread(sources).start();
      }catch(Exception e){
        e.printStackTrace();
        sources=new DataLine[0];
        sourceInfos=new Line.Info[0];
      }

      targets=new DataLine[MAXLINES];
      targetInfos=new Line.Info[MAXLINES];
      for(int i=0;i<MAXLINES;i++){
        targets[i]=new TargetDataLine();
        targetInfos[i]=targets[i].getLineInfo();
      }
      try{
        new MicrophoneThread(targets).start();
      }catch(Exception e){
        e.printStackTrace();
        targets=new DataLine[0];
        targetInfos=new Line.Info[0];
      }
/*
    }else{                                  
      sources=new DataLine[0];
      sourceInfos=new Line.Info[0];
      targets=new DataLine[0];
      targetInfos=new Line.Info[0];
    }
*/
  }

  /*
  Info class
  */

  static protected class Info extends javax.sound.sampled.Mixer.Info{

    protected Info(String name,String vendor,String description,String version){
      super(name,vendor,description,version);
    }

  }

}