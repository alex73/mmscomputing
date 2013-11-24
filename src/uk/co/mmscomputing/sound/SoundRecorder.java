package uk.co.mmscomputing.sound;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.util.JarImageIcon;

public class SoundRecorder extends SoundPlayer{

  protected JButton          saveButton;
  protected JButton          recordButton;

  private   boolean          recording=false;
  private   boolean          pause=false;
  private   byte[]           buffer=new byte[0];
  private   File             file=new File("uk/co/mmscomputing/sounds/test.wav");

  public SoundRecorder()throws IOException, LineUnavailableException{
    super(new AudioInputStream(new ByteArrayInputStream(new byte[0]),format,0));
  }

  public SoundRecorder(AudioInputStream input)throws IOException, LineUnavailableException{
    super(input);
  }

  protected void addButtons(JPanel buttons){
    recordButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/rec.png")){
        public void actionPerformed(ActionEvent ev){ record();}
      }
    );
    buttons.add(recordButton);
    super.addButtons(buttons);
    saveButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/save.png")){
        public void actionPerformed(ActionEvent ev){ save();}
      }
    );
    buttons.add(saveButton);
  }

  private void recordX(){      
    try{
      playButton.setEnabled(false);
      rewindButton.setEnabled(false);
      saveButton.setEnabled(false);
      recordButton.setEnabled(false);

      recording=true;
      pause=false;
      ByteArrayOutputStream out=new ByteArrayOutputStream();

      TargetDataLine input=SoundMixerEnumerator.getInputLine(format,1024);     // get default sound input i.e. microphone
      input.start();
      AudioInputStream in=new AudioInputStream(input);

      int count, bytesWritten=0;
      buffer = new byte[1024];
      while(recording&&((count=in.read(buffer))!=-1)){
        if(!pause){
          out.write(buffer,0,count);
          bytesWritten+=count;
          setTimeText((bytesWritten/format.getFrameSize())/format.getFrameRate());
        }
      }
      in.close();
      out.flush();
      out.close();
      input.close();

      buffer=out.toByteArray();
      in=new AudioInputStream(new ByteArrayInputStream(buffer),format,buffer.length/format.getFrameSize());

      setClip(in);

      playButton.setEnabled(true);
      rewindButton.setEnabled(true);
      saveButton.setEnabled(true);
      recordButton.setEnabled(true);
    }catch(LineUnavailableException lue){
      lue.printStackTrace();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }finally{
      recording=false;
    }
  }

  protected void record(){      
    if(recording){
      if(pause){
        pause=false;
        recordButton.setEnabled(pause);
      }
    }else{
      stop();
      new Thread(){
        public void run(){
          recordX();
        }
      }.start();
    }
  }

  protected void pause(){
    if(recording){
      pause=true;
      recordButton.setEnabled(pause);
    }else{
      super.pause();
    }    
  }

  protected void stop(){
    if(recording){
      recording=false;
    }else{
      super.stop();
    }
  }

  class SRFF extends javax.swing.filechooser.FileFilter{
    protected ArrayList exts=new ArrayList();
   
    public SRFF(){
      addExtension("wav");
    }

    public void addExtension(String s){
      exts.add(s);
    }

    public boolean accept(File f){
      if(f.isDirectory()){
        return true;
      }else if(f.isFile()){
        Iterator it=exts.iterator();
        while(it.hasNext()){
          String ext=(String)it.next();
          if(f.getName().toLowerCase().endsWith("."+ext.toLowerCase())){
            return true;
          }
        }
      }
      return false;
    }

    public String getDescription(){
      return "Sound Files";
    }
  }

  protected void save(){
    File dir=file.isDirectory()?file:file.getParentFile();
    JFileChooser fc=new JFileChooser(dir);
    fc.setSelectedFile(file);
    fc.addChoosableFileFilter(new SRFF());
    int res=fc.showSaveDialog(null);
    if(res==JFileChooser.APPROVE_OPTION){
      file=new File(fc.getSelectedFile().getPath());
      try{
        AudioInputStream in=new AudioInputStream(new ByteArrayInputStream(buffer),format,buffer.length/format.getFrameSize());
        AudioSystem.write(in,AudioFileFormat.Type.WAVE,file);
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

  public void setFile(File file){
    this.file=file;
  }

  static public JFrame getDevice(String argv[]){
    SoundRecorder recorder;
    JFrame        frame=null;

    if((argv==null)||(argv.length!=3)){throw new IllegalArgumentException();}
    if(argv[0]==null){argv[0]="mmsc - SoundRecorder [2004-09-29]";}
    if(argv[1]==null){argv[1]="uk/co/mmscomputing/sounds/test2.wav";}
    if(argv[2]==null){argv[2]="Playing & recording\n\t"+argv[1];}
    try{
      File file=new File(argv[1]);
      if(file.exists()){
        recorder=new SoundRecorder(AudioSystem.getAudioInputStream(file));
      }else{
        recorder=new SoundRecorder();
      }
      recorder.setFile(file);
      recorder.setText(argv[2]);

      frame=new JFrame(argv[0]);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.addWindowListener(recorder);
      frame.getContentPane().add(recorder);

      GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle r=ge.getMaximumWindowBounds();
      frame.setSize(400,200);
      frame.setLocationRelativeTo(null);

      frame.setVisible(true);
    }catch(UnsupportedAudioFileException uafe){
      uafe.printStackTrace();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }catch(Exception e){
      e.printStackTrace();
    }
    return frame;
  }

  public static void main(String[] argv){
      String[] arg=new String[3];
      JFrame frame=SoundRecorder.getDevice(arg);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }
}