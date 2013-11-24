package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import java.util.*;

/*
  BOOL WINAPI Monitor_EnumPorts    (LPWSTR servername,DWORD dwLevel,LPBYTE pPorts,DWORD cbBuf,LPDWORD pdwNeeded,LPDWORD pdwReturned);
  BOOL WINAPI Monitor_OpenPort     (LPWSTR portname,PHANDLE porthandle);
  BOOL WINAPI Monitor_StartDocPort (HANDLE hPort,LPWSTR pPrinterName,DWORD JobId,DWORD Level,LPBYTE pDocInfo);
  BOOL WINAPI Monitor_WritePort    (HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbWritten);
  BOOL WINAPI Monitor_ReadPort     (HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbRead);
  BOOL WINAPI Monitor_EndDocPort   (HANDLE hPort);
  BOOL WINAPI Monitor_ClosePort    (HANDLE hPort);
  BOOL WINAPI Monitor_AddPort      (LPWSTR servername,HWND hWnd,LPWSTR monitorname);
  BOOL WINAPI Monitor_ConfigurePort(LPWSTR servername,HWND hWnd,LPWSTR portname);
  BOOL WINAPI Monitor_DeletePort   (LPWSTR servername,HWND hWnd,LPWSTR portname);
*/

public class jprintmonitor{

  static String   cn       = "uk.co.mmscomputing.device.printmonitor.jprintmonitor";
  static PortList portlist = new PortList();

// Printer Properties Port Tab: enum, add, configure and delete ports

// BOOL WINAPI Monitor_EnumPorts(LPWSTR servername,DWORD dwLevel,LPBYTE pPorts,DWORD cbBuf,LPDWORD pdwNeeded,LPDWORD pdwReturned);

  static private int getPortInfoSize(int level){       // tell print spooler the buffer size we need
    try{
      System.out.println("getPortInfoSize level "+level);
      return portlist.getPortInfoSize(level);
    }catch(Throwable e){
      System.err.println(cn+".getPortInfoSize:\n\t"+e);
    }
    return -1;
  }

  static private int enumPorts(String servername,int level,int cptr,byte[] buffer){
    try{
      System.out.println("enumPorts level "+level+" ptr = 0x"+Integer.toHexString(cptr)+" buffer.length "+buffer.length); 
      portlist.writePortInfo(level,cptr,buffer);
      return portlist.size();
    }catch(Throwable e){
      System.err.println(cn+".enumPorts:\n\t"+e);
    }
    return 0;
  }

//  BOOL WINAPI Monitor_AddPort(LPWSTR servername,HWND hWnd,LPWSTR monitorname);

  static private boolean addPort(String servername,int handle,String monitorname){
    try{
      Port port=new Port("make_my_name_unique",monitorname,"MMSC Print Port");
      if(port!=null){
        new PMConfigurationPanel(port,true).display();
        boolean res=portlist.add(port);
        savePorts();
        return res;
      }
      System.out.println("Cannot add Port. New port could not be allocated.");
    }catch(Throwable e){
      System.err.println(cn+".addPort:\n\t"+e);
    }
    return false;
  }

//  BOOL WINAPI Monitor_ConfigurePort(LPWSTR servername,HWND hWnd,LPWSTR portname);

  static private boolean configurePort(String servername,int handle,String portname){
    try{
      Port port=portlist.find(portname);
      if(port!=null){
        new PMConfigurationPanel(port,false).display();
        savePorts();
        return true;
      }
      System.out.println("Cannot configure Port "+portname+". Port could not be found.");
    }catch(Throwable e){
      System.err.println(cn+".configurePort:\n\t"+e);
    }
    return false;
  }

//  BOOL WINAPI Monitor_DeletePort(LPWSTR servername,HWND hWnd,LPWSTR portname);

  static private boolean deletePort(String servername,int handle,String portname){
    try{
      Port port=portlist.find(portname);
      if(port!=null){
        boolean res=portlist.remove(port);
        savePorts();
        return res;
      }
      System.out.println(cn+".deletePort:\n\tCannot delete Port "+portname+". Port could not be found.");
    }catch(Throwable e){
      System.err.println(cn+".deletePort:\n\t"+e);
    }
    return false;
  }

// BOOL WINAPI Monitor_OpenPort(LPWSTR portname,PHANDLE porthandle);

  static private Port openPort(String portname){
    try{
  	  Port port=portlist.find(portname);
      if((port!=null)&&port.open()){
        return port;
      }
    }catch(Throwable e){
      System.err.println(cn+".openPort:\n\t"+e);
    }
    return null;
  }

  static private String     propertiesdir;                          // XP: c:\windows\system32\mmsc

  static private File getPropertiesFile(String parent){
    String filename="uk.co.mmscomputing.device.printmonitor.properties.txt";
    try{   
      return new File(parent,filename);
    }catch(Throwable e){
      System.err.println(cn+".getPropertiesFile:\n\tCould not create directory:\n\t"+parent+"\n\t"+e);
      return new File(filename);
    }
  }

  static private void loadPorts(){
    try{   
      File propertiesFile=getPropertiesFile(propertiesdir);
      if(propertiesFile.exists()){
        Properties properties=new Properties();
        properties.load(new FileInputStream(propertiesFile));
        portlist.load(properties);
      }
    }catch(Throwable e){
      System.err.println(cn+".loadPorts:\n\t"+e);
    }
  }

  static void savePorts(){
    try{
      Properties properties=new Properties();
      portlist.save(properties);
      File propertiesFile=getPropertiesFile(propertiesdir);
      properties.store(new FileOutputStream(propertiesFile),propertiesFile.getAbsolutePath());
    }catch(Throwable e){
      System.err.println(cn+".savePorts:\n\t"+e);
    }
  }

  static{
    try{
      propertiesdir=new File(System.getProperty("user.dir"),"uk.co.mmscomputing").getAbsolutePath();
      new File(propertiesdir).mkdirs();
      uk.co.mmscomputing.util.log.LogStream.redirectSystemOutToFile(new File(propertiesdir,"log.txt").getAbsolutePath());
      uk.co.mmscomputing.util.log.LogStream.redirectSystemErrToFile(new File(propertiesdir,"err.txt").getAbsolutePath());
      loadPorts();
    }catch(Throwable e){
      System.err.println(cn+".<static>:\n\t"+e);
    }
  }
}