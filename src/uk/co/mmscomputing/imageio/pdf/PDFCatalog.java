package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFCatalog extends PDFDictionary{

  // reference to tree of pages
  // reference to tree of objects representing the outline

  private PDFDocument.PDFPages pages;
//  private PDFOutlineTree       outlines;
  private PDFIndirectReference ref;

  public PDFCatalog(PDFBody body){
    this.ref    = body.getIndirectReference(this);
 
    put("Type",new PDFObject.PDFName("Catalog"));
    pages=new PDFDocument.PDFPages(body,null);
    put("Pages",pages.getReference());
//    outlines=new PDFOutlineTree();
//    put("Outlines",outlines);
  }

  public PDFIndirectReference getReference(){return ref;}

  public PDFPage getNewPage(){ 
    return pages.getNewPage();
  }

  public void read(PDFScanner s)throws IOException{
    super.read(s);
  }

}




