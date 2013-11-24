package uk.co.mmscomputing.device.sane.option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;

public class BoolPanel extends DescriptorPanel{

  public BoolPanel(Descriptor od){
    super(od);

    boolean selected=(od.getWordValue(0)==SANE_TRUE);
    JCheckBox cb=new JCheckBox(od.name,selected);
    cb.addActionListener(this);
    checkCapabilities(cb);
    addValuePanel(cb);

/*
    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    p.add(cb,BorderLayout.CENTER);
    String valstr="Value";
    if(od.unit!=0){ valstr+=" in ["+SANE_UNIT[od.unit]+"]";}
    p.setBorder(new TitledBorder(valstr));
    add(p,BorderLayout.NORTH);
*/
  }

  public void actionPerformed(ActionEvent e){
    JCheckBox cb=(JCheckBox)e.getSource();
    int value=(cb.isSelected())?SANE_TRUE:SANE_FALSE;
    try{
      value=od.setWordValue(0,value);
      cb.setSelected(value==SANE_TRUE);
    }catch(SaneIOException sioe){
      sioe.printStackTrace();
    }
  }

}

