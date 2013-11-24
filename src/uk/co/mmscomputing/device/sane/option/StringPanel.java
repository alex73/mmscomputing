package uk.co.mmscomputing.device.sane.option;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;

public class StringPanel extends DescriptorPanel{
  public StringPanel(StringDesc od){
    super(od);

    JTextField tf=new JTextField(10);
    tf.setHorizontalAlignment(SwingConstants.RIGHT);
    tf.addActionListener(this);
    checkCapabilities(tf);
    tf.setText(od.getStringValue());
    addValuePanel(tf);

/*
    String valstr="Value";
    if(od.unit!=0){ valstr+=" in ["+SANE_UNIT[od.unit]+"]";}
    tf.setBorder(new TitledBorder(valstr));
    tf.setText(od.getStringValue());
    add(tf,BorderLayout.NORTH);     
*/
  }

  public void actionPerformed(ActionEvent e){
    try{
      JTextField tf=(JTextField)e.getSource();
      tf.setText(od.setStringValue(tf.getText()));
    }catch(SaneIOException sioe){
      sioe.printStackTrace();
    }
  }
}

