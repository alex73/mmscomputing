package uk.co.mmscomputing.imageio.jpeg;

import java.io.*;
import java.awt.image.*;


abstract public class JFIFOutputStream extends JPEGOutputStream{

  protected int   version=0x102;                                            // 1.02
  protected int   units=0;                                                  // density unit = 0 => Only the aspect ratio is specified.
                                                                            // density unit = 1 => Density in pixels per inch.
                                                                            // density unit = 2 => Density in pixels per centimeter.

  protected int   xDensity=1,yDensity=1;
  protected int   xThumbnail=0,yThumbnail=0;                                // no support yet [2006-01-12]

  protected int[] hv,q,sel;

//  public JFIFOutputStream(OutputStream out){
//    super(out);
//    setTables();
//  }

  public JFIFOutputStream(OutputStream out, int height, int width)throws IOException{
    super(out);                                                             // SOI : start of image

    System.out.println("3\b"+getClass().getName()+"\n\tMMSC-JPEG Encoder.");

    app0(out);                                                              // APP0: straight after SOI
    setTables();                                                            // hv, q, sel
    startOfFrame(height,width,hv,q);                                        // SOF
    startOfScan(sel);                                                       // SOS
  }

  abstract protected void setTables()throws IOException;

  abstract public void writeInt(int c)throws IOException;

  public void write(int[] buf, int off, int len)throws IOException{         // int[]  : all samples per pixel in int
    for(int i=0;i<len;i++){
      writeInt(buf[off+i]);
    }
  }

  public void write(int[] buf)throws IOException{
    write(buf,0,buf.length);
  }

  public void setUnits(int units){this.units=units;}
  public int  getUnits(){return units;}
  public void setXDensity(int xDensity){this.xDensity=xDensity;}
  public int  getXDensity(){return xDensity;}
  public void setYDensity(int yDensity){this.yDensity=yDensity;}
  public int  getYDensity(){return yDensity;}

  protected void app0(OutputStream out)throws IOException{                  // 0xE0
    ByteArrayOutputStream  jfif   =new ByteArrayOutputStream();

    jfif.write('J');                                                        // JFIF identifier
    jfif.write('F');                                            
    jfif.write('I');                                            
    jfif.write('F');                                            
    jfif.write('\0');                                            
    jfif.write((version>>8)&0x000000FF);                                    // version
    jfif.write( version    &0x000000FF);
    jfif.write(units);                                                      // units
    jfif.write((xDensity>>8)&0x000000FF);                                   // Xdensity
    jfif.write( xDensity    &0x000000FF);                                                   
    jfif.write((yDensity>>8)&0x000000FF);                                   // Ydensity
    jfif.write( yDensity    &0x000000FF);                                                   
    jfif.write(xThumbnail);                                                 // Xthumbnail
    jfif.write(yThumbnail);                                                 // Ythumbnail

    // to do thumbnail

    byte[] data = jfif.toByteArray();
    int    len  = data.length+2;

    out.write(0xFF);                                                        // JFIF
    out.write(JPEGConstants.APP0);
    out.write((len>>8)&0x000000FF);
    out.write( len    &0x000000FF);
    out.write(data);
  }

/*
  public void writeHeader(OutputStream out,int height, int width)throws IOException{
    app0(out);                                                              // app0 straight after SOI
      
    defineQuantizationTables();                                             // write QTs
    defineHuffmanTables();                                                  // write HTs

    hv={0x22,0x11,0x11};                                                    // (Hi<<4)|Vi
    q={0,1,1};                                                              // quantization table Y=0, Cb=Cr=1
    startOfFrame(height,width,hv,q);
    sel={0,1,1};                                                            // DC,AC code table Y=0, Cb=Cr=1
    startOfScan(sel);
  }
*/

  public void write(BufferedImage image)throws IOException{
    DataBuffer buffer = image.getRaster().getDataBuffer();
    if(buffer instanceof DataBufferByte){
      write((byte[])((DataBufferByte)buffer).getData());
    }else if(buffer instanceof DataBufferInt){
      write((int[] )((DataBufferInt )buffer).getData());
    }else{
      throw new IllegalArgumentException(getClass().getName()+".write:\n\tCan only deal with byte[] and int[] arrays.");
    }
  }
}

// [1] JPEG File Interchange Format (JFIF)
//     Version 1.02 [1992-09-01]
// http://www.jpeg.org/public/jfif.pdf [last accessed 2005-11-28]

