package uk.co.mmscomputing.device.phone;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

import uk.co.mmscomputing.util.*;

public class PhoneCallerPanel extends JPanel implements PhoneConstants{

  private PhoneCallerMetadata md;
  private PhoneCaller         caller=null;
  private JDialog             dialog=null;       
  private JTabbedPane         tp=null;

  public PhoneCallerPanel(PhoneCaller caller, PhoneCallerMetadata md,Properties properties){
    this.caller=caller;      
    this.md=md;
    setLayout(new BorderLayout());

    tp=new JTabbedPane();
    tp.addTab("Properties",new PhonePropertiesPanel(false,true,properties));

    add(tp,BorderLayout.CENTER);


    JPanel bp=new JPanel();
    bp.setLayout(new GridLayout(1,2));

    JButton button;

    button=new JButton("call"/*,new JarImageIcon(getClass(),"32x32/fax.png")*/);
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "call"));
    bp.add(button);
    
    button=new JButton("cancel");
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "cancel"));
    bp.add(button);
    
    add(bp,BorderLayout.SOUTH);
  }

  synchronized public void call(){
    /*

    */
//    caller.fireListenerUpdate(md.GETFILE);
    if(dialog!=null){ dialog.dispose();dialog=null;}
//    String fn=md.getFile();
//    if(fn!=null){
      new Thread(){
        public void run(){
          try{
            caller.call();
          }catch(Exception e){
            System.out.println("9\b"+getClass().getName()+"\n\tCould not call.\n\t"+e.getMessage());
          }
        }
      }.start();
//    }
  }

  public void cancel(){
    if(dialog!=null){ dialog.dispose();dialog=null;}
  }

  public void display(){
    try{
      dialog=new JDialog((Frame)null,"Phone Caller Panel",true);
      dialog.setContentPane(this);
      GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
      dialog.setLocationRelativeTo(null);
      dialog.pack();
      dialog.setVisible(true);
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+"display:\n\t"+e.getMessage());
      e.printStackTrace();
    }
  }
}
