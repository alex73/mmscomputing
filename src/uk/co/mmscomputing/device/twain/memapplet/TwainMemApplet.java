package uk.co.mmscomputing.device.twain.memapplet;

import java.awt.Button;
import java.awt.GridLayout;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

//import javax.imageio.ImageWriter;
//import javax.imageio.IIOImage;
//import javax.imageio.stream.ImageOutputStream;

import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import uk.co.mmscomputing.device.scanner.*;
import uk.co.mmscomputing.device.twain.*;

public class TwainMemApplet extends Applet implements ActionListener, ScannerListener{

  static TwainMemApplet app;  
  static int transferCount = 0;

  // assume: no compression

//  int     twainimgtype = TwainConstants.TWPT_GRAY;
  int     twainimgtype = TwainConstants.TWPT_RGB;
//  int     twainimgtype = TwainConstants.TWPT_BW;

  File    dir = null;
  Scanner scanner;
  Button  acquireButton,selectButton;

  public TwainMemApplet(){}

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

    try{
      File home = new File(System.getProperty("user.home"));
      dir  = new File(home,"My Documents");
      if(!dir.exists()){ dir = home;}
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void actionPerformed(ActionEvent evt){
    try{
      if(evt.getSource()==acquireButton){
        scanner.acquire();
      }else if(evt.getSource()==selectButton){
        scanner.select();
      }
    }catch(ScannerIOException se){
      se.printStackTrace();
    }
  }

  public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata){
    if(type.equals(ScannerIOMetadata.MEMORY)){
      try{
        if(metadata instanceof TwainIOMetadata){                 // TWAIN only!
          TwainIOMetadata twaindata = (TwainIOMetadata)metadata;
          TwainTransfer.MemoryTransfer.Info info=twaindata.getMemory();

          byte[] twainbuf = info.getBuffer();
          int    width    = info.getWidth();
          int    height   = info.getHeight();

          BufferedImage image = null;

          if(twainimgtype == TwainConstants.TWPT_BW){
            width = info.getBytesPerRow()*8;                 // the cheap way out ;)
            image = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
            byte[]  imgbuf = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
            System.arraycopy(twainbuf,0,imgbuf,0,imgbuf.length);
          }else if(twainimgtype == TwainConstants.TWPT_GRAY){
            width = info.getBytesPerRow();                   // the cheap way out ;)
            image = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
            byte[]  imgbuf = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
            System.arraycopy(twainbuf,0,imgbuf,0,imgbuf.length);
          }else if(twainimgtype == TwainConstants.TWPT_RGB){
            image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
/*
Hint: 
If using TWPT_RGB - BufferedImage.TYPE_INT_RGB : 3 bytes to 1 int
If using TWPT_RGB - BufferedImage.TYPE_3BYTE_BGR : RGB -> BGR
*/
            int bpr = info.getBytesPerRow();

            int r, g, b, row=0, pixel=0;
            for(int y=0; y<height;y++){
              for(int x=0; x<width; x++){
                r = twainbuf[pixel++]&0x00FF;
                g = twainbuf[pixel++]&0x00FF;
                b = twainbuf[pixel++]&0x00FF;
                image.setRGB(x,y,(r<<16)|(g<<8)|b);
              }
              row  += bpr;
              pixel = row;
            }
          }         

          String fn = "image"+transferCount+".png";
          File file = (dir!=null)?new File(dir,fn):new File(fn);
          System.out.println(file.getCanonicalPath());

          ImageIO.write(image, "png", file);
          transferCount++;

          System.out.println("info = "+info.toString()+"\n");

        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      transferCount = 0;
      ScannerDevice device=metadata.getDevice();
      try{
        device.setShowUserInterface(false);
        device.setShowProgressBar(true);
        device.setResolution(100);

        if(metadata instanceof TwainIOMetadata){                 // TWAIN only!
          TwainSource source=((TwainIOMetadata)metadata).getSource();

          source.setCapability(TwainConstants.ICAP_PIXELTYPE,twainimgtype);
          System.out.println(source.getCapability(TwainConstants.ICAP_PIXELTYPE).toString());        

          source.setXferMech(TwainConstants.TWSX_MEMORY);
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      System.err.println(metadata.getStateStr());
    }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
      metadata.getException().printStackTrace();
    }
  }

  public static void main(String[] argv){
    try{
      app=new TwainMemApplet();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}


