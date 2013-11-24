package uk.co.mmscomputing.imageio.pdf;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class PDFInfo extends PDFDictionary{

  static private String defaultAuthor   = "mm's computing"; 
  static private String defaultCreator  = "http://www.mms-computing.co.uk/"; 
  static private String defaultProducer = "http://www.mms-computing.co.uk/"; 
//  static private SimpleDateFormat sdf   = new SimpleDateFormat("dd MMM yyyy HH:mm:ss SSS");
  static private SimpleDateFormat sdf   = new SimpleDateFormat("yyyyMMddHHmmssSSS");

  private PDFIndirectReference ref;

  public PDFInfo(PDFBody body){
    this.ref    = body.getIndirectReference(this);

    setAuthor(defaultAuthor);
    setCreator(defaultCreator);
    setProducer(defaultProducer);
    setCreationDate(new Date());
    setTitle("An mm's computing document!");
  }

  public PDFIndirectReference getReference(){return ref;}

  static public void setDefaultAuthor(String author){ defaultAuthor = author;}
  public void setAuthor(String author){ put("Author",new PDFObject.PDFString(author));}
  public String getAuthor(){ return ((PDFObject.PDFString)get("Author")).getValue();}

  static public void setDefaultDateFormat(String df){ sdf = new SimpleDateFormat(df);}
  public void setCreationDate(Date date){ setCreationDate(sdf.format(date));}
  public void setCreationDate(String date){ put("CreationDate",new PDFObject.PDFString(date));}
  public String getCreationDate(){ return ((PDFObject.PDFString)get("CreationDate")).getValue();}

  public void setModDate(Date date){ setModDate(sdf.format(date));}
  public void setModDate(String date){ put("ModDate",new PDFObject.PDFString(date));}
  public String getModDate(){ return ((PDFObject.PDFString)get("ModDate")).getValue();}

  static public void setDefaultCreator(String creator){ defaultCreator = creator;}
  public void setCreator(String creator){ put("Creator",new PDFObject.PDFString(creator));}
  public String getCreator(){ return ((PDFObject.PDFString)get("Creator")).getValue();}

  static public void setDefaultProducer(String producer){ defaultProducer = producer;}
  public void setProducer(String producer){ put("Producer",new PDFObject.PDFString(producer));}
  public String getProducer(){ return ((PDFObject.PDFString)get("Producer")).getValue();}

  public void setTitle(String title){ put("Title",new PDFObject.PDFString(title));}
  public String getTitle(){ return ((PDFObject.PDFString)get("Title")).getValue();}

  public void write(PDFFile out)throws IOException{
    super.write(out);
  }
}




