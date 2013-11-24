package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFBody extends PDFObject{

  // list of indirect objects
  // comment: from % until end of line; exception PDFString

  private PDFIndirectObjects     list;    // list of indirect objects
  private PDFCrossReferenceTable xref;
  private PDFCatalog             catalog;

  private PDFIndirectObject      defaultFont;

  public PDFBody(PDFCrossReferenceTable xreftable){ 
    super();
    list    = new PDFIndirectObjects();
    xref    = xreftable;
    catalog = new PDFCatalog(this);
  }
 
  public PDFCatalog getCatalog(){return catalog;}
  public PDFIndirectReference getRoot(){return catalog.getReference();}
  public PDFIndirectObjects getObjects(){return list;}
  public PDFObject.PDFInteger getSize(){return new PDFObject.PDFInteger(list.getSize());}

  public PDFIndirectObject getIndirectObject(PDFObject object){
    return list.add(object);  
  }

  public PDFIndirectReference getIndirectReference(PDFObject object){
    return new PDFIndirectReference(getIndirectObject(object));
  }

  public PDFIndirectObject getIndirectObject(int on,int gn){
    return list.add(on,gn);  
  }

  public PDFIndirectReference getDefaultFont(){  
    if(defaultFont==null){
      PDFDictionary font = new PDFDictionary();
      font.put("Type",new PDFObject.PDFName("Font"));
      font.put("Subtype",new PDFObject.PDFName("Type1"));
      font.put("Name",new PDFObject.PDFName("defaultFont"));
      font.put("BaseFont",new PDFObject.PDFName("Helvetica"));
      font.put("Encoding",new PDFObject.PDFName("MacRomanEncoding"));
      defaultFont = list.add(font);
    }
    return new PDFIndirectReference(defaultFont);
  }

  public void read(PDFScanner s,PDFCrossReferenceTable xref)throws IOException{
  }

  public void write(PDFFile out)throws IOException{
    list.write(out);
    list.setXRefTable(xref);
  }

  public String toString(){
    return list.toString();
  }
}

