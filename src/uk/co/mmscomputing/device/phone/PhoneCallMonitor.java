package uk.co.mmscomputing.device.phone;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.sound.sampled.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.sound.*;

public class PhoneCallMonitor extends PhoneCallSaver implements WindowListener{

  private Semaphore  blocker,pickup;
  private boolean    active=false;
  private JFrame     gui=null;
  private Recorder   recorder;

  private SourceDataLine speaker=null;
  private TargetDataLine microphone=null;

  public PhoneCallMonitor(){
    pickup  = new Semaphore(0,true);
    blocker = new Semaphore(0,true);
  }

  public void run(String local,String remote,InputStream pin,OutputStream pout){
    try{
      microphone=SoundMixerEnumerator.getInputLine(pcmformat,DefaultPhonePCMBlockSize);
    }catch(LineUnavailableException lue){
      System.out.println("\3b"+getClass().getName()+".<init>:\n\tCould not create microphone input stream.\n\t"+lue);
    }
    try{
      speaker=SoundMixerEnumerator.getOutputLine(pcmformat,DefaultPhonePCMBlockSize);
    }catch(LineUnavailableException lue){
      microphone.close();
      System.out.println("\3b"+getClass().getName()+".<init>:\n\tCould not create speaker output stream.\n\t"+lue);
    }
    if((speaker==null)||(microphone==null)){super.run(local,remote,pin,pout);return;}
    try{
      recorder=new Recorder(pout);
      recorder.start();
      gui=openMonitorGUI("Remote "+remote+" Local "+local);
      pin.skip(pin.available());  // waste whatever we couldn't process in time
      super.run(local,remote,new PhoneCallMonitorInputStream(pin),pout);
      recorder.interrupt();gui.dispose();
    }catch(Exception e){
      System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
      e.printStackTrace();
    }finally{
      deactivate();
      microphone.close();
      speaker.close();
      if(gui!=null){gui.dispose();}
    }
  }

  public void pickup(){
    pickup.release();
  }

  public void activate(){
    active=true;
    microphone.flush();
    speaker.flush();
    speaker.start();
    blocker.release();
    microphone.start();
    microphone.flush();
  }

  public void deactivate(){
    active=false;
    microphone.stop();
    microphone.flush();

    speaker.stop();
    speaker.flush();
  }

  public void windowOpened(WindowEvent e){      activate();}
  public void windowClosing(WindowEvent e){     stopRunning();}
  public void windowClosed(WindowEvent e){      stopRunning();}
  public void windowIconified(WindowEvent e){   deactivate();}
  public void windowDeiconified(WindowEvent e){ activate();}
  public void windowDeactivated(WindowEvent e){ deactivate();}
  public void windowActivated(WindowEvent e){   activate();}

  private class Recorder extends Thread{        // input: microphone ouput: telephone

    private OutputStream pout;
    
    public Recorder(OutputStream pout){
      this.pout=pout;
    }

    public void run(){
      int    count=-1;
      byte[] buffer = new byte[DefaultPhonePCMBlockSize];
      try{
        pickup.acquire();                            // wait until call gets picked up
        if(isrunning){
          stopPlaying();                             // stop parents player thread
          activate();
          do{
            blocker.acquire();                       // wait until call becomes active;
            if(isrunning){
              try{                                   // give player time to finish and phone output some time to send
                new Semaphore(0,true).tryAcquire(500,TimeUnit.MILLISECONDS);
              }catch(InterruptedException ie){}
              microphone.flush();
              while(isrunning&&active){
                count=microphone.read(buffer,0,DefaultPhonePCMBlockSize);
                if(count==-1){break;}
                pout.write(buffer,0,count);          // pipe out microphone input
              }
            }
          }while(isrunning&&(count>=0));             // finish when microphone stream was closed
        }
      }catch(InterruptedException ie){
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\tCould not create answer thread."+e);
      }
    }
  }

  class PhoneCallMonitorInputStream extends FilterInputStream{

    public PhoneCallMonitorInputStream(InputStream in){
      super(in);
    }

    public int read()throws IOException{                // we don't use this
      IOException ioe= new IOException(getClass().getName()+".read:\n\tInternal Error.");
      ioe.printStackTrace();
      throw ioe;
    }

    public int read(byte[] b, int off, int len)throws IOException{
      while(in.available()>len){in.read(b,off,len);}    // couldn't process in time, drop data
      len=in.read(b,off,len);
      if(len==-1){return -1;}
      if(isrunning&&active){speaker.write(b,off,len);}
      return len;
    }
  }

  private class MonitorGUI extends JComponent{

    private PhoneCallMonitor pcm;
    private JTextPane        text=new JTextPane();
    private JButton          button;

    public MonitorGUI(final PhoneCallMonitor pcm,String title){
      this.pcm=pcm;

      text.setFont(new Font("Courier",Font.PLAIN,12));
      text.setText(title);

      JPanel buttons=new JPanel();
      buttons.setLayout(new GridLayout(1,0));

      button=new JButton(
        new AbstractAction("Pick Up"/*,new JarImageIcon(getClass(),"32x32/play.png")*/){
          public void actionPerformed(ActionEvent ev){ 
            button.setEnabled(false);
            pcm.pickup();
          }
        }
      );
      buttons.add(button);

      setLayout(new BorderLayout());
      add(new JScrollPane(text),BorderLayout.CENTER);
      add(buttons,BorderLayout.SOUTH);
    }
  }

  JFrame openMonitorGUI(String title){
    try{
      MonitorGUI gui=new MonitorGUI(this,title);

      JFrame frame=new JFrame(title);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.addWindowListener(this);
      frame.getContentPane().add(gui);

      GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle r=ge.getMaximumWindowBounds();
      frame.setSize(400,200);
      frame.setLocationRelativeTo(null);
      frame.pack();
      frame.setVisible(true);
      return frame;
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+"\n\t"+e);
      return null;
    }
  }

  static{
    try{
      SoundMixer mixer=SoundMixerEnumerator.getMixerByVendor("mm's computing");
      System.out.println("Mixer "+mixer.getMixerInfo()+" is available");
    }catch(Exception e){
      System.out.println("9\b"+e.getMessage());
    }
  }
}