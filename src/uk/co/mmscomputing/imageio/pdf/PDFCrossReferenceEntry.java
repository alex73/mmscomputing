package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFCrossReferenceEntry implements PDFConstants{

    // <in-use entry> ::= <byte offset> <generation number> n
    // <free entry>   ::= <object number of next free object> <generation number> f

  private boolean isInUse;
  private int     offset;
  private int     objectNumber;
  private int     generationNumber;
  private PDFIndirectObject object;

/*
  public PDFCrossReferenceEntry(){
    object = null;
  }
*/
  public PDFCrossReferenceEntry(int objno){
    isInUse=true;
    offset=0;
    objectNumber=objno;
    generationNumber=0;
    object = null;
  }

  public PDFCrossReferenceEntry(int nextobjno,int nextgenno){  // not in use
    isInUse=false;
    offset=0;
    objectNumber=nextobjno;
    generationNumber=nextgenno;
    object = null;
  }

  public void setOffset(int v){offset=v;}
  public int  getOffset(){return offset;}
  public int  getObjectNumber(){ return objectNumber;}
  public int  getGenerationNumber(){ return generationNumber;}

  public void setObject(PDFIndirectObject v){object=v;}
  public PDFIndirectObject getObject(){return object;}
  public PDFObject getDirectObject(){                            // System.err.println("PDFCrossReferenceEntry = "+toString());
    return object.getDirectObject();
  }

  public void read(PDFScanner s)throws IOException{
    s.scan();
    if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tSubsection Entry: Byte offset.");}
    offset = s.intval;
    s.scan();
    if(s.symbol!=T_INTEGER){throw new IOException(getClass().getName()+".read:\n\tSubsection Entry: Generation number.");}
    generationNumber = s.intval;
    s.scan();
    if(s.symbol==T_N){                                           // in-use entry
      isInUse = true;
    }else if(s.symbol==T_F){                                     // free entry
      isInUse = false;
    }else{
      throw new IOException(getClass().getName()+".read:\n\tSubsection Entry: Missing <n>|<f> keyword.");
    }
  }

  private void write(PDFFile out,int size,int n)throws IOException{
    byte[] number = new byte[size];
    int    i      = size-1;

    while(n>0){ 
      if(i<0){ 
        throw new IllegalArgumentException(getClass().getName()+".write:\n\tNumber too big!");
      }
      number[i]=(byte)('0'+(n%10));
      n/=10;
      i--;
    }
    while(i>=0){ 
      number[i]='0';
      i--;
    }
    out.write(number);
    out.write(' ');
  }

  public void write(PDFFile out)throws IOException{
    if(isInUse){
      write(out,10,offset);
      write(out,5,generationNumber);
      out.write('n');
    }else{
      write(out,10,objectNumber);
      write(out,5,generationNumber);
      out.write('f');
    }
    out.write(' ');                         // end of line sequence ' ''\n' | ' ''\r' | '\r''\n'
    out.write('\n');
  }

  public String toString(){
    String s = "";
    if(isInUse){
      s += offset+" ";
      s += generationNumber+" n";
    }else{
      s += objectNumber+" ";
      s += generationNumber+" f";
    }
    return s;
  }
}

