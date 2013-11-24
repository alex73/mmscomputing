package uk.co.mmscomputing.application.install;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainApp extends JApplet{

  Log log=new Log();
  Setup setup=new Setup(log);

  public void init(){    

    Container cp=this.getContentPane();
    cp.setLayout(new BorderLayout());

    cp.add(setup,BorderLayout.NORTH);
    cp.add(log,BorderLayout.CENTER);

    JButton button=new JButton(getStartAction());
    cp.add(button,BorderLayout.SOUTH);

    log.msg("This installer will\nextract the 'uk.co.mmscomputing' packages in the installation directory,"
    +       "\ncompile all the '*.java' files,"
    +       "if provided with a valid path to a java compiler (javac),"
    +       "\nand start 'uk.co.mmscomputing.application.answerphone.MainApp'."
    +       "\n\nA path to a java compiler might look like :"
    +       "\nWindows XP :     c:\\j2sdk1.4.2\\bin\\javac"
    +       "\nLinux :          /usr/java/j2sdk1.4.2/bin/javac"
    +       "\n\nPlease press the start button to continue.\n\n"
    );
  }

  public Action getStartAction(){
    return new AbstractAction("start"){
      public void actionPerformed(ActionEvent ev){
        start();
      }
    };
  }

  public void start(){
    final String javac=setup.getJavac();
    final String dir=setup.getUserdir();
    log.msg(javac);
    new Thread(){
      public void run(){
        try{
          if(setup.getExtractIt()){
            new Unzip(log).extract("mmsc.jar");
          }
          Engine engine=new Engine(log);
          if(setup.getCompileIt()){
            engine.traverse(dir,javac);
            engine.metainf();
          }
          if(setup.getStartImageViewer()){
            engine.run(javac,dir,"uk.co.mmscomputing.application.imageviewer.MainApp");
          }
          if(setup.getStartAnswerphone()){
            engine.run(javac,dir,"uk.co.mmscomputing.application.answerphone.MainApp");
          }
        }catch(Exception e){
          log.err(e);
        }
      }
    }.start();
  }

  public void main(String title, String[] argv){
    JFrame frame=new JFrame(title);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(this);

    GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
    Rectangle r=ge.getMaximumWindowBounds();
    frame.setSize(r.width,r.height);
//    frame.setLocationRelativeTo(null);
//    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

    init();
    frame.setVisible(true);
  }

  public static void main(String[] argv){
    MainApp app=new MainApp();
    app.main("mmsc java package setup", argv);
  }
}


