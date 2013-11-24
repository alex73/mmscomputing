package uk.co.mmscomputing.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import javax.swing.*;

import uk.co.mmscomputing.util.log.LogBook;

abstract public class UtilMainApp extends JApplet{

  private Properties properties=new Properties();
  private File       propertiesFile;
  private JFrame     frame = null;

  public UtilMainApp(){
    super();
    frame = null;
  }

  public UtilMainApp(String title, String[] argv){    
    JFrame.setDefaultLookAndFeelDecorated(true);

    frame=new JFrame(title);
//    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        stop();System.exit(0);
      }
    });

    init();
    start();
  }

  protected boolean isApplet(){ return frame==null;}

  protected void setFrameSize(JFrame frame, Rectangle bounds){
    frame.setSize(bounds.width*4/5,bounds.height*4/5);
/*
    frame.pack();

    int w = frame.getBounds().width;
    if(bounds.width<w){w=bounds.width*95/100;}
    int h = frame.getBounds().height;
    if(bounds.height<h){h=bounds.height*95/100;}

    frame.setSize(w,h);
*/
  }

  abstract protected JPanel getCenterPanel(Properties properties)throws Exception;

  public void createGUI(){
    try{
      LogBook log=new LogBook(false);

      Runtime rt=Runtime.getRuntime();
      System.out.println("Runtime Total Memory: "+(rt.totalMemory()/(1024*1024))+" MB");
      System.out.println("Runtime Max   Memory: "+(rt.maxMemory()/(1024*1024))+" MB");

      String s=System.getProperty("java.home");
      System.out.println("java directory: "+s);

      String classname=getClass().getName();
      String filename =classname.substring(0,classname.lastIndexOf('.'))+".properties.txt";

      String userdir=System.getProperty("user.dir");
      System.out.println("current directory: "+userdir);

      String userhome=System.getProperty("user.home");
      System.out.println("user directory: "+userhome);

      File   parent   =new File(userhome,"mmsc");
      try{   
        parent.mkdirs();
        propertiesFile=new File(parent.getAbsolutePath(),filename);
      }catch(Exception e){
        System.out.println("9\bCould not create directory:\n\t"+parent.getAbsolutePath()+"\n\t"+e);
        propertiesFile=new File(filename);
      }

      System.out.println("properties file: "+propertiesFile.getAbsolutePath());

      if(propertiesFile.exists()){properties.load(new FileInputStream(propertiesFile));}


      JTabbedPane tp=new JTabbedPane();

      String mainapptitle = properties.getProperty(getClass().getName()+".mainapp.title");
      if(mainapptitle==null){mainapptitle="MainApp";}
      JPanel centerPanel=getCenterPanel(properties);
      tp.addTab(mainapptitle,centerPanel);

      String logtitle = properties.getProperty(getClass().getName()+".log.title");
      if(logtitle==null){logtitle="Log";}
      tp.addTab("Log",log);

      Container cp=this.getContentPane();
      cp.setLayout(new BorderLayout());
      cp.add(tp,BorderLayout.CENTER);

      if(frame!=null){
        frame.getContentPane().add(this);

        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        setFrameSize(frame,ge.getMaximumWindowBounds());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".createGUI:\n\tCould not create GUI\n\t"+e.getMessage());
      e.printStackTrace();
    }
  }

  public void init(){
    try{    
//      javax.swing.SwingUtilities.invokeAndWait(
      javax.swing.SwingUtilities.invokeLater(
        new Runnable(){
          public void run(){
            createGUI();        
          }
        }
      );
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".init:\n\tCould not create GUI\n\t"+e.getMessage());
      e.printStackTrace();
    }
  }

  public void stop(){
    try{
      properties.store(new FileOutputStream(propertiesFile),propertiesFile.getAbsolutePath());
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".stop:\n\tCould not save properties\n\t"+e.getMessage());
      e.printStackTrace();
    }
    super.stop();
  }
}