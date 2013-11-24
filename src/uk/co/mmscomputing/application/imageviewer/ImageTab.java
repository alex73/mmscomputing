package uk.co.mmscomputing.application.imageviewer;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;                // as of 1.5.0 java.util has class Scanner
import javax.imageio.*;
import javax.imageio.stream.*;
import java.beans.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.imageio.metadata.*;

import uk.co.mmscomputing.util.JarImageIcon;

//import uk.co.mmscomputing.imageio.*;
import uk.co.mmscomputing.image.operators.*;

public class ImageTab extends JPanel implements PropertyChangeListener{

  static public final String fileOpenID="uk.co.mmscomputing.file.open.dir";
  static public final String fileSaveID="uk.co.mmscomputing.file.save.dir";

  protected Properties   properties;
  protected JTabbedPane  images;
  protected JFileChooser openfc;
  protected JFileChooser savefc;

  public ImageTab(Properties properties){
    this.properties=properties;

    setLayout(new BorderLayout());

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    JPanel q=new JPanel();
    q.setLayout(new BoxLayout(q,BoxLayout.PAGE_AXIS));
    setButtonPanel(q);
    p.add(q,BorderLayout.NORTH);
    add(p,BorderLayout.EAST);

    images=new JTabbedPane();
    add(images,BorderLayout.CENTER);

    String userdir=System.getProperty("user.home");
    setOpenDir(properties.getProperty(fileOpenID,userdir));
    setSaveDir(properties.getProperty(fileSaveID,userdir));
  }

  public void setOpenDir(String path){
    new File(path).mkdirs();
    openfc=new JFileChooser(path);
  }

  public void setSaveDir(String path){
    new File(path).mkdirs();
    savefc=new JFileChooser(path);
  }

  protected void setButtonPanel(JPanel gui){
    JPanel buttonPanel=new JPanel();
    buttonPanel.setLayout(new GridLayout(0,1));

    buttonPanel.add(new JButton(getNewAction()));
    buttonPanel.add(new JButton(getOpenAction()));
    buttonPanel.add(new JButton(getSaveAction()));
    buttonPanel.add(new JButton(getPrintAction()));
    buttonPanel.add(new JButton(getConvertAction()));
    buttonPanel.add(new JButton(getRotateAction()));

    gui.add(buttonPanel);
  }

  public Action getNewAction(){
    return new AbstractAction("new",new JarImageIcon(getClass(),"32x32/new.png")){
      public void actionPerformed(ActionEvent ev){
        images.removeAll();
      }
    };
  }

  public Action getOpenAction(){
    return new AbstractAction("open",new JarImageIcon(getClass(),"32x32/open.png")){
      public void actionPerformed(ActionEvent ev){
        int res=openfc.showOpenDialog(null);
        properties.setProperty(fileOpenID,openfc.getCurrentDirectory().toString());
        if(res==JFileChooser.APPROVE_OPTION){
          try{
            open(openfc.getSelectedFile().getPath());
          }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Image Open Error : "+e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE); 
          }
        }
      }
    };
  }

  public Action getSaveAction(){
    return new AbstractAction("save",new JarImageIcon(getClass(),"32x32/save.png")){
      public void actionPerformed(ActionEvent ev){
        int res=savefc.showSaveDialog(null);
        properties.setProperty(fileSaveID,savefc.getCurrentDirectory().toString());
        if(res==JFileChooser.APPROVE_OPTION){
          new Thread(){
            public void run(){
              try{
                save(savefc.getSelectedFile().getPath());
              }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Image Save Error : "+e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE); 
              }
            }
          }.start();
        }
      }
    };
  }

  public Action getPrintAction(){
    return new AbstractAction("",new JarImageIcon(getClass(),"32x32/print.png")){
      public void actionPerformed(ActionEvent ev){
        new Thread(){
          public void run(){
            Printer p=new Printer();
            for(int i=0; i<images.getTabCount(); i++){
              JScrollPane sp=(JScrollPane)images.getComponentAt(i);
              ImagePanel ip=(ImagePanel)sp.getViewport().getView();
              p.append(ip);
            }
            p.print();
          }
        }.start();        
      }
    };
  }

  public Action getConvertAction(){
    return new AbstractAction("<html><center><b>Colour</b><br><b>Reduction</b></center></html>"/*,new JarImageIcon(getClass(),"32x32/blackWhite.png")*/){
      public void actionPerformed(ActionEvent ev){
        convertImage();
      }
    };
  }

  public Action getRotateAction(){
    return new AbstractAction("",new JarImageIcon(getClass(),"32x32/rotate.png")){
      public void actionPerformed(ActionEvent ev){
        JScrollPane sp=(JScrollPane)images.getSelectedComponent();
        if(sp!=null){
          ImagePanel ip=(ImagePanel)sp.getViewport().getView();
          ip.rotate();
        }
      }
    };
  }

  protected void addImage(String fn, BufferedImage img){
    Object md=img.getProperty("iiometadata");
    if((md!=Image.UndefinedProperty)&&(md!=null)&&(md instanceof IIOMetadata)){
//      new MetadataReader().read((IIOMetadata)md);
    }

    System.out.println("Image.Width ="+img.getWidth());
    System.out.println("Image.Height ="+img.getHeight());

    ImagePanel ip=new ImagePanel();
    ip.addPropertyChangeListener(this);

    JScrollPane sp=new JScrollPane(ip);
    sp.getVerticalScrollBar().setUnitIncrement(100);
    sp.getHorizontalScrollBar().setUnitIncrement(100);    
    ip.setImage(img);
//    images.addTab("image",sp);
    images.addTab(fn,new TabCloseIcon(), sp);
    images.setSelectedIndex(images.getTabCount()-1);
  }

  public void open(String filename)throws IOException{
    long time=System.currentTimeMillis();

    String ext=filename.substring(filename.lastIndexOf('.')+1);
    Iterator readers=ImageIO.getImageReadersByFormatName(ext);
    if(!readers.hasNext()){throw new IOException(getClass().getName()+".open:\n\tNo reader for format '"+ext+"' available.");}
    
    ImageReader reader=(ImageReader)readers.next();
    while(!reader.getClass().getName().startsWith("uk.co.mmscomputing")&&readers.hasNext()){// prefer our own reader
      reader=(ImageReader)readers.next();
    }
    File f=new File(filename);
    ImageInputStream iis=ImageIO.createImageInputStream(f);
    try{
      reader.setInput(iis,true);
      try{
        for(int i=0; true; i++){
          IIOMetadata md=reader.getImageMetadata(i);
//          if(md!=null){new MetadataReader().read(md);}
          addImage(f.getName()+" "+i,reader.read(i));
        }
      }catch(IndexOutOfBoundsException ioobe){}
    }catch(Error e){
      System.out.println("9\b"+getClass().getName()+".open:\n\t"+e);
      e.printStackTrace();
      throw e;
    }finally{
      iis.close();
    }
    time=System.currentTimeMillis()-time;
    System.out.println("Opened : "+filename);
    System.out.println("Time used to load images : "+time);
  }

  private IIOImage getIIOImage(ImageWriter writer,ImageWriteParam iwp,BufferedImage image){
    ImageTypeSpecifier   it  = ImageTypeSpecifier.createFromRenderedImage(image);

/*
    try{
      uk.co.mmscomputing.imageio.bmp.BMPMetadata md=(uk.co.mmscomputing.imageio.bmp.BMPMetadata)image.getProperty("iiometadata");
      if(md!=null){
        md.setXPixelsPerMeter(11812);    // force 300 dpi for bmp images
        md.setYPixelsPerMeter(11812);    // works only with mmsc.bmp package
      }
    }catch(Exception e){}
*/

    IIOMetadata md;
    Object      obj=image.getProperty("iiometadata");               // if image is a TwainBufferedImage get metadata
    if((obj!=null)&&(obj instanceof IIOMetadata)){
      md = (IIOMetadata)obj;
    }else{
      md = writer.getDefaultImageMetadata(it,iwp);
    }
    return new IIOImage(image,null,md);
  }

  public void save(String filename)throws IOException{

    if(images.getTabCount()<=0){throw new IOException(getClass().getName()+".save:\n\tNo images available!");}

    String ext=filename.substring(filename.lastIndexOf('.')+1);
    Iterator writers=ImageIO.getImageWritersByFormatName(ext);
    if(!writers.hasNext()){throw new IOException(getClass().getName()+".save:\n\tNo writer for format '"+ext+"' available.");}
    ImageWriter writer=(ImageWriter)writers.next();

    while(!writer.getClass().getName().startsWith("uk.co.mmscomputing")&&writers.hasNext()){// prefer our own writer
      writer=(ImageWriter)writers.next();
    }

    ImageWriteParam    iwp        = writer.getDefaultWriteParam();
    IIOParamController controller = iwp.getController();
    if(controller!=null){controller.activate(iwp);}

    long time=System.currentTimeMillis();

    File file=new File(filename);
    if(file.exists()){file.delete();}

    ImageOutputStream ios=null;
    try{
      ios=ImageIO.createImageOutputStream(file);
      writer.setOutput(ios);

      if(writer.canWriteSequence()){                               //i.e tiff,sff(fax)
        writer.prepareWriteSequence(null);
        for(int i=0;i<images.getTabCount();i++){
          JScrollPane sp=(JScrollPane)images.getComponentAt(i);
          ImagePanel ip=(ImagePanel)sp.getViewport().getView();
          writer.writeToSequence(getIIOImage(writer,iwp,ip.getImage()),iwp);
        }
        writer.endWriteSequence();
      }else{
        JScrollPane sp=(JScrollPane)images.getComponentAt(0);
        ImagePanel ip=(ImagePanel)sp.getViewport().getView();
        writer.write(null,getIIOImage(writer,iwp,ip.getImage()),iwp);
        for(int i=1; i<images.getTabCount(); i++){
          if(writer.canInsertImage(i)){
            sp=(JScrollPane)images.getComponentAt(i);
            ip=(ImagePanel)sp.getViewport().getView();
            writer.write(null,getIIOImage(writer,iwp,ip.getImage()),iwp);
          }else{
            throw new IOException("Image Writer cannot append image ["+i+"] ("+filename+")");
          }
        }
      }
      time=System.currentTimeMillis()-time;
      System.out.println("Saved : "+filename);
      System.out.println("3\bTime used to save images : "+time);
    }finally{
      if(ios!=null){ios.close();}
    }
  }

  public void convertImage(){
    new Thread(){
      public void run(){
        try{
          ImageTypeConvertOpPanel itcop=new ImageTypeConvertOpPanel();
          ImageTypeConvertOp itco=itcop.activate();
          if(itco!=null){
            for(int i=0; i<images.getTabCount(); i++){
              JScrollPane sp=(JScrollPane)images.getComponentAt(i);
              ImagePanel ip=(ImagePanel)sp.getViewport().getView();

              BufferedImage image=itco.filter(ip.getImage());
 
              ip.setImage(image);
              ip.revalidate();ip.repaint();

              
              String type="Unknown Type";

              switch(image.getType()){
              case BufferedImage.TYPE_BYTE_BINARY:  type="Byte Binary"; break;
              case BufferedImage.TYPE_BYTE_INDEXED: type="Byte Indexed";break;
              }
              ColorModel cm=image.getColorModel();
              System.out.println("9\bConverted Images to:\n\ntype: "+type+"\nbpp: "+cm.getPixelSize());
            }
          }
        }catch(Exception e){
          System.out.println("9\b"+getClass().getName()+".convertImage:\n\t"+e);
          e.printStackTrace();
        }
      }
    }.start();
  }

/*
  public void convertToBWImage(){
    for(int i=0; i<images.getTabCount(); i++){
      JScrollPane sp=(JScrollPane)images.getComponentAt(i);
      ImagePanel ip=(ImagePanel)sp.getViewport().getView();
      BufferedImage original=ip.getImage();
      BufferedImage image=new BufferedImage(original.getWidth(),original.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
      Graphics2D g=image.createGraphics();
      AffineTransform t=new AffineTransform();
      g.drawRenderedImage(original,t);
      ip.setImage(image);
      ip.revalidate();ip.repaint();
    }
  }
*/
  public void propertyChange(final PropertyChangeEvent evt){
/*
    String prop=evt.getPropertyName();
    if(prop.equals("open")){
      JTabbedPane tp=(JTabbedPane)getParent();
      tp.setSelectedIndex(1);
      new Thread(){
        public void run(){
          try{
            open((String)evt.getNewValue());
          }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Image Open Error : "+e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE); 
          }
        }
      }.start();
    }else if(prop.equals("save")){
      new Thread(){
        public void run(){
          try{
            open((String)evt.getNewValue());
            int res=savefc.showSaveDialog(null);
            if(res==JFileChooser.APPROVE_OPTION){
              save(savefc.getSelectedFile().getPath());
            }
          }catch(IOException ioe){
            System.out.println("9\b"+ioe.getMessage());
          }
        }
      }.start();
    }
*/
  }

  static private class Printer extends Thread{
    PrinterJob    pj;
    PageFormat    pf;
    Book          bk;
  
    public Printer(){
      pj=PrinterJob.getPrinterJob();

      pf = pj.defaultPage();
      Paper p=pf.getPaper();
      p.setImageableArea(0.0,0.0,p.getWidth(),p.getHeight());
      pf.setPaper(p);
      pf = pj.validatePage(pf);

      bk=new Book();
    }

    public void append(ImagePanel image){
      bk.append(image, pf);
    }
 
    public void print(){
      pj.setPageable(bk);
      if(pj.printDialog()){
        try{
          pj.print(); 
        }catch (Exception e){
          e.printStackTrace();
          System.out.println("9\b"+getClass().getName()+".print:\n\t"+e.getMessage());
        }
      }
    }
  }
}
