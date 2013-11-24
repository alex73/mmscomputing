package uk.co.mmscomputing.util.log;

import java.awt.*;
import javax.swing.*;

public class MainApp extends JApplet{
  static JFrame frame=new JFrame("mmsc Log");
  static LogBook log=new LogBook();

  public void init(){
    Container cp=this.getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(log,BorderLayout.CENTER);

    System.out.println("Hi this is a message!");
    System.out.println("9\bHi this is an error message!");
    System.out.println("5\bHi this is a warning message!");
    System.out.println("Hi this is another message!");
  }

  public static void main(String[] argv){
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    MainApp app=new MainApp();
    frame.getContentPane().add(app);
    app.init();
    Dimension ds=Toolkit.getDefaultToolkit().getScreenSize();
    frame.setSize(400,300);
    frame.setVisible(true);
    app.start();
  }
}

