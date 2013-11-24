package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;

import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class PDFImageReader extends ImageReader{

  private Vector images;

  protected PDFImageReader(ImageReaderSpi originatingProvider){
    super(originatingProvider);
  }

  public BufferedImage read(int imageIndex, ImageReadParam param)throws IOException{
    readImages();
    checkIndex(imageIndex);
    return (BufferedImage)images.elementAt(imageIndex);
  }

  public int getHeight(int imageIndex)throws IOException{
    readImages();
    checkIndex(imageIndex);
    return ((BufferedImage)images.elementAt(imageIndex)).getHeight();
  }

  public int getWidth(int imageIndex)throws IOException{
    readImages();
    checkIndex(imageIndex);
    return ((BufferedImage)images.elementAt(imageIndex)).getWidth();
  }

  public Iterator getImageTypes(int imageIndex)throws IOException{
    readImages();
    checkIndex(imageIndex);

    ImageTypeSpecifier imageType = null;
    java.util.List l = new ArrayList();
    imageType=ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

    l.add(imageType);
    return l.iterator();
  }

  public int getNumImages(boolean allowSearch)throws IOException{
    readImages();
    return images.size();
  }

  public IIOMetadata getImageMetadata(int imageIndex)throws IOException{
    readImages();
    checkIndex(imageIndex);
    return null;
  }

  public IIOMetadata getStreamMetadata() throws IOException{
    return null;
  }

  private void checkIndex(int imageIndex) {
    if (imageIndex > images.size()) {
      throw new IndexOutOfBoundsException("\n"+getClass().getName()+".checkIndex:\n\tBad index in image reader");
    }
  }

  private void readImages()throws IOException{
    if(images==null){
      try{
        images = new Vector();
        PDFFile file = new PDFFile();
        file.read((ImageInputStream)getInput());
        int index = 0;
        while(true){
          BufferedImage image=file.getImage(index);
          if(image==null){break;}
          images.add(image);
          index++;
        }
      }catch(Exception e){
        e.printStackTrace();
        throw new IOException(getClass().getName()+".readImages:\n\t"+e);
      }
    }
  }
}
