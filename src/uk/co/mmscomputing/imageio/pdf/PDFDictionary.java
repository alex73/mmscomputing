package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFDictionary extends PDFObject{

  private Vector list = new Vector();

  public void put(String key,PDFObject obj){
    if(obj instanceof PDFIndirectObject){
      obj=new PDFIndirectReference((PDFIndirectObject)obj);
    }
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      PDFDictionaryEntry entry=(PDFDictionaryEntry)e.nextElement();
      if(key.equals(entry.getKey())){
        entry.obj=obj;return;
      }
    }
    list.add(new PDFDictionaryEntry(key,obj));
  }

  public PDFObject get(String key){
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      PDFDictionaryEntry entry=(PDFDictionaryEntry)e.nextElement();
      if(key.equals(entry.getKey())){
        return entry.getValue();
      }
    }
    return null;
  }

  public PDFObject getObject(String key){
    PDFObject obj = get(key);
    while(obj instanceof PDFIndirectReference){
      obj = ((PDFIndirectReference)obj).getIndirectObject().getDirectObject();
    }
    return obj;
  }

  public boolean checkNameEntry(String name, String value){
    PDFObject obj = get(name);
    if(obj==null){return false;}
    if(!(obj instanceof PDFObject.PDFName)){return false;}
    return ((PDFObject.PDFName)obj).getName().equals(value);
  }

  public void read(PDFScanner s)throws IOException{
    if(s.symbol==T_DICTIONARY_START){                         //  should have just scanned << T_DICTIONARY_START
      PDFObject key,obj;
      s.scan();
      while(s.symbol!=T_DICTIONARY_END){
        key = readObject(s);                                   // System.err.println("key = "+key);
        if(!(key instanceof PDFName)){
          throw new IOException(getClass().getName()+".read\n\tDictionary key must be of type NAME.\nSYMBOL = "+s.symbol+" "+key);
        }
        obj = readObject(s);
        if(obj==null){ throw new IOException(getClass().getName()+".read\n\tDictionary obj is missing.");}
        put(((PDFName)key).getName(),obj);                     // System.err.println("obj = "+obj);
      }
      if(s.symbol!=T_DICTIONARY_END){
        throw new IOException(getClass().getName()+".read\n\tDictionary: Missing start >> .");
      }
    }else{
      throw new IOException(getClass().getName()+".read\n\tDictionary: Missing start << .");
    }
  }

  public void write(PDFFile out)throws IOException{
    out.writeln("<<");
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      ((PDFDictionaryEntry)e.nextElement()).write(out);
    }
    out.writeln(">>");
  }

  public String toString(){
    String s="<<\n";
    Enumeration e = list.elements();
    while(e.hasMoreElements()){
      s+=e.nextElement();
    }
    s+=">>\n";
    return s;
  }

  static public class PDFDictionaryEntry{

    private PDFName   key;
    private PDFObject obj;

    public PDFDictionaryEntry(String k,PDFObject o){
      key=new PDFName(k);obj=o;
    }

    public String getKey(){ return key.getName();}
    public PDFObject getValue(){ return obj;}
/*
    public void read(PDFScanner s)throws IOException{
      key = readObject(s);
      if(!(key instanceof PDFName)){
        throw new IllegalArgumentException(getClass().getName()+".read\n\tDictionary key must be of type NAME.");
      }
      obj = readObject(s);
      if(obj==null){
        throw new IllegalArgumentException(getClass().getName()+".read\n\tDictionary obj is missing.");
      }
    }
*/
    public void write(PDFFile out)throws IOException{
      key.write(out);
      obj.write(out);
      if(!(obj instanceof PDFDictionary)){out.write("\n");}
    }

    public String toString(){
      return key.toString()+obj.toString()+((obj instanceof PDFDictionary)?"":"\n");
    }
  }
}

