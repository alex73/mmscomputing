package uk.co.mmscomputing.sound;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.concurrent.TimeUnit;
import uk.co.mmscomputing.concurrent.Semaphore;
import uk.co.mmscomputing.util.JarImageIcon;

public class SoundPlayer extends JComponent implements WindowListener{

  static AudioFormat   format = new AudioFormat(8000,16,1,true,false);             // PCM 16 bit mono 8000Hz
//  static AudioFormat format = new AudioFormat(44100,16,2,true,false);             

  protected AudioInputStream in;
  protected Clip             clip=null;

  protected JButton          playButton;
  protected JButton          pauseButton;
  protected JButton          stopButton;
  protected JButton          rewindButton;

  private JSlider            slider;
  private boolean            mousedown=false;

  protected JTextPane        text=new JTextPane();

  private   JPanel           progress=new JPanel();
  private   TitledBorder     tb=new TitledBorder("Progress");

  private   Semaphore        blocker=new Semaphore(0,true);

  public SoundPlayer(AudioInputStream input)throws IOException, LineUnavailableException{
    slider=new JSlider(JSlider.HORIZONTAL,0,100,0);
    slider.setBorder(new EmptyBorder(5,10,5,10));

    MIA mia=new MIA();
    slider.addMouseListener(mia);
    slider.addMouseMotionListener(mia);

    slider.setMinorTickSpacing(5);
    slider.setMajorTickSpacing(10);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    progress.setLayout(new BorderLayout());
    progress.setBorder(tb);
    progress.add(slider,BorderLayout.CENTER);    

    text.setFont(new Font("Courier",Font.PLAIN,12));

    JPanel buttons=new JPanel();
    buttons.setLayout(new GridLayout(1,0));
    addButtons(buttons);

    setLayout(new BorderLayout());
    add(progress,BorderLayout.NORTH);
    add(new JScrollPane(text),BorderLayout.CENTER);
    add(buttons,BorderLayout.SOUTH);

    setClip(input);
  }

  protected void addButtons(JPanel buttons){
    playButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/play.png")){
        public void actionPerformed(ActionEvent ev){ play();}
      }
    );
    buttons.add(playButton);
    pauseButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/pause.png")){ 
        public void actionPerformed(ActionEvent ev){ pause();}
      }
    );
    buttons.add(pauseButton);
    stopButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/stop.png")){ 
        public void actionPerformed(ActionEvent ev){ stop();}
      }
    );
    buttons.add(stopButton);
    rewindButton=new JButton(
      new AbstractAction("",new JarImageIcon(getClass(),"32x32/rew.png")){
        public void actionPerformed(ActionEvent ev){ rewind();}
      }
    );
    buttons.add(rewindButton);
  }

  protected void setTimeText(double time){
    tb.setTitle("Progress ["+time+" sec]");
    progress.repaint();
  }

  protected void setClip(AudioInputStream in)throws IOException,LineUnavailableException{
    if(clip!=null){clip.close();}

    if(  in.getFormat().getEncoding().equals(AudioFormat.Encoding.ALAW)
      || in.getFormat().getEncoding().equals(AudioFormat.Encoding.ULAW)
    ){
      in=AudioSystem.getAudioInputStream(format,in);
    }
    DataLine.Info info=new DataLine.Info(Clip.class,in.getFormat());
    clip=(Clip)AudioSystem.getLine(info);
    clip.open(in);
    setTimeText(clip.getMicrosecondLength()/1000000.0);
    new Thread(){
      public void run(){
        try{
          while(clip.isOpen()){
            if(!mousedown){
              int value=(clip.getFramePosition()*100)/clip.getFrameLength();
              slider.setValue(value);
            }
            blocker.tryAcquire(200,TimeUnit.MILLISECONDS);
          }
        }catch(Exception e){e.printStackTrace();}
      }
    }.start();
  }

  protected AudioInputStream getAudioInputStream(){return in;}

  protected void play(){
    if(!clip.isActive()){
      clip.setFramePosition(clip.getFramePosition());
      clip.start();
    }
  }

  protected void pause(){
    if(clip.isActive()){
      clip.stop();
    }
  }

  protected void stop(){
    if(clip.isActive()){
      clip.stop();
    }
    clip.setFramePosition(0);
  }

  protected void rewind(){
    if(clip.isActive()){
      clip.stop();
      clip.setFramePosition(0);
      clip.start();
    }else{
      clip.setFramePosition(0);
    }
  }

  public void setText(String txt){
    text.setText(txt);
  }

  // start window listener methods

  public void windowOpened(WindowEvent e){play();}

  public void windowClosing(WindowEvent e){clip.stop();}
  public void windowClosed(WindowEvent e){clip.close();}

  public void windowIconified(WindowEvent e){pause();}
  public void windowDeiconified(WindowEvent e){play();}
  public void windowActivated(WindowEvent e){play();}
  public void windowDeactivated(WindowEvent e){pause();}

  // end window listener methods

  static public JFrame getDevice(String argv[]){
    SoundPlayer player;
    JFrame      frame=null;

    if((argv==null)||(argv.length!=3)){throw new IllegalArgumentException();}
    if(argv[0]==null){argv[0]="mmsc - SoundPlayer [2004-09-09]";}
    if(argv[1]==null){argv[1]="uk/co/mmscomputing/sounds/startmsg.wav";}

    if(argv[2]==null){argv[2]="Playing\n\t"+argv[1];}
    try{
      player=new SoundPlayer(AudioSystem.getAudioInputStream(new File(argv[1])));
      player.setText(argv[2]);

      frame=new JFrame(argv[0]);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.addWindowListener(player);
      frame.getContentPane().add(player);

      GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle r=ge.getMaximumWindowBounds();
      frame.setSize(400,200);
      frame.setLocationRelativeTo(null);

      frame.setVisible(true);
    }catch(Exception e){
      JOptionPane.showMessageDialog(null,e.getMessage(),"Soundplayer Exception",JOptionPane.ERROR_MESSAGE); 
      e.printStackTrace();
    }
    return frame;
  }

  public static void main(String[] argv){
    try{
//      System.err.println(new SoundMixerEnumerator().toString());

      String[] arg=new String[3];
      arg[0]="mmsc - Test SoundPlayer [2004-09-09]";
      arg[1]="uk/co/mmscomputing/sounds/startmsg.wav";
//      arg[1]="/home/mmsc/Documents/music/Grease/Track08-John Travolta-Greased Lightnin.wav";

      JFrame frame=SoundPlayer.getDevice(arg);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  // MouseInputAdapter

  class MIA extends MouseInputAdapter{

    public void mouseClicked(MouseEvent e){}

    public void mousePressed(MouseEvent e){
      mousedown=true;
      pause();
    }

    public void mouseReleased(MouseEvent e){
      int value=slider.getValue()*clip.getFrameLength()/100;
      clip.setFramePosition(value);
      clip.start();
      mousedown=false;
    }

    public void mouseMoved(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
  }

}