package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import java.util.*;

public class PortList extends Vector{

  static private final int PORT_INFO_1_SIZE=4;
  static private final int PORT_INFO_2_SIZE=20;

  PortList(){
  }

  int getPortInfoSize(int level){
    switch(level){
    case 1:case 2:
      int len=0;
      Enumeration e = elements();
      while(e.hasMoreElements()){
        Port port=(Port)e.nextElement();
        len+=port.getPortInfoSize(level);
      }  
      return len;
    default: 
      return -1;
    }
  }

  private int writeInt(byte[] buffer,int off,int i)throws IOException{
    buffer[off++]=(byte)(i&0x000000FF);		      // first : LSB least significant byte
    buffer[off++]=(byte)((i>>8)&0x000000FF);
    buffer[off++]=(byte)((i>>16)&0x000000FF);
    buffer[off++]=(byte)((i>>24)&0x000000FF);		// last:   MSB most significant byte
    return off;
  }

  void writePortInfo(int level,int ptr,byte[] buffer)throws IOException{
    Port port;
    int  ioff=0;
    int  soff=buffer.length;
    Enumeration e = elements();
    while(e.hasMoreElements()){
      port=(Port)e.nextElement();
      soff=port.writePortInfo(level,ptr,buffer,ioff,soff);
      switch(level){
      case 1: ioff+=PORT_INFO_1_SIZE;break;
      case 2: ioff+=PORT_INFO_2_SIZE;break;
      default:return;
      }
    }
  }

  Port find(String portname){
    Port        port;
    Enumeration e = elements();
    while(e.hasMoreElements()){
      port=(Port)e.nextElement();
      if(port.getName().equals(portname)){
        return port;
      }
    }
    return null;
  }

  boolean add(Port port){
    System.out.println("Add Port "+port.getName());
    return super.add(port);
  }

  boolean remove(String portname){
    Port port=find(portname);
    if(port!=null){
      System.out.println("Remove Port "+port.getName());
      return super.remove(port);
    }
    return false;
  }

  void save(Properties properties){
    try{
      int         i=0;
      Port        port;
      Enumeration e = elements();
      while(e.hasMoreElements()){
        port=(Port)e.nextElement();
        port.save(properties,i++);
      }
    }catch(Exception e){
      System.out.println(getClass().getName()+".save:\n\t"+e);
    }
  }

  void load(Properties properties){
    try{
      Port        port;
      String      key,value;
      Enumeration e=properties.propertyNames();
      while(e.hasMoreElements()){
        key=(String)e.nextElement();
        if(key.endsWith(".name")){
          value=properties.getProperty(key);
          port=new Port(value,"jprintmonitor","MMSC Print Port");
          port.load(properties,Integer.parseInt(key.split("\\.")[1]));
          add(port);
        }
      }
    }catch(Exception e){
      System.out.println(getClass().getName()+".load:\n\t"+e);
    }
  }
}
