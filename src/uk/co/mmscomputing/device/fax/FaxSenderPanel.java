package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

import uk.co.mmscomputing.util.*;

public class FaxSenderPanel extends JPanel implements FaxConstants{

  private FaxSenderMetadata md;
  private FaxSender         sender=null;
  private JDialog           dialog=null;       
  private JTabbedPane       tp=null;

  public FaxSenderPanel(FaxSender sender, FaxSenderMetadata md,Properties properties){
    this.sender=sender;      
    this.md=md;
    setLayout(new BorderLayout());

    tp=new JTabbedPane();
    tp.addTab("Properties",new FaxPropertiesPanel(false,true,properties));

    add(tp,BorderLayout.CENTER);


    JPanel bp=new JPanel();
    bp.setLayout(new GridLayout(1,2));

    JButton button;

    button=new JButton("send",new JarImageIcon(getClass(),"32x32/fax.png"));
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "send"));
    bp.add(button);
    
    button=new JButton("cancel");
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "cancel"));
    bp.add(button);
    
    add(bp,BorderLayout.SOUTH);
  }

  synchronized public void send(){
    /*
       Tell application to save fax file and set file name [md.setFile(faxfilename)]
    */
    sender.fireListenerUpdate(md.GETFILE);
    if(dialog!=null){ dialog.dispose();dialog=null;}
    String fn=md.getFile();
    if(fn!=null){
      new Thread(){
        public void run(){
          try{
            sender.send();
          }catch(Exception e){
            System.out.println("9\b"+getClass().getName()+"\n\tCould not send fax file.\n\t"+e.getMessage());
          }
        }
      }.start();
    }
  }

  public void cancel(){
    if(dialog!=null){ dialog.dispose();dialog=null;}
  }

  public void display(){
    try{
      dialog=new JDialog((Frame)null,"Fax Sender Panel",true);
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
