package uk.co.mmscomputing.device.twain.pdfapplet; 

import java.awt.Button;
import java.awt.GridLayout;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerDevice;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerIOException;

//import javax.imageio.ImageWriteParam;
//import uk.co.mmscomputing.imageio.pdf.*;

/*
You need to add the 

  META-INF/services/javax.imageio.spi.ImageWriterSpi

and more or less all of

  uk/co/mmscomputing/io/
  uk/co/mmscomputing/imageio/

to applet jar
*/


public class TwainPdfAppletExample extends Applet implements ActionListener, ScannerListener{

  static TwainPdfAppletExample app;

  Scanner scanner;
  Button  acquireButton,selectButton;

  File    dir;

//  ImageWriter writer;
//  boolean     writeImage=false;

  public TwainPdfAppletExample(){}

  public void init(){
    setLayout(new GridLayout(0,2));
    selectButton = new Button("select");
    add(selectButton);
    selectButton.addActionListener(this);

    acquireButton = new Button("acquire");
    add(acquireButton);
    acquireButton.addActionListener(this);

    scanner=Scanner.getDevice();
    scanner.addListener(this);

//    writer = (ImageWriter)ImageIO.getImageWritersByFormatName("pdf").next();
//    System.out.println(writer.getClass().getName()); 
//  should be: uk.co.mmscomputing.imageio.pdf.PDFImageWriter
  }

  public void actionPerformed(ActionEvent evt){
    try{
      if(evt.getSource()==acquireButton){
        try{
          File home = new File(System.getProperty("user.home"));
          dir  = new File(home,"My Documents");
          if(!dir.exists()){ dir = home;}
          File file = new File(dir,"testIO.pdf");file.delete();
          System.out.println(file.getCanonicalPath());
//          ImageOutputStream ios = ImageIO.createImageOutputStream(file);
//          writer.setOutput(ios);
//          writer.prepareWriteSequence(null);
//          writeImage=true;
        }catch(Exception e){
//          writeImage=false;
          e.printStackTrace();
        }
        scanner.acquire();
      }else if(evt.getSource()==selectButton){
        scanner.select();
      }
    }catch(ScannerIOException se){
      se.printStackTrace();
    }
  }

  public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata){
    try{
      if(type.equals(ScannerIOMetadata.ACQUIRED)){
        BufferedImage image=metadata.getImage();
        System.out.println("Have an image now!");
        File file = new File(dir,"testIO.pdf");file.delete();
        ImageIO.write(image, "pdf", file);
//        if(writeImage){
//          writer.writeToSequence(new IIOImage(image,null,null),null);
//        }
      }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
        System.out.println(metadata.getStateStr());
        if(metadata.isFinished()){                                         // You will get here only if you close source properly!
//          writer.endWriteSequence();
//          ((ImageOutputStream)writer.getOutput()).close();               // Don't forget this!
        }
      }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
        metadata.getException().printStackTrace();
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void main(String title, String[] argv){
    JFrame frame=new JFrame(title);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(this);

    frame.setSize(500,200);
    frame.setLocationRelativeTo(null);

    init();

    frame.setVisible(true);

//    ImageIO.scanForPlugins();

    start();
  }

  public static void main(String[] argv){
    try{
      TwainPdfAppletExample app=new TwainPdfAppletExample();
      app.main(TwainPdfAppletExample.class.getName(), argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
