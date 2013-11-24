package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

public class PDFStream extends PDFObject{

  protected PDFDictionary dict = null;
  protected InputStream   data = null;

  public PDFStream(){
    this(new PDFDictionary());
  }

  public PDFStream(PDFDictionary d){
    dict = d;
  }

  public void        setInputStream(byte[] v){ data=new ByteArrayInputStream(v);}
  public void        setInputStream(InputStream v){ data=v;}
  public InputStream getInputStream(){              return data;}

  public void put(String key,PDFObject obj){ dict.put(key,obj);}
  public PDFObject get(String key){          return dict.get(key);}

  public void setFilter(String filter){
    put("Filter",new PDFName(filter));
  }

  public void setFilters(String[] filters){    // i.e. /Filter [/ASCII85Decode /DCTDecode]
    PDFArray filter=new PDFArray();
    for(int i=0;i<filters.length;i++){
      filter.add(new PDFName(filters[i]));
    }
    put("Filter",filter);
  }

  public String[] getFilters(){                // i.e. /Filter [/ASCII85Decode /DCTDecode]
    PDFObject filter = get("Filter");
    if(filter instanceof PDFObject.PDFName){
      String[] filters = new String[1];
      filters[0] = ((PDFObject.PDFName)filter).getName();
      return filters;
    }else if(filter instanceof PDFArray){
      PDFArray fa = (PDFArray)filter;
      String[] filters = new String[fa.size()];
      for(int i=0;i<filters.length;i++){
        filters[i]=((PDFObject.PDFName)fa.elementAt(i)).getName();
      }
      return filters;
    } 
    throw new IllegalArgumentException(getClass().getName()+".getFilters:\n\t'Filter' has unknown type");
  }

  public void read(PDFScanner s)throws IOException{
    PDFObject lenobj = dict.getObject("Length");
    int length = ((PDFInteger)lenobj).getValue();
    setInputStream(s.scanStream(length));
    s.scan();
    if(s.symbol!=T_ENDSTREAM){
      throw new IOException(getClass().getName()+".read:\n\tCannot find keyword <endstream>.\nHave symbol = "+s.symbol+" and string = "+s.str);
    }
    s.scan();
  }

  private byte[] codeNone(InputStream data)throws IOException{
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int b;
    while((b=data.read())!=-1){
      out.write(b);
    }
    return out.toByteArray();
  }

  public void write(PDFFile out)throws IOException{
    if(data==null){return;}

    byte[]                buffer;
    PDFFilter.PDFEncoder  encoder;
    ByteArrayOutputStream baos    = new ByteArrayOutputStream();

    PDFObject filter = get("Filter");
    if(filter == null){
      buffer = codeNone(data);
    }else if(filter instanceof PDFName){
      encoder = PDFFilter.getEncoder(baos,dict,(PDFName)filter);

      buffer = new byte[4*1024];

      int len;
      while((len=data.read(buffer))!=-1){
        encoder.write(buffer,0,len);
      }
      encoder.close();
      buffer = baos.toByteArray();
    }else if(filter instanceof PDFArray){
      encoder = PDFFilter.getEncoder(baos,dict,(PDFArray)filter);

      buffer = new byte[4*1024];

      int len;
      while((len=data.read(buffer))!=-1){
        encoder.write(buffer,0,len);
      }
      encoder.close();
      buffer = baos.toByteArray();
    }else{
      throw new IOException(getClass().getName()+".write:\n\tUnknown filter object.");
    }
    put("Length",new PDFInteger(buffer.length));
    dict.write(out);
    out.write("stream\r\n");
    out.write(buffer);
    out.writeln("\rendstream");
  }

  public String toString(){
    String s=dict.toString();
    s+="stream\r\n...\rendstream";
    return s;
  }
}

