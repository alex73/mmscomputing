package uk.co.mmscomputing.application.install;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Unzip{

  private Log        log;
  private ZipFile    file=null;
  private byte[]     buffer=new byte[8092];

  public Unzip(Log log){
    this.log=log;
  }

  public void extract(String filename)throws IOException{
    file=new ZipFile(filename);
    Enumeration files=file.entries();
    while(files.hasMoreElements()){
      extract((ZipEntry)files.nextElement());
    }
  }

  private void extract(ZipEntry e)throws IOException{
    String name=e.getName();
    if(name.endsWith("/")){
      new File(name).mkdirs();
      return;
    }
    log.msg("extracting "+name);
    FileOutputStream out=new FileOutputStream(name);
    InputStream in=file.getInputStream(e);
    int n=0;
    while((n=in.read(buffer))>0){
      out.write(buffer,0,n);
    }
    in.close();out.close();
  }
  

}

// Ian F Darwin, Java Cookbook, O'Reilly p.269