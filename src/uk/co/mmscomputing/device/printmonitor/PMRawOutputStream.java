package uk.co.mmscomputing.device.printmonitor;

import java.io.*;

public class PMRawOutputStream extends PMOutputStream{

  public PMRawOutputStream(String filepath)throws IOException{
    super(new FileOutputStream(filepath));
  }

  protected void startDoc()throws IOException{}
  protected void startPage()throws IOException{}
  protected void sendBlockData()throws IOException{}
  protected void endBlockData()throws IOException{}
  protected void endPage()throws IOException{}
  protected void endJob()throws IOException{}

  protected void writeDataByte(int code)throws IOException{
    out.write(code);                                 // write every byte we get to output stream
  }

  public void write(int code)throws IOException{
    writeDataByte(code);                        
  }
}

