package uk.co.mmscomputing.util.lookandfeel;

import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.util.log.*;

public class MainApp extends JApplet{

  public void init(){    
    JTabbedPane tp=new JTabbedPane();
    tp.addTab("Log",new LogBook());

    Container cp=this.getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(tp,BorderLayout.CENTER);

    String osname=System.getProperty("os.name");
    System.out.println(osname);

    UIManager.LookAndFeelInfo[] infos=UIManager.getInstalledLookAndFeels();
    for(int i=0; i<infos.length; i++){
      System.out.println(infos[i].toString());
    }
  }

  public void main(String title, String[] argv){
    JFrame frame=new JFrame(title);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(this);

    GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
    Rectangle r=ge.getMaximumWindowBounds();
    frame.setSize(r.width*2/3,r.height*2/3);
    frame.setLocationRelativeTo(null);

    init();
    frame.setVisible(true);
    start();
  }

  public static void main(String[] argv){
    LookAndFeel.set();
    MainApp app=new MainApp();
    app.main("mmsc - Look & Feel Application [2003-10-26]", argv);
  }

}


