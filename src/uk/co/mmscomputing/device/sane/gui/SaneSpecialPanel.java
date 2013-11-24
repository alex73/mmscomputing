package uk.co.mmscomputing.device.sane.gui;

import java.util.HashMap;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;
import uk.co.mmscomputing.device.sane.option.*;

public class SaneSpecialPanel extends JPanel{

  static private String adfmodestr  = jsane.getResource("gui.SaneSpecialPanel.adfmodestr");
  static private String adfcountstr = jsane.getResource("gui.SaneSpecialPanel.adfcountstr");

  private HashMap    options=null;
  private SaneDevice scanner;

  public SaneSpecialPanel(SaneDevice scanner,HashMap options){
    this.options=options;
    this.scanner=scanner;

    setLayout(new BorderLayout());

    JTabbedPane      tp   = new JTabbedPane();
    SaneNoDocsPanel  sndp = new SaneNoDocsPanel(scanner);
    SaneMaxDocsPanel smdp = new SaneMaxDocsPanel(scanner);

    tp.insertTab(adfmodestr,null,sndp,null,0);
    tp.insertTab(adfcountstr,null,smdp,null,0);
    add(tp,BorderLayout.CENTER);
  }
}