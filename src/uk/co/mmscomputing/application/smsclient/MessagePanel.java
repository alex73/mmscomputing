package uk.co.mmscomputing.application.smsclient;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.sms.SMSCapiSender;

public class MessagePanel extends JPanel implements ActionListener{

  private Properties properties;
  private String     pn;
  private JTextArea  smstext;

  public MessagePanel(Properties properties){
    super(new BorderLayout());

    this.properties=properties;
    
    pn=getClass().getPackage().getName();

    JPanel dp=new JPanel(new BorderLayout());
    dp.setBorder(BorderFactory.createTitledBorder("Destination Number"));
    new UtilTextField(dp,properties,pn+".destination","",null,20);

    smstext = new JTextArea(5,20);
    smstext.setEditable(true);

    JScrollPane scp = new JScrollPane(
        smstext,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
    );

    JPanel sp=new JPanel(new BorderLayout());
    sp.setBorder(BorderFactory.createTitledBorder("SMS Text"));
    sp.add(scp);

    JButton send=new JButton("send");
    send.setActionCommand("send");
    send.addActionListener(this);

    JPanel bp = new JPanel(new GridLayout());
    bp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    bp.add(new JPanel());bp.add(send);bp.add(new JPanel());

    JPanel cp=new JPanel(new BorderLayout());
    add(dp,BorderLayout.NORTH);
    add(sp,BorderLayout.CENTER);
    add(bp,BorderLayout.SOUTH);
  }

  public void actionPerformed(ActionEvent event){
    if(event.getActionCommand().equals("send")){
      try{
        String centre;
        centre = properties.getProperty(pn+".sendingservicecentre");
//        centre+= properties.getProperty(pn+".sendingservicecentre_subadddress");
        new Thread(new SMSCapiSender(
          centre,
          properties.getProperty(pn+".destination"),
          smstext.getText()
        )).start();
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".actionPerformed:\n\t"+e);
      }
    }
  } 
}
