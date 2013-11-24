package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import uk.co.mmscomputing.io.*;
import uk.co.mmscomputing.imageio.sff.*;

public class PMSFFOutputStream extends PMOutputStream{

  RLEBit1OutputStream    rlos;       // byte = 8 black and white pixel
  ModHuffmanOutputStream mhos;       // MH
  SFFOutputStream        sffos;      // CAPI SFF (Simple Fax Format)
  
  public PMSFFOutputStream(OutputStream out){
    super(out);
    rlos=null;mhos=null;sffos=null;
  }

  public PMSFFOutputStream(String filepath)throws IOException{
    this(new FileOutputStream(filepath/*+".sff"*/));
  }

  protected void startDoc()throws IOException{
    sffos = new SFFOutputStream(out);                           // write tiff header
  }

  protected void startPage()throws IOException{
    sffos.setXYResolution(graphicsxres,graphicsyres);
    sffos.writePageHeader(pagewidth);                           // page width sent before StartPage
    mhos   = new ModHuffmanOutputStream(sffos);
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
    sffos.writePageEnd();
  }

  protected void endJob()throws IOException{
    sffos.writeDocumentEnd();
  }

  protected void writeDataByte(int code)throws IOException{
    rlos.write(~code);                                          // Here: black is zero; Want: white is zero
  }
}

