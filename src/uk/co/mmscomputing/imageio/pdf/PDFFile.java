package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;
import java.util.*;
import java.awt.image.BufferedImage;

import javax.imageio.stream.ImageInputStream;

// all ASCII
// binary data must be encoded in ASCII hex or ASCII base-85
// no line in a pdf file may be longer than 255 chars
// line delimiter \n (10), \r (13), \r\n (10,13)

public class PDFFile implements PDFConstants{

  // <PDF file> ::= <header>
  //                <body>
  //                <cross-reference table>
  //                <trailer>

  private PDFHeader              header;
  private PDFInfo                info;
  private PDFBody                body;
  private PDFCrossReferenceTable crossReferenceTable;
  private PDFTrailer             trailer;

  private OutputStream           out;
  private ImageInputStream       in;

  public PDFFile(){
    header              = new PDFHeader();
    crossReferenceTable = new PDFCrossReferenceTable();
    body                = new PDFBody(crossReferenceTable);
    info                = new PDFInfo(body);
    trailer             = new PDFTrailer();
  }

  public PDFBody getBody(){return body;}
  public PDFCatalog getCatalog(){return body.getCatalog();}

  public void write(OutputStream out)throws IOException{
    this.out=out;
    header.write(this);
    body.write(this);
    crossReferenceTable.write(this);
    trailer.put("Size",body.getSize());
    trailer.put("Root",body.getRoot());
    trailer.setInfo(info);
    trailer.write(this,crossReferenceTable.getLastCrossReferenceSectionOffset());
  }

  public int getOffset(){
    return ((ByteArrayOutputStream)out).size();
  }

  public void write(byte b)throws IOException{
    out.write(b);
  }

  public void write(char b)throws IOException{
    out.write((byte)b);
  }

  public void write(byte[] bytes)throws IOException{
    out.write(bytes);
  }

  public void write(String s)throws IOException{
    write(s.getBytes());
  }

  public void writeln(String s)throws IOException{
    write(s.getBytes());write('\n');
  }

  public void write(int i)throws IOException{
    write(Integer.toString(i));
  }

  public void writeln()throws IOException{
    write('\n');
  }

  public void read(ImageInputStream in)throws IOException{
    PDFScanner s=new PDFScanner(body,in);
    header.read(s);
    trailer.read(s);                                        // System.err.println("startxref: "+trailer.getStartXRef());

//    PDFIndirectObject root = trailer.getRoot();
//    int objectNumber       = root.getObjectNumber();
//    int generationNumber   = root.getGenerationNumber();

    s.seek(trailer.getStartXRef());
    crossReferenceTable = new PDFCrossReferenceTable(trailer.getSize());
    crossReferenceTable.read(s);                            // get list of indirect objects

    PDFCrossReferenceEntry[] entries = crossReferenceTable.getEntries();

    for(int i=1;i<entries.length;i++){
      PDFCrossReferenceEntry entry = crossReferenceTable.getEntry(i);
      PDFIndirectObject indobj = body.getIndirectObject(entry.getObjectNumber(),entry.getGenerationNumber());
      indobj.setOffset(entry.getOffset());
    }

    boolean again;int tries=0;
    do{
      again=false;
      for(int i=1;i<entries.length;i++){
        PDFCrossReferenceEntry entry = crossReferenceTable.getEntry(i);
        if(entry.getObject()==null){
          PDFIndirectObject indobj = body.getIndirectObject(entry.getObjectNumber(),entry.getGenerationNumber());
          s.seek(entry.getOffset());
          try{
            indobj.read(s);                                   // System.err.println("\n\n"+indobj.toString()+"\n");        
            entry.setObject(indobj);
          }catch(Exception e){
            again=true;                                       // Probably couldn't resolve 'Length' for a stream.
//            System.err.println("\n\nREADING RESOURCE["+i+"] => EXCEPTION: "+e+"\n");
//            e.printStackTrace();
          }
        }
      }
      tries++;                                                // System.err.println("try = "+tries);
    }while(again&&(tries<10));
  }

  public PDFPage getNewPage(){return body.getCatalog().getNewPage();}

  public BufferedImage getImage(int index)throws IOException{
    int size = trailer.getSize();
    int ind  = 0;
    for(int i=1;i<size;i++){
      PDFCrossReferenceEntry entry = crossReferenceTable.getEntry(i);
      PDFObject object = entry.getDirectObject();
      if(object instanceof PDFImage){
        if(ind==index){
          return ((PDFImage)object).getImage();
        }
        ind++;
      }
    }
    return null;
  }

  static public class PDFHeader{

    // <header> ::= <PDF version>

    private String header = "PDF-1.1";

    public void write(PDFFile out)throws IOException{
      out.writeln("%"+header);
      out.write("%");out.write((byte)129);out.write((byte)129);out.write((byte)129);out.write((byte)129);out.writeln();
    }

    public void read(PDFScanner s)throws IOException{
      header=s.scanComment();                         // System.err.println("header = "+header);
      
    }
  }

  static public class PDFTrailer extends PDFDictionary{

    // <trailer> ::= trailer
    //               <<
    //               <trailer key-value pair>+
    //               >>
    //               startxref
    //               <cross-reference table start address>
    //               %%EOF

    private int           xref = -1;

    public void setStartXRef(int offset){xref=offset;}
    public int  getStartXRef(){ return xref;}

    public int getSize(){ return ((PDFObject.PDFInteger)get("Size")).getValue();}
    public PDFIndirectObject getRoot(){return ((PDFIndirectObject)((PDFIndirectReference)get("Root")).getIndirectObject());}

    public void setInfo(PDFInfo info){
      if(info!=null){put("Info",info.getReference());}
    }

    public void read(PDFScanner s)throws IOException{
      s.seek(s.getLength()-256);                         // find token 'trailer' at end of file
      s.find(T_TRAILER);
      s.scan();
      super.read(s);
      s.scan();
      if(s.symbol!=T_STARTXREF){throw new IOException(getClass().getName()+".read:\n\tTrailer: Cannot find keyword <startxref>.");}
//    A normal s.scan() would scan %%EOF as well [reach EOF] and then seek does not work anymore ??? Why ???
//      s.scan();
      s.scanStartXRefNumber();
      if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tTrailer: Cannot find startxref value.");}
      xref = s.intval;
    }

    public void write(PDFFile out,int xref)throws IOException{
      out.writeln("trailer");
      super.write(out);
      out.writeln("startxref");
      out.write(xref);
      out.writeln();
      out.write("%%EOF");
    }
  }

  static public class PDFIncrementalUpdate{
  // todo
  }
}
