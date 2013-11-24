package uk.co.mmscomputing.device.twain.tiffapplet; 

import java.awt.Button;
import java.awt.GridLayout;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
//import uk.co.mmscomputing.imageio.tiff.*;

/*
You need to add the 

  META-INF/services/javax.imageio.spi.ImageWriterSpi

and more or less all of

  uk/co/mmscomputing/io/
  uk/co/mmscomputing/imageio/
  uk/co/mmscomputing/math/Rational.class

to applet jar
*/


public class TwainTiffAppletExample extends Applet implements ActionListener, ScannerListener{

  Scanner scanner;
  Button  acquireButton,selectButton;

  ImageWriter writer;
  boolean     writeImage=false;

  public TwainTiffAppletExample(){}

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

    writer = (ImageWriter)ImageIO.getImageWritersByFormatName("tif").next();
    System.out.println(writer.getClass().getName()); 
//  should be: uk.co.mmscomputing.imageio.tiff.TIFFImageWriter
  }

  public void actionPerformed(ActionEvent evt){
    try{
      if(evt.getSource()==acquireButton){
        try{
          File home = new File(System.getProperty("user.home"));
          File dir  = new File(home,"My Documents");
          if(!dir.exists()){ dir = home;}
          File file = new File(dir,"test.tif");file.delete();
          showStatus(file.getCanonicalPath());
          ImageOutputStream ios = ImageIO.createImageOutputStream(file);
          writer.setOutput(ios);
          writer.prepareWriteSequence(null);
          writeImage=true;
        }catch(Exception e){
          writeImage=false;
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
        if(writeImage){
          writer.writeToSequence(new IIOImage(image,null,null),null);
        }
      }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
        System.out.println(metadata.getStateStr());
        if(metadata.isFinished()){
          writer.endWriteSequence();
          ((ImageOutputStream)writer.getOutput()).close();
        }
      }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
        metadata.getException().printStackTrace();
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
