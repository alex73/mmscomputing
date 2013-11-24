package uk.co.mmscomputing.sound;

import java.io.*;
import javax.sound.sampled.*;

public class SoundMixer{

  static AudioFormat[] formats={
    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,16,2,4,8000,false),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,16,2,4,8000,false),

    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,16,2,4,8000,true),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,16,2,4,8000,true),

    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,16,1,2,8000,false),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,16,1,2,8000,false),

    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,16,1,2,8000,true),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,16,1,2,8000,true),

    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,8,2,2,8000,false),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,8,2,2,8000,false),

    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,8,1,1,8000,true),
    new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,8000,8,1,1,8000,true),

    new AudioFormat(AudioFormat.Encoding.ALAW,8000,8,1,1,8000,true),
    new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,true)
  };

  final private int        id;
  final private Mixer      mixer;
  final private Mixer.Info info;

  public SoundMixer(int id, Mixer.Info info){
    this.id=id;
    this.info=info;
    this.mixer=AudioSystem.getMixer(info);
  }

  public int getId(){ return id; }
  public String getName(){ return /*""+id+" "+*/mixer.getMixerInfo().getName(); };
  public Mixer.Info getMixerInfo(){return info;}

  public String toString(){
    Mixer.Info info=mixer.getMixerInfo();

    String s="\nMixer ["+id+"]";
    s+="\n\t Name: "+info.getName();
    s+="\n\t Desc: "+info.getDescription();
    s+="\n\t Ven : "+info.getVendor();
    s+="\n\t Ver : "+info.getVersion();
    s+="\n\t Str : "+info.toString();

    Line.Info[] infos=mixer.getSourceLineInfo();
    s+="\n\nSourceLine count : "+infos.length;
    for(int i=0;i<infos.length;i++){
      if(infos[i] instanceof DataLine.Info){
        s+="\n\t\tData Line Source ["+i+"]";
        s+="\n\t\t\t Str : "+infos[i].toString();
      }else if(infos[i] instanceof Port.Info){
        s+="\n\t\tPort Source ["+i+"]";
        s+="\n\t\t\t Name: "+((Port.Info)infos[i]).getName();
        s+="\n\t\t\t is Src: "+((Port.Info)infos[i]).isSource();
        s+="\n\t\t\t Str : "+infos[i].toString();
      }else/*if(infos[i]!=null)*/{
        s+="\n\t\tSource ["+i+"]";
        s+="\n\t\t\t Str : "+infos[i].toString();
      }
    }
    s+="\n\nOUTPUT\n";
    for(int i=0;i<formats.length;i++){
      try{
        SourceDataLine out=getOutputLine(formats[i]);
        out.close();
        s+="\n"+formats[i].toString();
      }catch(Exception e){
//        s+="\n"+e.getMessage();
      }
    }

    infos=mixer.getTargetLineInfo();
    s+="\n\nTargetLine count : "+infos.length;
    for(int i=0;i<infos.length;i++){
      if(infos[i] instanceof DataLine.Info){
        s+="\n\t\tData Line Target ["+i+"]";
        s+="\n\t\t\t Str : "+infos[i].toString();
      }else if(infos[i] instanceof Port.Info){
        s+="\n\t\tPort Target ["+i+"]";
        s+="\n\t\t\t Name: "+((Port.Info)infos[i]).getName();
        s+="\n\t\t\t is Src: "+((Port.Info)infos[i]).isSource();
        s+="\n\t\t\t Str : "+infos[i].toString();
      }else /*if(infos[i]!=null)*/{
        s+="\n\t\tTarget ["+i+"]";
        s+="\n\t\t\t Str : "+infos[i].toString();
      }
    }

    s+="\n\nINPUT\n";
    for(int i=0;i<formats.length;i++){
      try{
        TargetDataLine out=getInputLine(formats[i]);
        out.close();
        s+="\n"+formats[i].toString();
      }catch(Exception e){
//        s+="\n"+e.getMessage();
      }
    }

    return s;
  }

  public TargetDataLine getInputLine(AudioFormat format, int bufSize)throws LineUnavailableException{
    TargetDataLine in;

    DataLine.Info info=new DataLine.Info(TargetDataLine.class,format);
    in=(TargetDataLine)mixer.getLine(info);    
//    System.out.println("BufferSize "+in.getBufferSize());
    in.open(format,bufSize);
    return in;
  }

  public TargetDataLine getInputLine(AudioFormat format)throws LineUnavailableException{
    TargetDataLine in;

    DataLine.Info info=new DataLine.Info(TargetDataLine.class,format);
    in=(TargetDataLine)mixer.getLine(info);    
    in.open(format,in.getBufferSize());
    return in;
  }

  public SourceDataLine getOutputLine(AudioFormat format, int bufSize)throws LineUnavailableException{
    SourceDataLine out;

    DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
    out=(SourceDataLine)mixer.getLine(info);    
    out.open(format,bufSize);
    return out;
  }

  public SourceDataLine getOutputLine(AudioFormat format)throws LineUnavailableException{
    SourceDataLine out;

    DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
    out=(SourceDataLine)mixer.getLine(info);    
    out.open(format,out.getBufferSize());
    return out;
  }
/*
  static{
    try{
      System.err.println("LOAD uk.co.mmscomputing.sound.provider.Mixer");
      Class.forName("uk.co.mmscomputing.sound.provider.Mixer");
    }catch(ClassNotFoundException cnfe){
      System.err.println("uk.co.mmscomputing.sound.SoundMixer\n\t"+cnfe);
    }
  }
*/
}