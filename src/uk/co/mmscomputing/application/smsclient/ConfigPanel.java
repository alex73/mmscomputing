package uk.co.mmscomputing.application.smsclient;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.sms.*;

// The default service centre here is : BT-Text service centre (1709400X)
// The 1470 is BT's way to ensure the signalling of the calling number even  
// if it is a number that is usually not displayed. 
// The calling number is needed for billing purposes!!!

// The default service centre here is : BT-Text service centre (080058752X)
// The BT-Text subaddress X is 9 by default.

public class ConfigPanel extends JPanel{

  private Properties    properties;

  public ConfigPanel(Properties properties){
    super(new BorderLayout());

    this.properties=properties;

    JPanel cp=new JPanel(new GridLayout(0,2));

    String pn=getClass().getPackage().getName();

    new UtilTextField(cp,properties,pn+".sendingservicecentre","147017094009","Sending SMS Centre Number",20);
//    new UtilTextField(cp,properties,pn+".sendingservicecentre_subadddress","9","Sending SMS Client Sub Address [0..9]",1);
    new UtilTextField(cp,properties,pn+".receivingservicecentre","08005875290","Receiving SMS Centre Number",20);
//    new UtilTextField(cp,properties,pn+".receivingservicecentre_subadddress","0","Receiving SMS Client Sub Address [0..9]",1);
    new UtilTextField(cp,properties,pn+".t10","3","T10",3);

    add(cp,BorderLayout.NORTH);  
  }
}
