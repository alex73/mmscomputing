package uk.co.mmscomputing.device.sane.gui;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.device.scanner.*;
import uk.co.mmscomputing.device.sane.*;

public class SanePanel extends JComponent implements ScannerListener {

  Scanner scanner  =null;
  JButton acqbutton=null;
  JButton selbutton=null;
  JCheckBox guicheckbox=null;

  public SanePanel(Scanner scanner,int mode){
    this.scanner=scanner;

    String selectstr    = jsane.getResource("gui.SanePanel.selectstr");
    String acquirestr   = jsane.getResource("gui.SanePanel.acquirestr");
    String guienablestr = jsane.getResource("gui.SanePanel.guienablestr");

    switch(mode){
    case 0:
      acqbutton=new JButton(acquirestr);
      selbutton=new JButton(selectstr);
      setLayout(new GridLayout(0,1));
      break;
    case 1:
      acqbutton=new JButton(acquirestr);
      selbutton=new JButton(selectstr);
      setLayout(new GridLayout(1,0));
      break;
    case 2:
      acqbutton=new JButton(acquirestr,new JarImageIcon(getClass(),"16x16/scanner.png"));
      selbutton=new JButton(selectstr,new JarImageIcon(getClass(),"16x16/list.png"));
      setLayout(new GridLayout(0,1));
      break;
    case 3:
      acqbutton=new JButton(acquirestr,new JarImageIcon(getClass(),"16x16/scanner.png"));
      selbutton=new JButton(selectstr,new JarImageIcon(getClass(),"16x16/list.png"));
      setLayout(new GridLayout(1,0));
      break;
    case 4:
      acqbutton=new JButton(acquirestr,new JarImageIcon(getClass(),"32x32/scanner.png"));
      selbutton=new JButton(selectstr,new JarImageIcon(getClass(),"32x32/list.png"));
      setLayout(new GridLayout(0,1));
      break;
    case 5:
      acqbutton=new JButton(acquirestr,new JarImageIcon(getClass(),"32x32/scanner.png"));
      selbutton=new JButton(selectstr,new JarImageIcon(getClass(),"32x32/list.png"));
      setLayout(new GridLayout(1,0));
      break;
    }

    acqbutton.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "acquire"));
    add(acqbutton);
/*
    guicheckbox = new JCheckBox("Enable GUI");
    guicheckbox.setSelected(true);
    JPanel p=new JPanel();
    p.setBorder(BorderFactory.createEtchedBorder());
    p.add(guicheckbox);
    add(p);
*/
    guicheckbox = new JCheckBox(guienablestr);
    guicheckbox.setSelected(true);
    add(guicheckbox);

    selbutton.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "select"));
    add(selbutton);

    scanner.addListener(this);
  }

  public void acquire(){		                           // acquire BufferedImage from selected/default sane device
    try{
      scanner.acquire();
    }catch(ScannerIOException e){
      scanner.fireExceptionUpdate(e);
    }
  }

  public void select(){                   		         // select sane device
    try{
      scanner.select();
    }catch(ScannerIOException e){
      scanner.fireExceptionUpdate(e);
    }
  }

  public void update(ScannerIOMetadata.Type type, final ScannerIOMetadata md){
    if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      try{
        ScannerDevice source=md.getDevice();
        source.setShowUserInterface(guicheckbox.isSelected());      // then use checkbox value
      }catch(Exception e){}

    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      if(md.isFinished()){
        acqbutton.setEnabled(true);
        selbutton.setEnabled(true);
        guicheckbox.setEnabled(true);
      }else{
        acqbutton.setEnabled(false);
        selbutton.setEnabled(false);
        guicheckbox.setEnabled(false);
      }
    }
  }

}

