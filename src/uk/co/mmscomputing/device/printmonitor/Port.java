package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import java.util.*;

public class Port{

  static private final int PS_STARTDOC = 0x001;
  static private final int PS_OPENED   = 0x002;

  private String         name;               // PORT_INFO_2,PORT_INFO_2
  private String         monitor;            // PORT_INFO_2
  private String         description;        // PORT_INFO_2
  
  private String         documentspath;
  private String         document;
  private String         printer;
  private int            status;
  private int            job;
  private OutputStream   out=null;

  private int            outputmode=PMOutputStreamFactory.T6MMR;

  Port(String name,String monitor,String description){
    this.monitor=monitor;
    this.name=name;
    this.description=description;

    this.documentspath="";
    this.outputmode=PMOutputStreamFactory.T6MMR;
  }

  public String getName(){return name;}
  public void   setName(String name){this.name=name;}

  public int    getJob(){return job;}
  public String getPrinter(){return printer;}

  public String getDescription(){return description;}
  public void   setDescription(String description){this.description=description;}

  public int  getOutputMode(){return outputmode;}
  public void setOutputMode(int mode){outputmode=mode;}

  public String getDocumentsPath(){return documentspath;}
  public void   setDocumentsPath(String path){documentspath=path;}

  private int getSize(String s){ return (s.length()+1)*2;}

  int getPortInfoSize(int level){
    switch(level){
    case 1: return  4+getSize(name);
    case 2: return 20+getSize(name)+getSize(monitor)+getSize(description);
    }
    return 0;
  }

  private int writeInt(byte[] buffer,int off,int i)throws IOException{
    buffer[off++]=(byte)(i&0x000000FF);		      // first : LSB least significant byte
    buffer[off++]=(byte)((i>>8)&0x000000FF);
    buffer[off++]=(byte)((i>>16)&0x000000FF);
    buffer[off++]=(byte)((i>>24)&0x000000FF);		// last:   MSB most significant byte
    return off;
  }

  private int writeChar(byte[] buffer,int off,char i)throws IOException{
    buffer[--off]=(byte)((i>>8)&0x000000FF);		// last:   MSB most significant byte
    buffer[--off]=(byte)(i&0x000000FF);		      // first : LSB least significant byte
    return off;
  }

  private int writeString(byte[] buffer,int off,String s)throws IOException{
    off=writeChar(buffer,off,(char)0);          // 0 terminated
    for(int i=s.length()-1;i>=0;i--){
      off=writeChar(buffer,off,s.charAt(i));
    }
    return off;
  }

  int writePortInfo(int level,int ptr,byte[] buffer,int ioff,int soff)throws IOException{
    switch(level){
    case 1: 
      soff=writeString(buffer,soff,name);
      writeInt(buffer,ioff,ptr+soff);           // offset to port name string
      break;  
    case 2: 
      soff=writeString(buffer,soff,monitor);
      writeInt(buffer,ioff+4,ptr+soff);         // offset to monitor name string
      soff=writeString(buffer,soff,description);
      writeInt(buffer,ioff+8,ptr+soff);         // offset to port descriptor string
      soff=writeString(buffer,soff,name);
      writeInt(buffer,ioff,ptr+soff);           // offset to port name string
      break; 
    }
    return soff;
  }

  synchronized boolean open(){                    // called only once when spooler needs it for first time
    try{
      System.out.println("Open port "+name);
      status|=PS_OPENED;
      return true;
    }catch(Throwable e){
      System.err.println(getClass().getName()+".open:\n\t"+e);
      status&=~PS_OPENED;
      return false;
    }
  }

//  BOOL WINAPI Monitor_StartDocPort(HANDLE hPort,LPWSTR pPrinterName,DWORD JobId,DWORD Level,LPBYTE pDocInfo);

  synchronized boolean start(String printer,int job,String document){
	  if((status&PS_STARTDOC)!=0){return false;}
    try{
      this.printer=printer;
      this.document=document;
      this.job=job;

      System.out.println("Start printing \""+document+"\" on printer "+printer+" to file "+documentspath+" output mode = "+outputmode);

      out=PMOutputStreamFactory.getOutputStream(outputmode,documentspath,document);
      if(out!=null){
        status|=PS_STARTDOC;
        return true;
      }else{
        status&=~PS_STARTDOC;
        return false;
      }
    }catch(Throwable e){
      System.err.println(getClass().getName()+".start:\n\t"+e);
      status&=~PS_STARTDOC;
      return false;
    }
  }

// BOOL WINAPI Monitor_WritePort(HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbWritten);

  int write(byte[] buffer){
    try{                                //      System.out.println("Write "+buffer.length+" bytes");
      out.write(buffer);                   
      return buffer.length;
    }catch(Throwable e){
      System.err.println(getClass().getName()+".write:\n\t"+e);
      return -1;
    }
  }

// BOOL WINAPI Monitor_EndDocPort(HANDLE hPort);

  boolean end(){
    try{
      System.out.println("Stop printing \""+document+"\" on printer "+printer);
      out.close();
      out=null;
    }catch(Throwable e){
      System.err.println(getClass().getName()+".end:\n\t"+e);
    }
    status&=~PS_STARTDOC;
    printer="";
    document="";
    job=-1;
    return true;
  }

// BOOL WINAPI Monitor_ClosePort(HANDLE hPort);

  boolean close(){
    try{
      System.out.println("Closed port "+name);
    }catch(Throwable e){
      System.err.println(getClass().getName()+".close:\n\t"+e);
    }
    status&=~PS_OPENED;
    return true;
  }

  void save(Properties properties,int index){
    try{
      String id="port."+index;
      properties.setProperty(id+".name",name);
      properties.setProperty(id+".path",documentspath);    
      properties.setProperty(id+".desc",description);    
      properties.setProperty(id+".mode",Integer.toString(outputmode));
    }catch(Exception e){
    }
  }

  void load(Properties properties,int index){
    try{
      String id="port."+index;
      name                       =properties.getProperty(id+".name");
      documentspath              =properties.getProperty(id+".path");    
      description                =properties.getProperty(id+".desc");    
      outputmode=Integer.parseInt(properties.getProperty(id+".mode"));
    }catch(Exception e){
      outputmode=PMOutputStreamFactory.T6MMR;
    }
  }
}