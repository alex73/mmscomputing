package uk.co.mmscomputing.util.lookandfeel;

import java.awt.*;
import javax.swing.*;

public class LookAndFeel{

  static public void set(){    
    String osname=System.getProperty("os.name");
    try{
       if(osname.startsWith("Linux")){
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
      }else if(osname.startsWith("Windows")){
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      }else if(osname.startsWith("Mac")){
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
      }else{
//      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      }
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }

  static public void printAllInfos(){    
    UIManager.LookAndFeelInfo[] infos=UIManager.getInstalledLookAndFeels();
    for(int i=0; i<infos.length; i++){
      System.err.println(infos[i].toString());
    }
  }
}


