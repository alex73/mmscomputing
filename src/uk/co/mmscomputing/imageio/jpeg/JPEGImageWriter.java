package uk.co.mmscomputing.imageio.jpeg;

import java.io.*;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class JPEGImageWriter extends ImageWriter{

  protected JPEGImageWriter(ImageWriterSpi originatingProvider){
    super(originatingProvider);
  }

  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param){
    return null;
  }

  public IIOMetadata convertStreamMetadata(IIOMetadata inData,ImageWriteParam param){
    return null;
  }

  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,ImageWriteParam param){
    return null;
  }

  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param){
    return null;
  }

  public ImageWriteParam getDefaultWriteParam(){
    return new ImageWriteParam(getLocale());
  }

  public boolean canInsertImage(int imageIndex)throws IOException{
    return (imageIndex==0);
  }

  public void write(IIOMetadata streamMetadata,IIOImage img,ImageWriteParam param)throws IOException{
    ImageOutputStream out=(ImageOutputStream)getOutput();

    if(!(img.getRenderedImage() instanceof BufferedImage)){
      throw new IOException(getClass().getName()+"write:\nCan only write BufferedImage objects");
    }
    ByteArrayOutputStream  baos = new ByteArrayOutputStream();
    write(baos,(BufferedImage)img.getRenderedImage());
    out.write(baos.toByteArray());                                   // write to image stream
  }

  static public void write(OutputStream out, BufferedImage image)throws IOException{
    int width  = image.getWidth();
    int height = image.getHeight();

    JFIFOutputStream os = null;
    try{  
      System.out.println("3\b"+JPEGImageWriter.class.getName()+"\n\tMMSC-JPEG Encoder: type = "+image.getType());
      switch(image.getType()){
      case BufferedImage.TYPE_INT_RGB:      os = new RGBJFIFOutputStream(out,height,width);   break; //  1
      case BufferedImage.TYPE_INT_BGR:                                                               //  4
      case BufferedImage.TYPE_3BYTE_BGR:    os = new BGRJFIFOutputStream(out,height,width);   break; //  5
      case BufferedImage.TYPE_BYTE_GRAY:    os = new GreyJFIFOutputStream(out,height,width);  break; // 10
      case BufferedImage.TYPE_BYTE_INDEXED: 
//      os = new IndexedJFIFOutputStream(out,height,width);  break; // 13
      default:                                                                                       // three components; YCbCr
        os = new RGBJFIFOutputStream(out,height,width);
        for(int y=0;y<height;y++){
          for(int x=0;x<width;x++){
            os.writeInt(image.getRGB(x,y));
          }
        }
        os.close();
        return;
      }
      os.write(image);
    }catch(Exception e){
      e.printStackTrace();
      throw new IOException("3\b"+JPEGImageWriter.class.getName()+".write:\n\tCould not write image due to :\n\t"+e.getMessage());
    }finally{
      if(os!=null){os.close();}                                        // EOF: end of frame
    }
  }
}
