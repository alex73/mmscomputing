package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.imageio.sff.*;
import uk.co.mmscomputing.imageio.tiff.*;

public class FaxCallSffSaver extends FaxCallSaver{

  public FaxCallSffSaver(){}

  public void run(String local,String remote,InputStream pin,OutputStream pout){

    // called by a FaxAnswerer in own thread
    // expect 'in' to be a sff data stream

    FileOutputStream fout=null;

    try{
      filename = createFilePath(local,remote,"sff");

      File    file     = new File(filename);
              fout     = new FileOutputStream(file);
      String  fn       = filename.toLowerCase();
      int     maxilc;
      try{
        maxilc = Integer.parseInt(properties.getProperty(faxMaxIllegalLineCodingsID,"-1"));
      }catch(Exception e){
        System.out.println("3\b"+getClass().getName()+".run\n\tCould not set 'illegal line coding' limit.\n\t"+e);
        maxilc = -1;
      }
      if(fn.endsWith(".tif")||fn.endsWith(".tiff")){          // want to save as TIFF
        writeTiff(pin,fout,maxilc);                           // drop line after 'maxilc' illegal line codings
      }else if(maxilc>=0){                                    // write as .sff file
        writeSff(pin,fout,maxilc);                            // drop line after 'maxilc' illegal line codings
      }else{                                                  // just write .sff file
        write(pin,fout);
      }
    }catch(SFFInputStream.IllegalLineCodingException ilce){
      System.out.println("5\b"+getClass().getName()+".run\n\tDisconnect call.\n\t"+ilce);
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
      e.printStackTrace();
    }
    try{pin.close();pout.close();if(fout!=null){fout.close();}
    }catch(Exception ioe){System.out.println(getClass().getName()+".run:\n\t"+ioe);}
  }

  protected void writeSff(InputStream cin,OutputStream fout,int maxilc)throws IOException{
    SFFInputStream  in  = new SFFInputStream(cin);    // decode sff stream first to get illegal line coding errors
    SFFOutputStream out = new SFFOutputStream(fout);  // encode to sff stream again

    in.setMaxAllowedIllegalLineCodings(maxilc);       // after receiving 'maxilc' illegal line coding errors throw exception
    while(in.hasImage()){                             //
      out.writePageHeader(in.getWidth());
      byte[] buf=new byte[256];int len;
      while(/*!in.isEndOfPage()&&*/(len=in.read(buf))!=-1){           // read huffman codes
        out.write(buf,0,len);                                         // write huffman codes
      }
      out.writePageEnd();
    }
    out.writeDocumentEnd();
  }

  public void writeTiff(InputStream cin,OutputStream fout,int maxilc)throws IOException{
    SFFInputStream         in  = new SFFInputStream(cin);             // decode sff stream
    TIFFClassFOutputStream out = new TIFFClassFMHOutputStream(fout);  // encode tiff stream

    in.setMaxAllowedIllegalLineCodings(maxilc);
    while(in.hasImage()){
      out.writePageHeader(in.getWidth());
      byte[] buf=new byte[256];int len;
      while(/*!in.isEndOfPage()&&*/(len=in.read(buf))!=-1){           // read huffman codes
        out.write(buf,0,len);                                         // write huffman codes
      }
      out.writePageEnd();
    }
    out.writeDocumentEnd();
  }
}

