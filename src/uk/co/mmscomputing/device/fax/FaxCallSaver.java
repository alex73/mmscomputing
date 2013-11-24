package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.*;
import java.text.*;

public class FaxCallSaver extends Thread implements FaxConstants,FaxCallHandler{

  protected Properties    properties;
  protected String        filename=null;

  public FaxCallSaver(){}

  public String getFile(){return filename;}

  public void init(Properties properties){this.properties=properties;}

  public void run(String local,String remote,InputStream pin,OutputStream pout){

    // called by a FaxAnswerer in own thread

    FileOutputStream fout=null;

    try{
      filename = createFilePath(local,remote,".sff");

      File    file     = new File(filename);
              fout     = new FileOutputStream(file);

      write(pin,fout);

    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
      e.printStackTrace();
    }finally{
      try{pin.close();pout.close();if(fout!=null){fout.close();}
      }catch(Exception ioe){System.out.println(getClass().getName()+".run:\n\t"+ioe);}
    }
  }

  static private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS-");
  static private int msgno=0;

  protected String createFilePath(String localno,String remoteno,String ext){
    File file,parent=new File(properties.getProperty(faxReceiverFileDirID));
    String rno=remoteno.replaceAll("\\*\\*","");    // PBX: internal dialling

    try{
      ext = properties.getProperty(faxFileTypeID,ext);
    }catch(Exception e){
      System.out.println("3\b"+getClass().getName()+".run\n\tCould not set file type extension.\n\t"+e);
    }
    String destfile=sdf.format(new Date())+(msgno++)+"_"+localno+"_"+rno+"."+ext;
    try{
       parent.mkdirs();
       file=new File(parent.getAbsolutePath(),destfile);
    }catch(Exception e){ 
       System.out.println("9\b"+getClass().getName()+".createFilePath:\n\tCould not create directory:\n\t"
           +parent.getAbsolutePath());
       file=new File(destfile);
    }
    return file.getAbsolutePath();
  }

  protected void write(InputStream pin,OutputStream fout)throws IOException{
    int    count;byte[] buffer = new byte[256];
    while((count=pin.read(buffer))!=-1){
      fout.write(buffer,0,count);
    }
  }
}

