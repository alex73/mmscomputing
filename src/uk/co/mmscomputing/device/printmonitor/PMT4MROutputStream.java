package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import uk.co.mmscomputing.io.*;
import uk.co.mmscomputing.imageio.tiff.*;

public class PMT4MROutputStream extends PMOutputStream{

  RLEBit1OutputStream    rlos;       // byte = 8 black and white pixel
  ModREADOutputStream    mros;       // T.4 MR
  TIFFClassFOutputStream tiffos;     // TIFF Class F
  
  public PMT4MROutputStream(OutputStream out){
    super(out);rlos=null;mros=null;tiffos=null;
  }

  public PMT4MROutputStream(String filepath)throws IOException{
    this(new FileOutputStream(filepath/*+".tif"*/));
  }

  protected void startDoc()throws IOException{
    tiffos = new TIFFClassFMROutputStream(out);                 // write tiff header
  }

  protected void startPage()throws IOException{
    tiffos.setXYResolution(graphicsxres,graphicsyres);
    tiffos.writePageHeader(pagewidth);                          // page width sent before StartPage
    mros   = new ModREADOutputStream(tiffos,pagewidth);
    rlos   = new RLEBit1OutputStream(mros);                     // print driver: black/white 8 pixel per byte
  }

  protected void sendBlockData()throws IOException{
    super.sendBlockData();
    rlos.setStartCodeWord(0x0001);                              // white run first
    mros.writeEOL();                                            // write EOL
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

