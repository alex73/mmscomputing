package uk.co.mmscomputing.imageio.jpeg;

import java.io.*;

public class YCbCrJFIFOutputStream extends JFIFOutputStream{

  public YCbCrJFIFOutputStream(OutputStream out, int height, int width)throws IOException{
    super(out,height,width);                                                
  }

  protected void setTables()throws IOException{
    setZZQuantizationTable(0,JPEGConstants.LQT2);                           // Y,Gray,(RGB) component
    setRawDCHuffmanTable(0,JPEGConstants.HLDCTable);
    setRawACHuffmanTable(0,JPEGConstants.HLACTable);

    setZZQuantizationTable(1,JPEGConstants.CQT2);                           // Cb,Cr Component
    setRawDCHuffmanTable(1,JPEGConstants.HCDCTable);
    setRawACHuffmanTable(1,JPEGConstants.HCACTable);

    defineQuantizationTables();                                             // write QTs
    defineHuffmanTables();                                                  // write HTs

    hv    = new int[3];
    hv[0] = 0x22;                                                           // (Hi<<4)|Vi
    hv[1] = 0x11;
    hv[2] = 0x11;
    q     = new int[3];
    q[0]  = 0;                                                              // quantization table Y=0, Cb=Cr=1
    q[1]  = 1;
    q[2]  = 1;                                                              
    sel   = new int[3];
    sel[0]= 0;                                                              // DC,AC code table Y=0, Cb=Cr=1
    sel[1]= 1;
    sel[2]= 1;
  }

  public void writeInt(int c)throws IOException{
    super.write((c>>16)&0x000000FF);                                        // Y
    super.write((c>> 8)&0x000000FF);                                        // Cb
    super.write( c     &0x000000FF);                                        // Cr
  }

  // use for YCbCr pictures (three components)

//public void write(byte[] buf, int off, int len)throws IOException{        // byte[] : samples divided into bytes
//  super.write(buf,off,len);
//}


}

// [1] JPEG File Interchange Format (JFIF)
//     Version 1.02 [1992-09-01]
// http://www.jpeg.org/public/jfif.pdf [last accessed 2005-11-28]

