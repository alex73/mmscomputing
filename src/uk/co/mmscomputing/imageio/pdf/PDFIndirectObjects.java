package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFIndirectObjects{
  private Vector list;

  public PDFIndirectObjects(){
    list = new Vector();
    addNotInUse(65535);
  }
  
  public int getSize(){
    return list.size();
  }

  public PDFIndirectObject add(PDFObject object){
    PDFIndirectObject obj = new PDFIndirectObject(list.size(),object);
    list.add(obj);
    return obj;
  }

  public PDFIndirectObject add(int on,int gn){
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      PDFIndirectObject obj = (PDFIndirectObject)e.nextElement();
      if((obj.getObjectNumber()==on)&&(obj.getGenerationNumber()==gn)){
        return obj;
      }
    }
    PDFIndirectObject obj = new PDFIndirectObject(on,gn,true);
    list.add(obj);
    return obj;
  }

  public void addNotInUse(int nextFreeEntry){
    list.add(new PDFIndirectObject(list.size(),nextFreeEntry,false));
  }

  public void write(PDFFile out)throws IOException{
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      ((PDFObject)e.nextElement()).write(out);
    }
  }

  public String toString(){
    String s="";
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      s+=((PDFObject)e.nextElement()).toString();
    }
    return s;
  }

  public void setXRefTable(PDFCrossReferenceTable xref){
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      PDFIndirectObject entry = (PDFIndirectObject)e.nextElement();
      xref.add(entry.getXRefEntry());
    }
  }
}

