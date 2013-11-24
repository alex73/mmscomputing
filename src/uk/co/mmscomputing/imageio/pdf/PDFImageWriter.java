package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

import java.awt.image.*;

import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class PDFImageWriter extends ImageWriter{

  private PDFFile pdffile;
  private int     index = 0;

  protected PDFImageWriter(ImageWriterSpi originatingProvider){
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
    return new PDFImageWriteParam(getLocale());
  }

  public boolean canInsertImage(int imageIndex)throws IOException{
    super.canInsertImage(imageIndex);
    return (imageIndex==0);                //  use sequence for more than one picture
  }

  public void write(IIOMetadata streamMetadata,IIOImage img,ImageWriteParam param)throws IOException{
    prepareWriteSequence(streamMetadata);
    writeToSequence(img,param);            //  just one page !
    endWriteSequence();
  }

  public boolean canWriteSequence(){
    return true;
  }

/*
  private void writeln(String line)throws IOException{
    out.write(line.getBytes());
    out.write('\n');
  }
*/

  public void prepareWriteSequence(IIOMetadata streamMetadata)throws IOException{
    pdffile=new PDFFile();
  }

  public void writeToSequence(IIOImage img,ImageWriteParam param)throws IOException{
    if(!(img.getRenderedImage() instanceof BufferedImage)){
      throw new IOException(getClass().getName()+".writeToSequence:\n\tCan only write BufferedImage objects");
    }
    BufferedImage image=(BufferedImage)img.getRenderedImage();

    PDFPage       page = pdffile.getNewPage();
    String        name = "image"+(index++);
    page.addImage(name,image);
  }

  public void endWriteSequence()throws IOException{
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    pdffile.write(baos);
    ImageOutputStream ios=(ImageOutputStream)getOutput();
    ios.write(baos.toByteArray());
    ios.flush();
    ios.close();
  }
}
