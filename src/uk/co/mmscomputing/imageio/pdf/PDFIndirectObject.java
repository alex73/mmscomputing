package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class PDFIndirectObject extends PDFObject{

  private int   objectNumber;
  private int   generationNumber;
  private int   offset;

  private PDFObject directObject;

  public PDFIndirectObject(int on,int gn,boolean b){  // read
    objectNumber     = on;
    generationNumber = gn;
    directObject     = null;                // null => don't know yet or not in use
    offset           = -1;
  }

  public PDFIndirectObject(int on,PDFObject obj){  // write
    objectNumber     = on;                  // resource entry index in catalog
    generationNumber = 0;
    directObject     = obj;
    offset           = -1;
  }

  public void setOffset(int v){offset=v;}

  public void setObjectNumber(int v){objectNumber=v;}
  public int  getObjectNumber(){ return objectNumber;}

  public void setGenerationNumber(int v){generationNumber=v;}
  public int  getGenerationNumber(){ return generationNumber;}

  public void setDirectObject(PDFObject v){directObject=v;}
  public PDFObject  getDirectObject(){ return directObject;}

  public void read(PDFScanner s)throws IOException{
    if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tCannot find 'object number'.");}
    setObjectNumber(s.intval);       // System.err.println("obj no "+s.intval);
    s.scan();
    
    if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tCannot find 'generation number'.");}
    setGenerationNumber(s.intval);   // System.err.println("obj gen "+s.intval);
    s.scan();
    if(s.symbol!=T_OBJ){throw new IOException(getClass().getName()+".read:\n\tCannot find keyword <obj>.");}
    s.scan();

    directObject = PDFObject.readObject(s);

    if(s.symbol!=T_ENDOBJ){throw new IOException(getClass().getName()+".read:\n\tCannot find keyword <endobj>.");}
    s.scan();
  }

  public void write(PDFFile out)throws IOException{
    if(directObject!=null){
      offset=out.getOffset();
      out.write(objectNumber);out.write(' ');
      out.write(generationNumber);out.write(' ');
      out.writeln("obj");
      directObject.write(out);
      out.writeln("endobj");
    }
  }

  public String toString(){
    String s="\nNo Direct Object\n";
    if(directObject!=null){
      s =getObjectNumber()+" "+getGenerationNumber()+" obj ";
      s+=directObject.toString();
      s+="endobj ";
    }
    return s;
  }

  public PDFCrossReferenceEntry getXRefEntry(){
    if(directObject!=null){
      PDFCrossReferenceEntry entry = new PDFCrossReferenceEntry(objectNumber);
      entry.setOffset(offset);
      return entry;
    }else{
      return new PDFCrossReferenceEntry(objectNumber,generationNumber);
    }
  }
}

