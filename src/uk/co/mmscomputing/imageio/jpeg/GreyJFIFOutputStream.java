package uk.co.mmscomputing.imageio.jpeg;

import java.io.*;

public class GreyJFIFOutputStream extends JFIFOutputStream{

  public GreyJFIFOutputStream(OutputStream out, int height, int width)throws IOException{
    super(out,height,width);                                                
  }

  protected void setTables()throws IOException{
    setZZQuantizationTable(0,JPEGConstants.LQT2);                           // Gray component
    setRawDCHuffmanTable(0,JPEGConstants.HLDCTable);
    setRawACHuffmanTable(0,JPEGConstants.HLACTable);

    defineQuantizationTables();                                             // write QTs
    defineHuffmanTables();                                                  // write HTs

    hv    = new int[1];
    hv[0] = 0x11;                                                           // (Hi<<4)|Vi
    q     = new int[1];
    q[0]  = 0;                                                              // quantization table Y=0, Cb=Cr=1
    sel   = new int[1];
    sel[0]= 0;                                                              // DC,AC code table Y=0, Cb=Cr=1
  }

  public void writeInt(int c)throws IOException{
    throw new IllegalArgumentException(getClass().getName()+".writeInt:\n\tCan only deal with byte arrays.");
  }
}

