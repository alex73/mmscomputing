package uk.co.mmscomputing.device.printmonitor;

import java.io.*;

abstract public class PMOutputStream extends FilterOutputStream{

  private   String  cmd;                       // buffer for current command sent by printer driver
  private   int     count;                     // count of scan line data still to come

  protected int pagewidth=0;                   // physical page width
  protected int pageheight=0;                  // physical page height
  protected int graphicsxres=0;
  protected int graphicsyres=0;
  protected int numofdatabytes=0;              // number of data send in one block 
                                               // print driver sends only one scanline per block
                                               // =>(numofdatabytes*8<=pagewidth)has to be true
  public PMOutputStream(OutputStream out){
    super(out);cmd="";count=0;
  }

  abstract protected void startDoc()throws IOException;
  abstract protected void startPage()throws IOException;
  
  protected void sendBlockData()throws IOException{
    count=numofdatabytes;                                         // what follows now is 'numofdatabytes' of image data
  }
  abstract protected void writeDataByte(int code)throws IOException;

  protected void endBlockData()throws IOException{
    int len=pagewidth>>3;
    for(int i=numofdatabytes;i<len;i++){writeDataByte(0x00);}
  }

  abstract protected void endPage()throws IOException;
  abstract protected void endJob()throws IOException;

  private int getValue(String cmd){
    try{
      String[] strs=cmd.split("=");
      return Integer.parseInt(strs[1]);      
    }catch(Exception e){
      System.err.println(getClass().getName()+".getValue:\n\t"+e);
      return -1;
    }
  }

  private void processCmd(String cmd)throws IOException{
    if(cmd.startsWith("StartDoc")){                               // StartDoc
      startDoc();
    }else if(cmd.startsWith("PhysPaperWidth")){
      pagewidth=getValue(cmd);                                    // System.out.println("Page Width "+pagewidth);
    }else if(cmd.startsWith("PhysPaperLength")){
      pageheight=getValue(cmd);                                   // System.out.println("Page Height "+pageheight);
    }else if(cmd.startsWith("GraphicsXRes")){
      graphicsxres=getValue(cmd);                                 // System.out.println("X-Resolution in dpi "+graphicsxres);
    }else if(cmd.startsWith("GraphicsYRes")){
      graphicsyres=getValue(cmd);                                 // System.out.println("Y-Resolution in dpi "+graphicsyres);
    }else if(cmd.startsWith("StartPage")){                        // StartPage
      startPage();
    }else if(cmd.startsWith("NumOfDataBytes")){
      numofdatabytes=getValue(cmd);                               // number of bytes to expect in next scan line
      if((numofdatabytes<<3)>pagewidth){
        System.err.println("Number of data bytes ["+numofdatabytes+"*8] is greater than pagewidth ["+pagewidth+"]");
      }
    }else if(cmd.startsWith("SendBlockData")){
      sendBlockData();
    }else if(cmd.startsWith("EndBlockData")){
      endBlockData();
    }else if(cmd.startsWith("EndPage")){                          // EndPage
      endPage();
    }else if(cmd.startsWith("EndJob")){                           // EndJob
      endJob();
    }
  }

  protected void addCmdChar(int code)throws IOException{
    code&=0x00FF;                                                 // otherwise have printer commands
    if(code==';'){
      processCmd(cmd);cmd="";                                     // System.out.println(new String(cmd.getBytes()));
    }else{
      cmd+=(char)code;
    }
  }

  public void write(int code)throws IOException{
    if(count>0){                                                  // if have data bytes write image data
      if(count<=(pagewidth>>3)){                                  // if we have more bytes per line than space then skip first bytes of data
        writeDataByte(code);
      }
      count--;
    }else{
      addCmdChar(code);
    }
  }
}

