package uk.co.mmscomputing.device.sane.option;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;

public class FixedPanel extends DescriptorPanel{

  JTextField[] tfs=null;

  public FixedPanel(FixedDesc od){
    super(od);

    JPanel p=new JPanel();
    tfs=new JTextField[od.size>>2];
    for(int i=0;i<tfs.length;i++){
      JTextField tf=new JTextField(10);    
      tf.setHorizontalAlignment(SwingConstants.RIGHT);
      tf.addActionListener(this);
      checkCapabilities(tf);
      tf.setText(od.getStringValue(i));
      p.add(tf);
      tfs[i]=tf;
    }
    addValuePanel(new JScrollPane(p));

/*
    String valstr="Value";
    if(od.unit!=0){ valstr+=" in ["+SANE_UNIT[od.unit]+"]";}
    p.setBorder(new TitledBorder(valstr));
    add(new JScrollPane(p),BorderLayout.NORTH);
*/
  }

  public void actionPerformed(ActionEvent e){
    JTextField tf=(JTextField)e.getSource();
    for(int i=0;i<tfs.length;i++){
      if(tfs[i]==tf){
        try{
          tf.setText(od.setStringValue(i,tf.getText()));
        }catch(SaneIOException sioe){
          sioe.printStackTrace();
        }
      }
    }
  }
}

