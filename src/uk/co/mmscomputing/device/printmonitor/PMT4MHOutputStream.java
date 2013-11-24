package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import uk.co.mmscomputing.io.*;
import uk.co.mmscomputing.imageio.tiff.*;

public class PMT4MHOutputStream extends PMOutputStream{

  RLEBit1OutputStream    rlos;       // byte = 8 black and white pixel
  ModHuffmanOutputStream mhos;       // T.4 MH
  TIFFClassFOutputStream tiffos;     // TIFF Class F
  
  public PMT4MHOutputStream(OutputStream out){
    super(out);
    rlos=null;mhos=null;tiffos=null;
  }

  public PMT4MHOutputStream(String filepath)throws IOException{
    this(new FileOutputStream(filepath/*+".tif"*/));
  }

  protected void startDoc()throws IOException{
    tiffos = new TIFFClassFMHOutputStream(out);                 // write tiff header
  }

  protected void startPage()throws IOException{
    tiffos.setXYResolution(graphicsxres,graphicsyres);
    tiffos.writePageHeader(pagewidth);                          // page width sent before StartPage
    mhos   = new ModHuffmanOutputStream(tiffos);
    rlos   = new RLEBit1OutputStream(mhos);                     // print driver: black/white 8 pixel per byte
  }

  protected void sendBlockData()throws IOException{
    super.sendBlockData();
    rlos.setStartCodeWord(0x0001);                              // white run first
    mhos.writeEOL();                                            // write EOL
  }

  protected void endBlockData()throws IOException{
    super.endBlockData();
    rlos.flush();                                               // write out last run
  }

  protected void endPage()throws IOException{    
    tiffos.writePageEnd();
  }

  protected void endJob()throws IOException{
    tiffos.writeDocumentEnd();
  }

  protected void writeDataByte(int code)throws IOException{
    rlos.write(~code);                                          // Here: black is zero; Want: white is zero
  }
}

