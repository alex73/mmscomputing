package uk.co.mmscomputing.sound;

import java.io.*;
import javax.sound.sampled.*;

public class SoundMixerEnumerator{

  static private SoundMixer[] mixers=null;

  static public int getNoOfSoundMixers(){ return mixers.length;}
  static public SoundMixer getMixer(int i){ return mixers[i];}

  static public SoundMixer getMixerByVendor(String vendor)throws IllegalArgumentException{
    for(int i=0;i<mixers.length;i++){
      Mixer.Info info=mixers[i].getMixerInfo();
      if(info.getVendor().equals(vendor)){
        return mixers[i];
      }
    }
    throw new IllegalArgumentException(
      "uk.co.mmscomputing.sound.SoundMixerEnumerator.getMixerByVendor(String vendor)\n\t"+
      "Mixer from vendor "+vendor+" not available."
    );
  }

  static public SourceDataLine getOutputLine(AudioFormat format, int bufsize)
    throws LineUnavailableException
  {
    SourceDataLine out;
    String emsg=null;
    for(int i=0;i<mixers.length;i++){
      try{
        out=mixers[i].getOutputLine(format, bufsize);
        return out;
      }catch(Exception e){
        emsg=e.getMessage();
      }
    }
    throw new LineUnavailableException(emsg);
  }

  static public SourceDataLine getOutputLine(AudioFormat format)
    throws LineUnavailableException
  {
    SourceDataLine out;
    String emsg=null;
    for(int i=0;i<mixers.length;i++){
      try{
        out=mixers[i].getOutputLine(format);
        return out;
      }catch(Exception e){
        emsg=e.getMessage();
      }
    }
    throw new LineUnavailableException(emsg);
  }

  static public TargetDataLine getInputLine(AudioFormat format)throws LineUnavailableException{
    TargetDataLine in;
    String emsg=null;
    for(int i=0;i<mixers.length;i++){
      try{
        in=mixers[i].getInputLine(format);
        return in;
      }catch(Exception e){
        emsg=e.getMessage();
      }
    }
    throw new LineUnavailableException(emsg);
  }

  static public TargetDataLine getInputLine(AudioFormat format, int bufsize)throws LineUnavailableException{
    TargetDataLine in;
    String emsg=null;
    for(int i=0;i<mixers.length;i++){
      try{
        in=mixers[i].getInputLine(format,bufsize);
        return in;
      }catch(Exception e){
        emsg=e.getMessage();
      }
    }
    throw new LineUnavailableException(emsg);
  }

  public String toString(){
    String s="\nMixer count: "+mixers.length;
    for(int i=0;i<mixers.length;i++){
      s+=mixers[i].toString();
    }
    return s;
  }

  static public void print(){
    String s="\nMixer count: "+mixers.length;
    for(int i=0;i<mixers.length;i++){
      s+=mixers[i].toString();
    }
    System.out.println(s);;
  }

  static{
    Mixer.Info[] infos=AudioSystem.getMixerInfo();
    mixers=new SoundMixer[infos.length];
    for(int i=0;i<infos.length;i++){
      mixers[i]=new SoundMixer(i,infos[i]);
    }
  }
}