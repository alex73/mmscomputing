package uk.co.mmscomputing.imageio.pdf;

import java.io.*;

public class PDFXObject extends PDFStream{

  public PDFXObject(String subtype){
    super();
    put("Type",new PDFObject.PDFName("XObject"));
    put("Subtype",new PDFObject.PDFName(subtype));
  }

  public PDFXObject(PDFDictionary d){
    super(d);
  }

  static public PDFXObject readXObject(PDFScanner s,PDFDictionary dict)throws IOException{
    PDFXObject obj = null;

    if(dict.checkNameEntry("Subtype","Image")){
      obj=new PDFImage(dict);
    }else{
      obj=new PDFXObject(dict);
    }
    obj.read(s);
    return obj;
  }
}

/*

[1] Portable Document Format Reference Manual
    ISBN 0-201-62628-4
    1996
*/