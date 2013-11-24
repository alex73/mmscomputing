package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFDocument{

  static public class PDFOutlineTree extends PDFDictionary{

    public PDFOutlineTree(){
      put("Type",new PDFObject.PDFName("Outlines"));
      put("Count",new PDFObject.PDFInteger(0));
    }
  }

  static public class PDFPages extends PDFDictionary{

    private PDFBody              body;
    private PDFIndirectReference parent, ref;
    private PDFArray             kids;

    public PDFPages(PDFBody body, PDFIndirectReference parent){
      this.body   = body;
      this.parent = parent;
      this.ref    = body.getIndirectReference(this);
      this.kids   = new PDFArray();
 
      put("Type",new PDFObject.PDFName("Pages"));
      put("Kids",kids);
      put("Count",new PDFObject.PDFInteger(0));
      if(parent!=null){                                         // if not pages root add parent
        put("Parent",parent);
      }
    }

    public PDFIndirectReference getReference(){return ref;}

    public PDFPage getNewPage(){
      PDFPage page = new PDFPage(body,this);
      kids.add(page.getReference());
      return page;
    }

    public void addResource(String name,PDFIndirectObject object){
      PDFDictionary resources=(PDFDictionary)get("Resources");
      if(resources==null){
        resources=new PDFDictionary();
        put("Resources",resources);
      }
      resources.put(name,new PDFIndirectReference(object));
    }

    public void write(PDFFile out)throws IOException{
      put("Count",new PDFObject.PDFInteger(kids.size()));
      super.write(out);
    }
  }
}




