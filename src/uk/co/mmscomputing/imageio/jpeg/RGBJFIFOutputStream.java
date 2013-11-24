package uk.co.mmscomputing.imageio.jpeg;

import java.io.*;

public class RGBJFIFOutputStream extends YCbCrJFIFOutputStream{

  public RGBJFIFOutputStream(OutputStream out, int height, int width)throws IOException{
    super(out,height,width);                                   
  }

  // BufferedImage.TYPE_INT_RGB = 1


  public void writeInt(int c)throws IOException{
    double R=(c>>16)&0x000000FF;
    double G=(c>> 8)&0x000000FF;
    double B= c     &0x000000FF;

    int Y  =(int)( 0.299   *R + 0.587    *G + 0.114     *B);          if(Y<0){  Y=0;}else if(Y>255){Y=255;}
    int Cb =(int)(-0.168736*R - 0.331264 *G + 0.5       *B + 128.0);  if(Cb<0){Cb=0;}else if(Cb>255){Cb=255;}
    int Cr =(int)( 0.5     *R - 0.4186876*G - 0.08131241*B + 128.0);  if(Cr<0){Cr=0;}else if(Cr>255){Cr=255;}

    super.write(Y);
    super.write(Cb);
    super.write(Cr);
  }

  private int color    = 0;
  private int colInd   = 0;

  public void write(int c)throws IOException{
    color    <<= 8;
    color    |= (c & 0x00FF);
    colInd++;
    if(colInd==3){
      writeInt(color);
      color  = 0;
      colInd = 0;
    }
  }
}

