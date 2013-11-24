package uk.co.mmscomputing.application.smsclient;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.util.UtilMainApp;
import uk.co.mmscomputing.device.capi.CapiSystem;

public class MainApp extends UtilMainApp{

  public MainApp(){super();}

  public MainApp(String title, String[] argv){
    super(title,argv);
  }

  protected JPanel getCenterPanel(Properties properties)throws Exception{
    String pn=getClass().getPackage().getName();

    JTabbedPane tp=new JTabbedPane();
    tp.addTab("Send SMS",new MessagePanel(properties));
    tp.addTab("Received SMSs",new ReceiverPanel(properties));
    tp.addTab("Configuration",new ConfigPanel(properties));
    tp.addTab("Capi",CapiSystem.getSystem().getGUI());

    JPanel p = new JPanel(new BorderLayout());
    p.add(tp);    
    return p;
  }

  protected void setFrameSize(JFrame frame, Rectangle bounds){
    frame.setSize(600,400);
  }

  public void stop(){
    try{
      CapiSystem.getSystem().close();
    }catch(Exception e){
      System.out.println(getClass().getName()+".stop:\n\t"+e);
    }
    super.stop();
  }

  public static void main(String[] argv){
    try{
      new MainApp("SMS Client [2006-08-08]", argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}