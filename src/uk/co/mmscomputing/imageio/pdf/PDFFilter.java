package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.io.ModModREADOutputStream;
import uk.co.mmscomputing.io.RLEBit1OutputStream;

public class PDFFilter{

  static public class PDFEncoder extends FilterOutputStream{

    protected int len=0;

    public PDFEncoder(OutputStream out){
      super(out);
    }

    public int getLength(){return len;}

    public void write(byte[] b,int off,int len)throws IOException{
      for(int i=off;i<off+len;i++){
        write(b[i]);
      }
    }

    public void write(int b)throws IOException{
      super.write(b);len++;
    }
  }

  static public class PDFLineEncoder extends PDFEncoder{

    public PDFLineEncoder(OutputStream out){
      super(out);
    }

    public void write(int b)throws IOException{
      if((len%255)==254){super.write('\n');}
      super.write(b);
    }
  }

  static public class PDFASCIIHexEncoder extends PDFLineEncoder{

    public PDFASCIIHexEncoder(OutputStream out){
      super(out);
    }

    public void write(int b)throws IOException{
      int hi = (b>>4) & 0x000F;
      int lo =  b     & 0x000F;

      hi = (hi<10)?'0'+hi:'A'+(hi-10);
      lo = (lo<10)?'0'+lo:'A'+(lo-10);
          
      super.write(hi);super.write(lo);
    }

    public void close()throws IOException{
      super.write('>');
      super.close();
    }
  }

  static public class PDFASCII85Encoder extends PDFLineEncoder{   // [1] p.31

    int  i=0; long val=0;

    public PDFASCII85Encoder(OutputStream out){
      super(out);
    }

    public void write(int b)throws IOException{                   // ASCII character between 33 ('!') and 117 (u)
      val <<= 8;
      val |= (b&0x00FF);

      i++;
      if(i==4){
        if(val==0){
          super.write('z');
        }else{
          int c85=85*85*85*85;
          for(int j=0;j<5;j++){
            super.write('!'+((int)(val/c85))); val%=c85; c85/=85;
          }
        }
        i=0;val=0;
      }
    }

    public void close()throws IOException{
      if(i>0){                                               // if length of data not multiple of 4
        for(int j=i;j<4;j++){val<<=8;}                       // append 4-i zero bytes

        int c85=85*85*85*85;                                 // encode in usual way, but without z case
        for(int j=0;j<i+1;j++){                              // write only first i+1 characters
          super.write('!'+((int)(val/c85))); val%=c85; c85/=85;
        }
      }
      super.write('~');super.write('>');
      super.close();
    }
  }

/*
  static public class PDFDCTEncoder extends PDFEncoder{
    public PDFDCTEncoder(OutputStream out)throws IOException{
      super(out);
    }
  }
*/


  static public class PDFCCITTFaxEncoder extends PDFEncoder{

    RLEBit1OutputStream       rlos;                        // byte = 8 black and white pixel
    ModModREADOutputStream    mmros;                       // T.6 MMR
    int                       offLine,bytesPerLine;

    public PDFCCITTFaxEncoder(OutputStream out,int width)throws IOException{
      super(out);

      bytesPerLine = (width+7)>>3;

      mmros  = new ModModREADOutputStream(out,width);
      rlos   = new RLEBit1OutputStream(mmros);

      out = rlos;


      rlos.setStartCodeWord(0x0001);                       // white run first
      mmros.writeEOL();                                    // T.6 does not really write EOL; only new line initialization
      offLine = 0;
    }

    public void write(int b)throws IOException{
      if(offLine==bytesPerLine){
        rlos.setStartCodeWord(0x0001);                     // white run first
        mmros.writeEOL();                                  // T.6 does not really write EOL; only new line initialization
        offLine=0;
      }
      super.write(b);
      offLine++;
    }

    public void close()throws IOException{
      rlos.flush();
      mmros.writeEOFB();
      mmros.flush();
      super.close();
    }
  }

  static private PDFEncoder getEncoder(
      OutputStream out,
      PDFDictionary dict,
      String coderName
  )throws IOException{
    if(coderName.equals("ASCIIHexDecode")){
      return new PDFASCIIHexEncoder(out);
    }else if(coderName.equals("ASCII85Decode")){
      return new PDFASCII85Encoder(out);
    }else if(coderName.equals("DCTDecode")){
      /*
         This will be encoded before PDFStream.setInputStream
         see. PDFXObject.PDFImage.write
      */
      return new PDFEncoder(out);
    }else if(coderName.equals("CCITTFaxDecode")){
      int width = ((PDFObject.PDFInteger)dict.get("Width")).getValue();
//System.err.println("width = "+width);
//      return new PDFCCITTFaxEncoder(out,width);

      /*
         This will be encoded before PDFStream.setInputStream
         see. PDFXObject.PDFImage.write
      */
      return new PDFEncoder(out);
    }
    throw new IllegalArgumentException();
  }

  static private PDFEncoder getEncoder(
      OutputStream out,
      PDFDictionary dict,
      PDFArray filter, 
      int i
  )throws IOException{


    PDFObject.PDFName coder     = (PDFObject.PDFName)filter.elementAt(i);
    String            coderName = coder.getName();

    PDFEncoder        encoder   = getEncoder(out,dict,coderName);

    try{
      return getEncoder(encoder,dict,(PDFArray)filter,i+1);
    }catch(ArrayIndexOutOfBoundsException e){
      return encoder;
    }
  }

  static PDFEncoder getEncoder(
      OutputStream  out,
      PDFDictionary dict,
      PDFObject     filter
  )throws IOException{
    if(filter instanceof PDFArray){
      return getEncoder(out,dict,(PDFArray)filter,0);
    }else if(filter instanceof PDFObject.PDFName){
      return getEncoder(out,dict,((PDFObject.PDFName)filter).getName());
    }
    throw new IllegalArgumentException();
  }


  public static void main(String[] argv){
    try{
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PDFASCII85Encoder encoder = new PDFASCII85Encoder(baos);

      // FF D8 FF E0 00 10 4A 46 49 46

      byte[] buf = {(byte)0xFF,(byte)0xD8,(byte)0xFF,(byte)0xE0,(byte)0x00,(byte)0x10,(byte)0xA4,(byte)0x46,(byte)0x49,(byte)0x46};

      encoder.write(buf);
      encoder.close();

      System.err.println(new String(baos.toByteArray()));
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

/*

[1] Portable Document Format Reference Manual
    ISBN 0-201-62628-4
    1996
*/