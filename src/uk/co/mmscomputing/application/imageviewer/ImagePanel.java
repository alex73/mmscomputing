package uk.co.mmscomputing.application.imageviewer;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.swing.*;

public class ImagePanel extends JComponent implements Printable{

  protected BufferedImage image=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);

  public ImagePanel(){
  }

  public BufferedImage getImage(){return image;}

  public void setImage(BufferedImage image){
    this.image=image;repaint();
  }

  public void paint(Graphics gc){
    Graphics2D g=(Graphics2D)gc;
    g.drawImage(image,0,0,null);
  }

  public Dimension getPreferredSize(){
    return new Dimension(image.getWidth(),image.getHeight());
  }

  public int print(Graphics gc, PageFormat pf, int page) throws PrinterException{
    Graphics2D g=(Graphics2D)gc;
    double xs = pf.getImageableWidth()/image.getWidth();
    double ys = pf.getImageableHeight()/image.getHeight();
    double newscale = Math.min(xs, ys);
    g.translate(pf.getImageableX(),pf.getImageableY());
    g.drawImage(image,0,0,(int)(image.getWidth()*newscale),(int)(image.getHeight()*newscale),this);
    return Printable.PAGE_EXISTS;
  }  

  public void rotate(){
    BufferedImage oldimage=image;
    try{
      int w=image.getWidth(),h=image.getHeight(),t=image.getType();
      image=newImage(w,h,t);
      Graphics2D g=image.createGraphics();
      AffineTransform rotate = new AffineTransform(0.0,1.0,-1.0,0.0,(double)oldimage.getHeight(),0.0);
      g.drawRenderedImage(oldimage,rotate);
      setSize(image.getWidth(),image.getHeight());
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+"\n\t.rotate:\n\t"+e.getMessage());
      image=oldimage;
      e.printStackTrace();
    }catch(OutOfMemoryError e){
      System.out.println("9\b"+getClass().getName()+"\n\t.rotate:\n\t"+e.getMessage());
      image=oldimage;
    }
  }

  static public BufferedImage newImage(int w,int h,int t)throws Exception{
    try{
      return new BufferedImage(h,w,t);
    }catch(OutOfMemoryError mem){
      System.err.println("start gc ; free memory "+Runtime.getRuntime().freeMemory());
      Runtime.getRuntime().gc();
      System.err.println("end gc ; free memory "+Runtime.getRuntime().freeMemory());
      try{
        return new BufferedImage(h,w,t);
      }catch(java.lang.OutOfMemoryError me){
        throw new Exception("uk.co.mmscomputing.application.imageviewer.newImage:\n\t"+me.getMessage());    
      }
    }
  }

}
