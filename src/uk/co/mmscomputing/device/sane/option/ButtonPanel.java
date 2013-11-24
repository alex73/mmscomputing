package uk.co.mmscomputing.device.sane.option;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.sane.*;

public class ButtonPanel extends DescriptorPanel{
  public ButtonPanel(ButtonDesc od){
    super(od);

    // Not tested.

    JButton button=new JButton("press ["+od.name+"]");
    button.addActionListener(this);
    checkCapabilities(button);
    addValuePanel(button);

/*
    button.setBorder(new TitledBorder("Value"));
    add(button,BorderLayout.NORTH);     
*/
  }

  public void actionPerformed(ActionEvent e){
    try{
      od.setWordValue(0,0);        // value doesn't matter
    }catch(SaneIOException sioe){
      sioe.printStackTrace();
    }
  }
}
