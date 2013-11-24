package uk.co.mmscomputing.device.phone;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class PhoneCallerGUI extends JComponent implements PhoneCallerListener {

  private PhoneCaller  caller;
  private JButton      callbutton;
  private Properties   properties;

  public PhoneCallerGUI(Properties properties,PhoneCaller caller){
    this.properties=properties;
    this.caller=caller;
    setLayout(new BorderLayout());
    callbutton=new JButton("call"/*,new JarImageIcon(getClass(),"32x32/fax.png")*/);
    callbutton.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "call"));
//    callbutton.setEnabled(false);
    caller.addListener(this);
    add(callbutton);
  }

  public void call(){
//    callbutton.setEnabled(false);
    caller.setMetadata(properties);	      
  }

  public void update(PhoneCallerMetadata.Type type, PhoneCallerMetadata md){
  }
}

