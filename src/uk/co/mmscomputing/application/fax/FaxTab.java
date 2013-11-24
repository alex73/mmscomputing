package uk.co.mmscomputing.application.fax;

import java.util.*;
import java.awt.*;
import java.beans.*;
import javax.swing.*;

import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.device.phone.*;
import uk.co.mmscomputing.application.phone.*;
import uk.co.mmscomputing.application.faxsender.*;
import uk.co.mmscomputing.application.faxreceiver.*;

import uk.co.mmscomputing.device.capi.CapiVoicePlugin;

public class FaxTab extends JPanel{

  final private String faximagetxt="Fax Images";

  public FaxTab(Properties properties,PhoneBook phonebook){
    setLayout(new BorderLayout());
    JTabbedPane tp=new JTabbedPane();
    FaxReceiverTab rp=new FaxReceiverTab(properties,phonebook);
    CapiVoicePlugin.getDefaultPlugin().addListener(rp);

    tp.addTab("Fax List",rp);
    ImagePanel ip=new ImagePanel(properties);
    tp.addTab(faximagetxt,ip);
    rp.addPropertyChangeListener(ip);
    tp.addTab("Fax Properties",new FaxPropertiesPanel(properties));
    add(tp,BorderLayout.CENTER);
  }

  public class ImagePanel extends FaxSenderTab{

    public ImagePanel(Properties properties){
      super(properties);
    }

    public void propertyChange(final PropertyChangeEvent evt){
      String prop=evt.getPropertyName();
      if(prop.equals("open")){
        JTabbedPane tp=(JTabbedPane)getParent();
        tp.setSelectedIndex(tp.indexOfTab(faximagetxt));
        new Thread(){
          public void run(){
            try{
              open((String)evt.getNewValue());
            }catch(Exception e){
              System.out.println("9\b"+e.getMessage());
              e.printStackTrace();
            }
          }
        }.start();
      }
    }
  }
}