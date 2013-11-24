package uk.co.mmscomputing.application.phone;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.sound.*;
import uk.co.mmscomputing.device.phone.*;
import uk.co.mmscomputing.device.capi.CapiVoicePlugin;

public class PhoneTab extends JPanel{

  public PhoneTab(Properties properties,PhoneBook phonebook){
    setLayout(new BorderLayout());
    JTabbedPane tp=new JTabbedPane();

    AnswerPanel rp=getAnswerPanel(properties,phonebook);
    CapiVoicePlugin.getDefaultPlugin().addListener(rp);

    tp.addTab("Phone Call List",rp);
    tp.addTab("Phone Properties",new PhonePropertiesPanel(properties));
    add(tp,BorderLayout.CENTER);
  }

  protected AnswerPanel getAnswerPanel(Properties properties,PhoneBook phonebook){
    return new AnswerPanel(properties,phonebook);
  }

  private static class HandlerFactory implements PhoneCallHandlerFactory{
    public PhoneCallHandler getHandler(Properties properties){
//      PhoneCallHandler handler=new PhoneCallSaver();
      PhoneCallMonitor handler=new PhoneCallMonitor();
      handler.init(properties);
      return handler;
    }
  }

  protected class AnswerPanel extends PhoneAnswererPanel{

    public AnswerPanel(Properties properties,PhoneBook phonebook){
      super(
        properties,
        new HandlerFactory(),
        phonebook
      );
    }

    protected void playPhoneCall(String time,String local,String remote,String file){
      String[] arg=new String[3];
      String   name=phonebook.getName(remote);
               arg[0]=name+" ["+remote+"]";
               arg[1]=file;

      JFrame frame=SoundPlayer.getDevice(arg);
    }
  }
}