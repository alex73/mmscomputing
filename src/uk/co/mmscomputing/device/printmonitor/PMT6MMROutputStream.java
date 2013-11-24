package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import uk.co.mmscomputing.io.*;
import uk.co.mmscomputing.imageio.tiff.*;

public class PMT6MMROutputStream extends PMOutputStream{

  RLEBit1OutputStream       rlos;       // byte = 8 black and white pixel
  ModModREADOutputStream    mmros;      // T.6 MMR
  TIFFClassFOutputStream    tiffos;     // TIFF Class F
  
  public PMT6MMROutputStream(OutputStream out){
    super(out);rlos=null;mmros=null;tiffos=null;
  }

  public PMT6MMROutputStream(String filepath)throws IOException{
    this(new FileOutputStream(filepath/*+".tif"*/));
  }

  protected void startDoc()throws IOException{
    tiffos = new TIFFClassFMMROutputStream(out);                // write tiff header
  }

  protected void startPage()throws IOException{
    tiffos.setXYResolution(graphicsxres,graphicsyres);
    tiffos.writePageHeader(pagewidth);                          // page width sent before StartPage
    mmros  = new ModModREADOutputStream(tiffos,pagewidth);
    rlos   = new RLEBit1OutputStream(mmros);                    // print driver: black/white 8 pixel per byte
  }

  protected void sendBlockData()throws IOException{
    super.sendBlockData();
    rlos.setStartCodeWord(0x0001);                              // white run first
    mmros.writeEOL();                                           // T.6 does not really write EOL; only new line initialization
    tiffos.write(0x0000);tiffos.write(0x0080);                  // signal tiffos new line
  }

  protected void endBlockData()throws IOException{
    super.endBlockData();
    rlos.flush();                                               // write out last run
  }

  protected void endPage()throws IOException{
    mmros.writeEOFB();
    tiffos.writePageEnd();                                      // write page to out
  }

  protected void endJob()throws IOException{
    tiffos.writeDocumentEnd();
  }

  protected void writeDataByte(int code)throws IOException{
    rlos.write(~code);                                          // Here: black is zero; Want: white is zero
  }
}

