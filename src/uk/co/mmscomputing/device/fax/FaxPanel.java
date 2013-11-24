package uk.co.mmscomputing.device.fax;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import javax.swing.border.*;

import uk.co.mmscomputing.util.*;

public class FaxPanel extends JComponent implements FaxConstants,FaxSenderListener {

  private FaxSender  sender;
  private JButton    sendbutton;
  private JButton    selbutton;
  private Properties properties;

  public FaxPanel(Properties properties,FaxSender sender){
    this.properties=properties;
    this.sender=sender;
    setLayout(new GridLayout(2,1));
    sendbutton=new JButton("send",new JarImageIcon(getClass(),"32x32/fax.png"));
    sendbutton.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "send"));
//    sendbutton.setEnabled(false);
    add(sendbutton);
    selbutton=new JButton("select",new JarImageIcon(getClass(),"32x32/list.png"));
    selbutton.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "select"));
//    add(selbutton);
    sender.addListener(this);
  }

  public void send(){
//    sendbutton.setEnabled(false);
//    selbutton.setEnabled(false);
    sender.setMetadata(properties);	      
  }

  public void select(){		      	//	select sane data source
//    sendbutton.setEnabled(false);
//    selbutton.setEnabled(false);
  }

  public void update(FaxSenderMetadata.Type type, FaxSenderMetadata md){
    try{
      if(type.equals(FaxSenderMetadata.STATE)){
        if(md.isState(faxRinging)){
          int    waitforsecs = md.getTimeOut();
          String rno         = md.getRemoteNo();

          md.setTimeOut(waitforsecs,new RequestProgressMonitor(rno,"",waitforsecs*1000));
        }
      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".update:\n\t"+e);
      e.printStackTrace();
    }
  }
}

class RequestProgressMonitor extends uk.co.mmscomputing.concurrent.Timer
    implements uk.co.mmscomputing.concurrent.TimerListener{

  JFrame              dialog;
  JProgressBar        progressBar;

  public RequestProgressMonitor(String remote,String name,int timeout){
    super(timeout);
    setDelay(500);
    setListener(this);
    dialog=getDialog(remote,name);
  }

  public JFrame getDialog(String remote,String name){
    JPanel panel=new JPanel();

    JPanel q=new JPanel();
    q.setLayout(new BorderLayout());
    Border border = q.getBorder();
    Border margin = new EmptyBorder(10,10,5,10);
    q.setBorder(new CompoundBorder(border, margin));
    q.add(new JLabel("Fax Connection Request :"),BorderLayout.NORTH);

    JLabel img=new JLabel(new JarImageIcon(getClass(),"32x32/fax.png"));
    img.setBorder(new EmptyBorder(10,10,10,10));
    q.add(img,BorderLayout.WEST);

//    JLabel msg=new JLabel("<html><i>"+name+"</i><br>"+remote+"</html>");
    JLabel msg=new JLabel("Fax Number : "+remote);
    msg.setBorder(new EmptyBorder(10,10,10,10));
    q.add(msg,BorderLayout.CENTER);

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());

    progressBar = new JProgressBar(0,timeout);
    progressBar.setValue(0);
    progressBar.setIndeterminate(false);
    progressBar.setStringPainted(true);

    progressBar.setBorder(new EmptyBorder(5,0,5,0));
    p.add(progressBar,BorderLayout.NORTH);


    JPanel buttons=new JPanel();
    buttons.setLayout(new GridLayout(0,3));
    buttons.add(new JPanel());

    JButton button=new JButton(
      new AbstractAction("cancel"){
        public void actionPerformed(ActionEvent ev){ 
          release();
        }
      }
    );
    buttons.add(button);
    buttons.add(new JPanel());

    p.add(buttons,BorderLayout.CENTER);
    q.add(p,BorderLayout.SOUTH);

    panel.setLayout(new BorderLayout());
    panel.setOpaque(true);
    panel.add(q,BorderLayout.NORTH);

    JFrame dialog = new JFrame("Progress...");
    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    dialog.setContentPane(panel);
    dialog.pack();

    return dialog;
  }

  public void begin(int timeleft){
    dialog.setLocationRelativeTo(null);
    dialog.toFront();
    dialog.setVisible(true);
  }

  public void tick(int timeleft){
    progressBar.setValue(timeout-timeleft);
    progressBar.setString("Pick up in "+timeleft+" ms");
  }

  public void end(int timeleft){
    dialog.dispose();
  }
}
