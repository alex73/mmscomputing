package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFArray extends PDFObject{

  private Vector value=new Vector();

  public void setValue(Vector v){value=v;}
  public Vector getValue(){ return value;}

  public int size(){ return value.size();}

  public void add(PDFObject obj){
    if(obj instanceof PDFIndirectObject){
      obj=new PDFIndirectReference((PDFIndirectObject)obj);
    }
    value.add(obj);
  }

  public PDFObject elementAt(int index){ return (PDFObject)value.elementAt(index);}

  public void read(PDFScanner s)throws IOException{
      //  scanned [ T_ARRAY_START
    s.scan();
    while(s.symbol!=T_ARRAY_END){
      add(readObject(s));
    }
  }

  public void write(PDFFile out)throws IOException{
    out.write('[');
    Enumeration e = value.elements();
    while(e.hasMoreElements()){
      ((PDFObject)e.nextElement()).write(out);
    }
    out.write(']');
  }

  public String toString(){
    String s="[";
    Enumeration e = value.elements();
    while(e.hasMoreElements()){
      s+=((PDFObject)e.nextElement()).toString();
    }
    s+="]";
    return s;
  }
}

