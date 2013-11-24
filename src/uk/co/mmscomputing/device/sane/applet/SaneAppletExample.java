package uk.co.mmscomputing.device.sane.applet; 

import java.awt.Button;
import java.awt.GridLayout;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerDevice;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import javax.swing.JFrame;

public class SaneAppletExample extends Applet implements ActionListener, ScannerListener{

  String  filename;

  Scanner scanner;
  Button  acquireButton,selectButton,cancelButton;

  public SaneAppletExample(){}

  public SaneAppletExample(String title, String[] argv){    
    JFrame.setDefaultLookAndFeelDecorated(true);

    JFrame frame=new JFrame(title);
//    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        stop();System.exit(0);
      }
    });

    init();

    frame.getContentPane().add(this);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    start();
  }

  public void init(){
    setLayout(new GridLayout(1,3));
    selectButton = new Button("select");
    add(selectButton);
    selectButton.addActionListener(this);

    acquireButton = new Button("acquire");
    add(acquireButton);
    acquireButton.addActionListener(this);

    cancelButton = new Button("cancel next scan");
    add(cancelButton);
    cancelButton.addActionListener(this);

    filename=System.getProperty("user.home")+"/test.jpg";

    scanner=Scanner.getDevice();
    scanner.addListener(this);
  }

  public void actionPerformed(ActionEvent evt){
    try{
      if(evt.getSource()==acquireButton){
        scanner.acquire();
      }else if(evt.getSource()==selectButton){
        scanner.select();
      }else if(evt.getSource()==cancelButton){
        scanner.setCancel(true);
      }
    }catch(ScannerIOException sioe){
      sioe.printStackTrace();
    }
  }

  public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata){

    if(type.equals(ScannerIOMetadata.ACQUIRED)){
      BufferedImage image=metadata.getImage();
      System.out.println("Have an image now!");
      try{
        File file = new File(filename);
        System.out.println("Image Path: "+file.getAbsolutePath());
        ImageIO.write(image, "jpg", file);
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      ScannerDevice device=metadata.getDevice();
/*
      try{
//        device.setShowUserInterface(false);
        device.setShowProgressBar(true);
        device.setRegionOfInterest(20,40,300,200);
        device.setResolution(100.0);
      }catch(Exception e){
        e.printStackTrace();
      }
*/
    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      System.err.println(metadata.getStateStr());
    }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
      metadata.getException().printStackTrace();
    }
  }

  public static void main(String[] argv){
    try{
      SaneAppletExample app = new SaneAppletExample("Sane Applet Example [2007-11-19]", argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}