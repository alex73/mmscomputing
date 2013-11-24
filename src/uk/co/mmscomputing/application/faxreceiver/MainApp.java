package uk.co.mmscomputing.application.faxreceiver;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.application.phone.*;
import uk.co.mmscomputing.application.imageviewer.*;

public class MainApp extends UtilMainApp implements FaxConstants{

  public MainApp(){super();}                     // need this in every applet application class

  public MainApp(String title, String[] argv){
    super(title,argv);
  }

  protected JPanel getCenterPanel(Properties properties)throws Exception{
    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    JTabbedPane tp=new JTabbedPane();

    PhoneBookImpl phonebook=new PhoneBookImpl(properties);
    phonebook.read();

    tp.addTab("Fax List",new FaxReceiverTab(properties,phonebook));
    tp.addTab("Fax Image",new FaxImageTab(properties));
    tp.addTab("Fax Properties",new FaxPropertiesPanel(true,false,properties));
    tp.addTab("Phone Book",new PhoneBookTab(properties,phonebook));
    try{
      tp.addTab("Capi System",uk.co.mmscomputing.device.capi.CapiSystem.getSystem().getGUI());
    }catch(Exception ce){
      System.out.println(getClass().getName()+".getCenterPanel:\n\tNo Capi System available.");
    }

    p.add(tp,BorderLayout.CENTER);
    return p;
  }

  public static void main(String[] argv){
    try{
      MainApp app=new MainApp("Fax Receiver [2005-09-26]", argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}




