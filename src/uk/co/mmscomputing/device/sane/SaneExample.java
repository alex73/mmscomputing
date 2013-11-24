package uk.co.mmscomputing.device.sane;

import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerDevice;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerIOException;

import uk.co.mmscomputing.device.sane.*;

public class SaneExample implements ScannerListener{

  static SaneExample app;

  Scanner scanner;
  int     index=0;

  public SaneExample(String[] argv){
    scanner=Scanner.getDevice();
    scanner.addListener(this); 
    try{
//      scanner.select("fujitsu:/dev/sg0");
      System.out.println("Device Name: "+scanner.getSelectedDeviceName());    
      scanner.acquire();
    }catch(ScannerIOException se){
      se.printStackTrace();
    }
  }

  public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata){
    if(type.equals(ScannerIOMetadata.ACQUIRED)){
      BufferedImage image=metadata.getImage();
      System.out.println("Have image "+index+" now! ");
      try{
        ImageIO.write(image, "jpeg", new File("mmsc_image"+(index++)+".jpeg"));
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      System.err.println(metadata.getStateStr());
      if(metadata.isFinished()){
//        System.exit(0);
      }
    }else if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      SaneDevice device=(SaneDevice)metadata.getDevice();

//      device.setADFMode(true);
      try{
        device.setShowUserInterface(false);   // default: true
        device.setShowProgressBar(true);      // default: true
//        device.setResolution(75);
//        device.setOption("mode","Color");

//        device.setOption("source","Flatbed");
//        System.out.println(device.getOption("source"));
//        System.out.println(device.getOption("source").getStringValue());
//        device.setOption("source","ADF Duplex");
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
      metadata.getException().printStackTrace();
    }
  }

  public static void main(String[] argv){
    try{
      app=new SaneExample(argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}


