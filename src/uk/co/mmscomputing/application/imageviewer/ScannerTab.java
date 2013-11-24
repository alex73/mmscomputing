package uk.co.mmscomputing.application.imageviewer;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import uk.co.mmscomputing.device.scanner.*;

import uk.co.mmscomputing.device.twain.*;
import uk.co.mmscomputing.device.sane.*;

public class ScannerTab extends ImageTab implements ScannerListener{

  static private int no=1;

  private Scanner scanner;

  public ScannerTab(java.util.Properties properties){
    super(properties);
  }

  protected void setButtonPanel(JPanel gui){
    super.setButtonPanel(gui);

    try{
      scanner=Scanner.getDevice();                  // get a device and set GUI panel up
      if(scanner!=null){
        gui.add(scanner.getScanGUI());
        scanner.addListener(this);

        if(scanner instanceof TwainScanner){
          TwainIdentity[] list=((TwainScanner)scanner).getIdentities();
          for(int i=0;i<list.length;i++){
            System.out.println(list[i].toString());
          }
        }
      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".setButtonPanel:\n\t"+e);
    }
  }

  public void update(ScannerIOMetadata.Type type, final ScannerIOMetadata metadata){
    if(type.equals(ScannerIOMetadata.ACQUIRED)){    // acquired BufferedImage
      final BufferedImage image=metadata.getImage();// make reference copy here to avoid race condition
      new Thread(){
        public void run(){
          try{
            addImage("scan_"+(no++),image);
          }catch(Exception e){
            System.out.println("9\b"+getClass().getName()+".update:\n\t"+e);
            System.err.println(getClass().getName()+".update:\n\t"+e);
            e.printStackTrace();
          }
        }
      }.start();
    }else if(type.equals(ScannerIOMetadata.FILE)){  // acquired image as file (twain only for the time being)
      final File file=metadata.getFile();           // make reference copy here to avoid race condition
      new Thread(){
        public void run(){
          try{
            open(file.getPath());
          }catch(Exception e){
            System.out.println("9\b"+getClass().getName()+".update:\n\t"+e);
            System.err.println(getClass().getName()+".update:\n\t"+e);
            e.printStackTrace();
          }finally{
            if(!file.delete()){
              System.out.println("9\b"+getClass().getName()+".update:\n\tCould not delete: "+file.getPath());
              System.err.println(getClass().getName()+".update:\n\tCould not delete: "+file.getPath());
            }
          }
        }
      }.start();
    }else if(type.equals(ScannerIOMetadata.NEGOTIATE)){
      negotiate(metadata);    
    }else if(type.equals(ScannerIOMetadata.STATECHANGE)){
      System.out.println("Scanner State "+metadata.getStateStr());
      System.err.println("Scanner State "+metadata.getStateStr());

      if(metadata instanceof TwainIOMetadata){                                // TWAIN only !
        if(metadata.isState(TwainConstants.STATE_TRANSFERREADY)){             // state = 6
          TwainSource source = ((TwainIOMetadata)metadata).getSource();
          try{
            TwainImageInfo imageInfo=new TwainImageInfo(source);            
            imageInfo.get();
            System.out.println(imageInfo.toString());
          }catch(Exception e){
            System.out.println("3\b"+getClass().getName()+".update:\n\tCannot retrieve image information.\n\t"+e);
          }
          try{
            TwainImageLayout imageLayout=new TwainImageLayout(source);      
            imageLayout.get();
            System.out.println(imageLayout.toString());
          }catch(Exception e){
            System.out.println("3\b"+getClass().getName()+".update:\n\tCannot retrieve image layout.\n\t"+e);
          }
        }else if(metadata.isState(TwainConstants.STATE_TRANSFERRING)){       // state = 7

//        In state 4: supportTwainExtImageInfo=source.getCapability(TwainConstants.ICAP_EXTIMAGEINFO).booleanValue();

          TwainSource source = ((TwainIOMetadata)metadata).getSource();
          try{            
            int[] tweis=new int[0x1240-0x1200];
            for(int i=0;i<tweis.length;i++){tweis[i]=0x1200+i;}

            TwainExtImageInfo imageInfo=new TwainExtImageInfo(source,tweis);
            imageInfo.get();
            System.out.println(imageInfo.toString());
          }catch(Exception e){
            System.out.println("3\b"+getClass().getName()+".update:\n\tCannot retrieve extra image information.\n\t"+e);
          }
        }
      }

    }else if(type.equals(ScannerIOMetadata.INFO)){
      System.out.println(metadata.getInfo());
    }else if(type.equals(ScannerIOMetadata.EXCEPTION)){
      System.out.println("9\b"+metadata.getException().getMessage());
      metadata.getException().printStackTrace();
    }
  }

  private void negotiate(ScannerIOMetadata metadata){

/*
    ScannerDevice sd=metadata.getDevice();                   // SANE & TWAIN
    try{
//      sd.setShowUserInterface(false);
//      sd.setShowProgressBar(false);                          // Twain: works only if user interface is inactive
      sd.setResolution(100.0);                                 // set resolution in dots per inch
//      sd.setRegionOfInterest(20,40,600,400);                 // set region of interest to x,y,width,height in pixels
//      sd.setRegionOfInterest(20.5,45.0,66.66,40.0);          // set region of interest to x,y,width,height in millimeters
    }catch(Exception e){
      System.out.println("9\b"+e.getMessage());
//      metadata.setCancel(true);                              // cancel scan if we can't set it up our way
    }
*/

    if(metadata instanceof TwainIOMetadata){                 // TWAIN only!
      TwainSource source=((TwainIOMetadata)metadata).getSource();

      // Remember: We are in state 4 and in windows thread. Catch all exceptions!

/*
      String[] names=TwainIdentity.getProductNames();        // let's see what data sources we have
      for(int i=0;i<names.length;i++){
        System.out.println(names[i]);
      }
*/
/*
      try{
        TwainIdentity[] list=TwainIdentity.getIdentities();
        for(int i=0;i<list.length;i++){
          System.out.println(list[i].toString());
        }
      }catch(Exception e){
        System.out.println("9\b"+e.getMessage());
      }
*/    
///*  
      try{      
        TwainCapability[] caps=source.getCapabilities();     // print out all the capabilities
        for(int i=0;i<caps.length;i++){
          System.out.println(caps[i].toString());
        }
      }catch(Exception e){
        System.out.println("9\b"+e.getMessage());
      }
//*/
/*
      // use automatic document feeder, scan 5 pages
      try{
        int transferCount=5;
        source.setCapability(TwainConstants.CAP_FEEDERENABLED,true);
        source.setCapability(TwainConstants.CAP_AUTOFEED,true);

//        source.setCapability(TwainConstants.CAP_AUTOSCAN,true);
//        System.err.println(source.getCapability(TwainConstants.CAP_AUTOSCAN).toString());

        source.setCapability(TwainConstants.CAP_XFERCOUNT,transferCount);
        transferCount=source.getCapability(TwainConstants.CAP_XFERCOUNT).intValue();
        System.err.println("set transferCount: "+transferCount);
      }catch(Exception e){
        System.out.println("9\bCAP_FEEDERENABLED/CAP_AUTOFEED/CAP_XFERCOUNT: "+e.getMessage());
//        metadata.setCancel(true);                     // negotiation failed let's try to abort scan
      }
*/
/*
      try{
//      source.setXferMech(TwainConstants.TWSX_NATIVE);      // send image as BufferedImage (default)

//      source.setXferMech(TwainConstants.TWSX_MEMORY);      // send image as byte[] blocks (not implemented here yet)

        source.setXferMech(TwainConstants.TWSX_FILE);        // send image as file
//        source.setImageFileFormat(TwainConstants.TWFF_BMP);  // set file format to bmp (must be supported by all sources)
        source.setImageFileFormat(TwainConstants.TWFF_JFIF); // set file format to jpeg

//        source.setImageFileFormat(TwainConstants.TWFF_TIFF); // set file format to tiff if supported otherwise use last settings
                                                             // i.e. source's default. The default does not have to be bmp!
        System.out.println();
        System.out.println(source.getCapability(TwainConstants.ICAP_XFERMECH).toString());
        System.out.println(source.getCapability(TwainConstants.ICAP_IMAGEFILEFORMAT).toString());
        System.out.println();
      }catch(Exception e){
        System.out.println("9\bTransfer Mechanism : "+e.getMessage());
      }
*/
/*
      try{                                                   
        System.err.println("Test BitDepth");
        source.setCapability(TwainConstants.ICAP_PIXELTYPE,TwainConstants.TWPT_GRAY);
        System.err.println(source.getCapability(TwainConstants.ICAP_PIXELTYPE).toString());        
        System.err.println(source.getCapability(TwainConstants.ICAP_BITDEPTH).toString());        
        source.setCapability(TwainConstants.ICAP_BITDEPTH,4);
        System.err.println(source.getCapability(TwainConstants.ICAP_BITDEPTH).toString());        
      }catch(Exception e){
        System.out.println("9\bPixel Type: "+e.getMessage());
      }
*/
/*
      try{                                                   // set Black/White aka Lineart
        source.setCapability(TwainConstants.ICAP_PIXELTYPE,TwainConstants.TWPT_BW);
        System.out.println(source.getCapability(TwainConstants.ICAP_PIXELTYPE).toString());        
      }catch(Exception e){
        System.out.println("9\bPixel Type: "+e.getMessage());
      }
*/
/*
      try{
        int TWSS_NONE       = 0;
        int TWSS_A4LETTER   = 1;
        int TWSS_USLETTER   = 3;
        int TWSS_USLEGAL    = 4;

        TwainCapability tc=source.getCapability(TwainConstants.ICAP_SUPPORTEDSIZES);
        if(tc.querySupport(TwainConstants.TWQC_SET)){  // is set operation allowed
          System.out.println(tc.toString());
          tc.setCurrentValue(TWSS_A4LETTER);
          System.out.println(source.getCapability(TwainConstants.ICAP_SUPPORTEDSIZES).toString());
        }
      }catch(Exception e){
        System.out.println("9\bPaper Sizes: "+e.getMessage());
      }
*/
    }
/*
    if(metadata instanceof TwainIOMetadata){                 // TWAIN only!
      TwainSource source=((TwainIOMetadata)metadata).getSource();
      try{                                                   // set Black/White aka Lineart
        source.setCapability(TwainConstants.ICAP_PIXELTYPE,TwainConstants.TWPT_BW);
        System.out.println(source.getCapability(TwainConstants.ICAP_PIXELTYPE).toString());        

        source.setCapability(TwainConstants.ICAP_THRESHOLD,40.0);
        System.out.println(source.getCapability(TwainConstants.ICAP_THRESHOLD).toString());        
      }catch(Exception e){
        System.out.println("9\bPixel Type: "+e.getMessage());
      }
    }
*/
/*
    if(metadata instanceof TwainIOMetadata){                 // TWAIN only!
      TwainSource source=((TwainIOMetadata)metadata).getSource();
      try{
        source.setCapability(TwainConstants.ICAP_PIXELTYPE,TwainConstants.TWPT_GRAY);
        System.out.println(source.getCapability(TwainConstants.ICAP_PIXELTYPE).toString());        
        source.setCapability(TwainConstants.ICAP_AUTOBRIGHT,false);
        System.out.println(source.getCapability(TwainConstants.ICAP_AUTOBRIGHT).toString());        
        source.setCapability(TwainConstants.ICAP_BRIGHTNESS,-800.0);
        System.out.println(source.getCapability(TwainConstants.ICAP_BRIGHTNESS).toString());        
//        source.setCapability(TwainConstants.ICAP_CONTRAST,500);
//        System.out.println(source.getCapability(TwainConstants.ICAP_CONTRAST).toString());        
      }catch(Exception e){
        System.out.println("9\bPixel Type: "+e.getMessage());
      }
    }
*/
  }

  public void stop(){                                                    // execute before System.exit
    if(scanner!=null){                                                   // make sure user waits for scanner to finish!
      scanner.waitToExit();
    }
  }
}
