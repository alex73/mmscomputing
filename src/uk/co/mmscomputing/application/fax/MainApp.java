package uk.co.mmscomputing.application.fax;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.device.fax.*;

import uk.co.mmscomputing.application.phone.*;

public class MainApp extends UtilMainApp{

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

    tp.addTab("Fax",new FaxTab(properties,phonebook));
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
      MainApp app=new MainApp("Fax Application [2005-09-26]", argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}




