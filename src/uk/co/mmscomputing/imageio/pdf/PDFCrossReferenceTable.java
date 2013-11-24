package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFCrossReferenceTable implements PDFConstants{                                  // [1] 43

    // one line entry for each indirect object; pointer to location in file
    // list of cross-reference sections
    // one section for new file; one section per update

    // the free entries form a linked list
    // first entry always obj 0 and gen no 65535 -> head of linked list of free objects
    // tail of list uses 0 as object number  [1] p.44

  private PDFCrossReferenceEntry[] entries=null;

  public PDFCrossReferenceTable(int size){              // read
    entries=new PDFCrossReferenceEntry[size];
  }

  public PDFCrossReferenceEntry[] getEntries(){ return entries;}

  private Vector sections = new Vector();

  public PDFCrossReferenceTable(){                      // write
  }

  public void add(PDFCrossReferenceEntry obj){
    add(0,0,obj);
  }

  public void add(int section, int subsection, PDFCrossReferenceEntry obj){
    while(sections.size()<=section){
      sections.add(new PDFCrossReferenceSection());
    }
    ((PDFCrossReferenceSection)sections.elementAt(section)).add(subsection,obj);
  }

  public int getLastCrossReferenceSectionOffset(){
    return ((PDFCrossReferenceSection)sections.lastElement()).getOffset();
  }

  public int size(){
    int count=0;
    Enumeration e = sections.elements();
    while(e.hasMoreElements()){
      PDFCrossReferenceSection section=(PDFCrossReferenceSection)e.nextElement();
      count+=section.size();
    }
    return count;
  }

  public void write(PDFFile out)throws IOException{
    Enumeration e = sections.elements();
    while(e.hasMoreElements()){
      PDFCrossReferenceSection section=(PDFCrossReferenceSection)e.nextElement();
      section.write(out);
    }
  }

  public PDFCrossReferenceEntry getEntry(int objectNumber){
    return entries[objectNumber];
  }

  public void read(PDFScanner s)throws IOException{
    int index=0;
    if(s.symbol==T_XREF){                                                    // at least one cross-reference section
      do{                                                                    // todo: updated files; how does it work ????
        PDFCrossReferenceSection section=new PDFCrossReferenceSection();
        sections.add(section);
        index=section.read(s,entries,index);
      }while(s.symbol==T_XREF);                                              // if cross-reference update sections then read on
    }else{
      throw new IOException(getClass().getName()+".read:\n\tExpect symbol <xref>");
    }
  }

  static public class PDFCrossReferenceSection{

    // one line entry for each indirect object; pointer to location in file
    // <cross-reference section> ::= 
    //                               xref
    //                               <cross-reference subsection>+

    private Vector subsections = new Vector();
    private int    offset      = -1;

    public int getOffset(){ return offset;}

    public void add(int subsection, PDFCrossReferenceEntry obj){
      while(subsections.size()<=subsection){
        subsections.add(new PDFCrossReferenceSubSection());
      }
      ((PDFCrossReferenceSubSection)subsections.elementAt(subsection)).add(obj);
    }

    public int size(){
      int count=0;
      Enumeration e = subsections.elements();
      while(e.hasMoreElements()){
        PDFCrossReferenceSubSection subsection=(PDFCrossReferenceSubSection)e.nextElement();
        count+=subsection.size();
      }
      return count;
    }

    public int read(PDFScanner s,PDFCrossReferenceEntry[] entries,int index)throws IOException{
      do{
        PDFCrossReferenceSubSection subsection=new PDFCrossReferenceSubSection();
        subsections.add(subsection);
        index=subsection.read(s,entries,index);
        s.scan();
      }while(s.symbol==T_INTEGER);                                // if we find an integer instead of xref | trailer 
      return index;
    }                                                             // read another cross-reference subsection

    public void write(PDFFile out)throws IOException{
      offset = out.getOffset();
      out.writeln("xref");
      Enumeration e = subsections.elements();
      while(e.hasMoreElements()){
        PDFCrossReferenceSubSection subsection=(PDFCrossReferenceSubSection)e.nextElement();
        subsection.write(out);
      }
    }
  }

  static public class PDFCrossReferenceSubSection{

    // continuous range of object numbers
    // <cross-reference subsection> ::= 
    //                               <object number of first entry> <number of entries>
    //                               <cross-reference entry>+

    private int    first   = -1;
    private Vector entries = new Vector();

    public void add(PDFCrossReferenceEntry obj){
      entries.add(obj);
    }

    public int size(){
      return entries.size();
    }

    public int read(PDFScanner s,PDFCrossReferenceEntry[] entries,int index)throws IOException{
      s.scan();
      if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tSubsection: Missing object number of first entry.");}
      first = s.intval;                                                  // System.err.println("first "+first);
      s.scan();
      if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tSubsection: Missing number of entries.");}
      int len = s.intval;                                                // System.err.println("len "+len);
      for(int i=0;i<len;i++){
        PDFCrossReferenceEntry entry=new PDFCrossReferenceEntry(index);
        entry.read(s);
        entries[index++]=entry;                                          // System.err.println("entry "+entry.toString());
      }
      return index;
    }

    public void write(PDFFile out)throws IOException{
      Enumeration e     = entries.elements();
      if(e.hasMoreElements()){
        PDFCrossReferenceEntry   entry = (PDFCrossReferenceEntry)e.nextElement();
        out.write(entry.getObjectNumber());
        out.write(' ');
        out.write(entries.size());
        out.writeln();
        entry.write(out);
        while(e.hasMoreElements()){
          entry=(PDFCrossReferenceEntry)e.nextElement();
          entry.write(out);
        }
      }
    }
  }
}
