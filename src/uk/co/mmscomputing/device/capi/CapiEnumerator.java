package uk.co.mmscomputing.device.capi;

import java.io.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class CapiEnumerator{

  static private int nobc=0;               //  number of b-channels
  static private CapiController[] ctrls=new CapiController[0];

  static private void updateControllers(){
    try{
      nobc=0;
      int noc=jcapi.getNoOfControllers();
      ctrls=new CapiController[noc];

      int i=0,n=0;
      while(i<noc){
        n++;                                          // capi starts counting at 1
        try{
          ctrls[i]=new CapiController(n);
          nobc+=ctrls[i].getNoOfBChannels();
          i++;
        }catch(Exception e){
//          e.printStackTrace();
        }
      }
    }catch(Exception e){
      System.err.println(CapiEnumerator.class.getName()+"\n\t"+e.getMessage());
    }
  }

//  static public int getNoOfControllers(){
//    return ctrls.length;
//  }

  static public int getNoOfBChannels(){ 
    return nobc;
  }

  static public CapiController[] getControllers(){ 
    return ctrls;
  }

  static public CapiController getController(int id)throws CapiException{ 
    for(int n=0;n<ctrls.length;n++){
      CapiController cntl = ctrls[n];
      if(cntl.getId()==id){ return cntl;}
    }
    if(nobc>0){
      CapiController cntl = ctrls[0];
      System.err.println("CapiEnumerator.getControllerById:\n\tUse controller "+cntl.getId()+" instead of "+id);
      return cntl;
    }
    throw new CapiException("CapiEnumerator.getControllerById:\n\tCannot find any controller.");
  }

  static public CapiController getController(CapiChannel channel)throws CapiException{ 
    return getController(channel.getCtrlId());
  }

  public CapiEnumerator(){
  }

  public String toString(){
    String s="";
    s+="CapiEnumerator.getNoOfControllers() = "+ctrls.length+"\n\n\n";
    for(int i=0;i<ctrls.length;i++){
      s+="CapiEnumerator.getController("+ctrls[i].getId()+"):\n\n"+ctrls[i].toString()+"\n\n";
    }
    return s;
  }

  static{
    try{
      jcapi.checkInstalled();
      updateControllers();
    }catch(Exception e){
      System.err.println(CapiEnumerator.class.getName()+"\n\t"+e.getMessage());
    }
  }
}