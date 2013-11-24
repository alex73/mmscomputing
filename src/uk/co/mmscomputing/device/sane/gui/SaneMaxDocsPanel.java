package uk.co.mmscomputing.device.sane.gui;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;
import uk.co.mmscomputing.device.sane.gui.*;
import uk.co.mmscomputing.device.sane.option.DescriptorPanel;

public class SaneMaxDocsPanel extends JPanel implements SaneConstants, DocumentListener{

  static private String adfcountdescstr = jsane.getResource("gui.SaneMaxDocsPanel.adfcountdescstr");

  protected SaneDevice scanner;
  protected JTextField tf;

  public SaneMaxDocsPanel(SaneDevice scanner){
    this.scanner=scanner;

    setLayout(new BorderLayout());

    tf=new JTextField(10);    
    tf.setHorizontalAlignment(SwingConstants.RIGHT);
    tf.setText(""+scanner.getADFCount());
    Document td=tf.getDocument();
    td.addDocumentListener(this);

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    p.add(tf,BorderLayout.CENTER);

    p.setBorder(new TitledBorder(DescriptorPanel.valstr));
    add(p,BorderLayout.NORTH);

    add(newDescriptionPanel(adfcountdescstr),BorderLayout.CENTER);
  }

  private JComponent newDescriptionPanel(String desc){
    JTextArea ta=new JTextArea(desc);
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    JScrollPane sp=new JScrollPane(ta);
    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    p.setBorder(new TitledBorder(DescriptorPanel.descstr));
    p.add(sp);
    return p;
  }

  public void changedUpdate(DocumentEvent e){ commit(e);}
  public void insertUpdate(DocumentEvent e){  commit(e);}
  public void removeUpdate(DocumentEvent e){  commit(e);}

  protected void commit(DocumentEvent de){
    int count=-1;

    Document d=de.getDocument();
    try{
      String value=d.getText(d.getStartPosition().getOffset(),d.getEndPosition().getOffset()).trim();
      count=Integer.parseInt(value);
      if(count<1){ throw new IllegalArgumentException();}
    }catch(Exception ex){
      count=-1;
      try{
        d.remove(0,d.getLength());
      }catch(Exception ble){}
    }
    if(count>1){scanner.setADFMode(true);}
    scanner.setADFCount(count);
  }
}
