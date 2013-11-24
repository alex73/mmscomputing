package uk.co.mmscomputing.imageio.pdf;

import java.io.*;

// no line in a pdf file may be longer than 255 chars
// comment: from % until end of line; exception PDFString

abstract public class PDFObject implements PDFConstants{

  public void write(PDFFile out)throws IOException{
    out.write(toString());
  }

//  abstract public void read(PDFScanner in)throws IOException;

  public void read(PDFScanner in)throws IOException{
    System.err.println("\n\nNOT IMPLEMENTED YET\n\n");
    new Exception().printStackTrace();
  }

  static public PDFObject readObject(PDFScanner s)throws IOException{
    PDFObject obj = null;
    switch(s.symbol){
    case T_STRING:       obj = new PDFString(s.str);      s.scan(); break;
    case T_NAME:         obj = new PDFName(s.str);        s.scan(); break;
    case T_TRUE:         obj = new PDFBoolean(true);      s.scan(); break;
    case T_FALSE:        obj = new PDFBoolean(false);     s.scan(); break;
    case T_NULL:         obj = new PDFNull();             s.scan(); break;
    case T_INTEGER:      obj = new PDFInteger(s.intval);  s.scan(); break;
    case T_REAL:         obj = new PDFReal(s.realval);    s.scan(); break;
    case T_R:            obj = new PDFIndirectReference(s.getIndirectObject(s.objectNumber,s.generationNumber)); s.scan(); break;
    case T_ARRAY_START:{
      obj = new PDFArray();
      obj.read(s);
      s.scan();
      return obj;
      }
    case T_ARRAY_END:        return null;
    case T_DICTIONARY_START:{
      PDFDictionary dict = new PDFDictionary();
      dict.read(s);
      s.scan();
      if(s.symbol==T_STREAM){                    
        if(dict.checkNameEntry("Type","XObject")){ 
          return PDFXObject.readXObject(s,dict);
        }
        obj=new PDFStream(dict);
        obj.read(s);
        return obj;
      }
      return dict;
    }
    case T_DICTIONARY_END:   return null;
    default:
      System.err.println("symbol    = "+s.symbol);
      break;
    }
    return obj;
  }      

  static public class PDFNull extends PDFObject{
    public String toString(){ return "null";}
  }

  static public class PDFBoolean extends PDFObject{

    private boolean value=true;

    public PDFBoolean(boolean v){
      value=v;
    }

    public void setValue(boolean v){value=v;}
    public boolean getValue(){ return value;}

    public String toString(){ return (value)?"true ":"false ";}
  }

  static public class PDFInteger extends PDFObject{

    private int value=0;

    public PDFInteger(int v){
      value=v;
    }

    public void setValue(int v){value=v;}
    public int getValue(){ return value;}

    public String toString(){ return ""+value+" ";}
  }

  static public class PDFReal extends PDFObject{

    private double value=0;

    public PDFReal(double v){
      value=v;
    }

    public void setValue(double v){value=v;}
    public double getValue(){ return value;}

    public String toString(){ return ""+value+" ";}
  }

  static public class PDFString extends PDFObject{

    private String value="";

    public PDFString(String v){
      value=v;
    }

    public void setValue(String v){value=v;}
    public String getValue(){ return value;}

    public String toString(){
      String s="(";
      byte[] chars=value.getBytes();
      for(int i=0;i<chars.length;i++){             // [2] Tab 4.1
        char c=(char)chars[i];
        switch(c){
        case '\n':   s+="\\n";break;
        case '\r':   s+="\\r";break;
        case '\t':   s+="\\t";break;
        case '\b':   s+="\\b";break;
        case '\f':   s+="\\f";break;
        case '(' :   s+="\\(";break;
        case ')' :   s+="\\)";break;
        case '\\':   s+="\\\\";break;
        default:     s+=c;break;
        }
// to do: \ddd for non ascii characters
      }
      s+=") ";
      return s;
    }
  }

  static public class PDFHexString extends PDFObject{

    private String value="";

    public PDFHexString(String v){
      value=v;
    }

    public void setValue(String v){
      if((v.length()%2)==0){
        value=v;
      }else{
        value=v+"0";
      }
    }
    public String getValue(){ return value;}

    public String toString(){
      String s="<";
      byte[] chars=value.getBytes();
      for(int i=0;i<chars.length;i++){
        byte c=chars[i];
        if(('0'<=c)&&(c<='9')){
          s+=c;
        }else if(('A'<=c)&&(c<='F')){
          s+=c;
        }else if(('a'<=c)&&(c<='f')){
          s+=c;
        }
      }
      s+="> ";
      return s;
    }
  }

  static public class PDFName extends PDFObject{

    private String name="";

    public PDFName(String name){
      set(name);
    }

    public void set(String n){
      name="";
      byte[] chars=n.getBytes();
      for(int i=0;i<chars.length;i++){
        char c=(char)chars[i];
        switch(c){
        case '\n':
        case '\r':
        case '%':
        case '(':case ')':
        case '<':case '>':
        case '[':case ']':
        case '{':case '}':
                  System.err.println(getClass().getName()+".set:\n\tInvalid character "+n);
                  throw new IllegalArgumentException(getClass().getName()+".set\n\tInvalid character "+c);
        default:  name+=c;break;
        }
      }
    }

    public String getName(){ return name;}
    public String toString(){ return "/"+name+" ";}

    public int hashCode(){ return name.hashCode();}
    public boolean equals(Object obj){ return obj.equals(name);}
  }

// DecodeParams
// ASCIIHexDecode, ASCII85Decode, LZWDecode, RunLengthDecode, CCITTFaxDecode, DCTDecode
}

// [2] PostScript Language Reference Manual, Second Edition