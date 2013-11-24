package uk.co.mmscomputing.application.faxreceiver;

import java.util.*;
import java.beans.*;
import javax.swing.*;

import uk.co.mmscomputing.application.imageviewer.*;

public class FaxImageTab extends ImageTab{

  public FaxImageTab(Properties properties){
    super(properties);
  }

  public void propertyChange(final PropertyChangeEvent evt){
    String prop=evt.getPropertyName();
    if(prop.equals("open")){
      JTabbedPane tp=(JTabbedPane)getParent();
      tp.setSelectedIndex(tp.indexOfTab("Fax Image"));
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

