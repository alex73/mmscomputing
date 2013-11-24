package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;
import java.awt.image.*;

public class PDFPage extends PDFDictionary{

  private PDFBody              body;
  private PDFDocument.PDFPages parent;
  private PDFIndirectReference ref;
  private PDFArray             contents;

  public PDFPage(PDFBody body, PDFDocument.PDFPages parent){
    this.body     = body;
    this.parent   = parent;
    this.ref      = body.getIndirectReference(this);
    this.contents = null;
    put("Type",new PDFObject.PDFName("Page"));
    setMediaBox(0,0,613,793);
    put("Parent",parent.getReference());
  }

  public PDFIndirectReference getReference(){return ref;}
  public PDFDocument.PDFPages getParent(){return parent;}

  // All pages from <parent> will inherit: MediaBox, CropBox, Resources and Rotate !

  public void setMediaBox(int x,int y,int width,int height){
    PDFArray mediaBox;
    mediaBox = new PDFArray();
    mediaBox.add(new PDFObject.PDFInteger(x));            // lower-left (x,y)
    mediaBox.add(new PDFObject.PDFInteger(y));
    mediaBox.add(new PDFObject.PDFInteger(x+width/*-1*/));    // upper-right (x,y)
    mediaBox.add(new PDFObject.PDFInteger(y+height/*-1*/));
    put("MediaBox",mediaBox);
  }

  public void addResource(String name,PDFObject object){
    PDFDictionary resources=(PDFDictionary)get("Resources");
    if(resources==null){
      resources=new PDFDictionary();
      put("Resources",resources);
    }
    resources.put(name,object);
  }

  public void addResource(String name,PDFIndirectObject object){
    PDFDictionary resources=(PDFDictionary)get("Resources");
    if(resources==null){
      resources=new PDFDictionary();
      put("Resources",resources);
    }
    resources.put(name,new PDFIndirectReference(object));
  }

  public boolean haveResource(String name){
    PDFDictionary resources=(PDFDictionary)get("Resources");
    if(resources==null){ return false;}
    return resources.get(name)!=null;
  }

  // "If contents is absent, the page is empty" [1] p.53

  public void setContents(PDFIndirectObject content){
    if(contents==null){
      contents = new PDFArray();
      put("Contents",contents);   
    }
    contents.add(content);   
  }

  public void setCropBox(int x,int y,int width,int height){
    PDFArray cropBox;
    cropBox = new PDFArray();
    cropBox.add(new PDFObject.PDFInteger(x));             // lower-left (x,y)
    cropBox.add(new PDFObject.PDFInteger(y));
    cropBox.add(new PDFObject.PDFInteger(x+width/*-1*/));     // upper-right (x,y)
    cropBox.add(new PDFObject.PDFInteger(y+height/*-1*/));
    put("CropBox",cropBox);
  }

  public void addDefaultFont(){
    PDFIndirectReference defaultFont = body.getDefaultFont();
    PDFDictionary font = new PDFDictionary();
    font.put("defaultFont",defaultFont);    
    addResource("Font",font);
  }

  public void addPostScript(byte[] data){
    if(!haveResource("Font")){addDefaultFont();}

    PDFStream         stream = new PDFStream();
    PDFIndirectObject obj    = body.getIndirectObject(stream);
    stream.setInputStream(new ByteArrayInputStream(data));
    setContents(obj);
  }

  public void addText(int x,int y, int size, String text){
    String data="BT\n";
    data+="/defaultFont "+size+" Tf\n";
    data+=""+x+" "+y+" Td("+text+") Tj\n";
    data+="ET";
    addPostScript(data.getBytes());
  }

  public void addImage(String name, BufferedImage image)throws IOException{
    PDFIndirectObject obj;

    PDFDictionary resources=(PDFDictionary)get("Resources");
    if(resources==null){
      resources=new PDFDictionary();
      put("Resources",resources);
    }

    PDFArray procset = new PDFArray();
    procset.add(new PDFObject.PDFName("PDF"));
    procset.add(new PDFObject.PDFName("Text"));
    procset.add(new PDFObject.PDFName("ImageB"));
    procset.add(new PDFObject.PDFName("ImageC"));
    obj = body.getIndirectObject(procset);
    resources.put("ProcSet",new PDFIndirectReference(obj));

    PDFDictionary xobject=(PDFDictionary)resources.get("XObject");
    if(xobject==null){
      xobject=new PDFDictionary();
      resources.put("XObject",xobject);
    }

    int width  = image.getWidth();
    int height = image.getHeight();

    setMediaBox(0,0,width,height);

    PDFImage  pdfimage = new PDFImage(name, image);
              obj      = body.getIndirectObject(pdfimage);

    xobject.put(name,new PDFIndirectReference(obj));

    PDFStream stream   = new PDFStream();
              obj      = body.getIndirectObject(stream);

    /*
    Images are only 1 by 1 pixels in user space.
    CTM is transformation matrix from user space to device space.
    We need to scale the image to its original width and height
    */

    String data="q\n";                 // q : save current graphics state
                                       // cm: concat CTM with following matrix
    data +=Integer.toString(width)+" 0 0 "+Integer.toString(height)+" 0 0 cm\n";
    data +="/"+name+" Do\n";           // paint image with name: /name
//    data +=Double.toString(1.0/width)+" 0 0 "+Double.toString(1.0/height)+" 0 0 cm\n";
    data +="Q\n";                      // Q : restore the graphics state to the last saved state

    stream.setInputStream(new ByteArrayInputStream(data.getBytes()));

    setContents(obj);
  }  
}


/*

[1] Portable Document Format Reference Manual
    ISBN 0-201-62628-4
    1996
*/