package uk.co.mmscomputing.imageio.pdf;

import java.io.*;

public class PDFIndirectReference extends PDFObject{

  private PDFIndirectObject obj;

  public PDFIndirectReference(PDFIndirectObject obj){
    this.obj=obj;
  }

//  public void setDirectObject(PDFObject v){obj.setDirectObject(v);}
//  public PDFObject  getDirectObject(){ return obj.getDirectObject();}

  public PDFIndirectObject  getIndirectObject(){ return obj;}

  public String toString(){
    return obj.getObjectNumber() +" "+obj.getGenerationNumber()+" R ";
  }
}

