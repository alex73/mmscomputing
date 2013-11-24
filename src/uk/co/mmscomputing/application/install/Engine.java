package uk.co.mmscomputing.application.install;

import java.io.*;
import java.util.*;

public class Engine{

  private Log    log;
  private String javac="";

  public Engine(Log log){
    this.log=log;
  }

  public void print(InputStream in){
    try{
      BufferedReader r=new BufferedReader(new InputStreamReader(in));

      String s;
      while((s=r.readLine())!=null){
        log.msg(s);
      }        
    }catch(Exception e){
      log.err(e);
    }
  }

  public void run(String javac, String classpath, String programname){
    String cmd="";
    String java="";
    String osname=System.getProperty("os.name");
    if(osname.startsWith("Windows")){
      if(javac.endsWith(".exe")){
        java=javac.substring(0,javac.length()-5);
      }else{
        java=javac.substring(0,javac.length()-1);
      }
      cmd+="\""+java+"\"";
    }else{
      java+=javac.substring(0,javac.length()-1);
      cmd+=java;
    }
    cmd+=" "+programname;
    final String runcmd=cmd;


//    final String cmd="java -cp "+classpath+" "+programname;

    log.msg("Start : "+cmd);    
    new Thread(){
      public void run(){
        try{
          final Process p=Runtime.getRuntime().exec(runcmd);
          new Thread(){ public void run(){ print(p.getInputStream()); }}.start();
          print(p.getErrorStream());
        }catch(Exception e){
          log.err(e);
        }
        log.msg("End : "+runcmd);
      }
    }.start();
  }

  public void traverse(String dir, String javac)throws Exception{
    this.javac=javac;
    traverse(dir);
  }

  private void traverse(String dir)throws Exception{
    String[] dirs=listDir(dir);
    for(int i=0; i<dirs.length; i++){
      traverse(dir+"/"+dirs[i]);
    }
    compileDir(dir);
  }

  private void compileDir(String dir)throws Exception{
    String[] files=list(dir);
    for(int i=0; i<files.length; i++){
      compile(dir+"/"+files[i]);
    }
  }

  public void compile(String filename)throws Exception{

    String cmd;
    String osname=System.getProperty("os.name");
    if(osname.startsWith("Windows")){
      cmd="\""+javac+"\" \""+filename.replace('\\','/')+"\"";
    }else{
      cmd=javac+" "+filename.replace('\\','/');
    }
    log.msg("Compile "+cmd);

    final Process p=Runtime.getRuntime().exec(cmd);
    new Thread(){ public void run(){ print(p.getInputStream()); }}.start();
    print(p.getErrorStream());
  }

  public String[] list(String dir){
    return list(dir, new JavaFNF());
  }

  public String[] listDir(String dir){
    return list(dir, new DirFNF());
  }

  public String[] list(String dir, FilenameFilter fnf){
    String dirs[]=new java.io.File(dir).list(fnf);
    if(dirs==null){ log.msg("ERROR list : "+dir); return new String[0];}
    java.util.Arrays.sort(dirs);
    return dirs;
  }

  class JavaFNF implements FilenameFilter{
    public boolean accept(File dir, String s){
      s=dir+"/"+s;
      File f=new File(s);
      return f.isFile() && s.endsWith(".java");
    }
  }

  class DirFNF implements FilenameFilter{
    public boolean accept(File dir, String s){
      s=dir+"/"+s;
      File f=new File(s);
      return f.isDirectory();
    }
  }

  public void metainf(String fn, String spi){
    try{
      log.msg("META-INF/services/"+fn+"    "+spi);
      File dir=new File("META-INF/services/");
      dir.mkdirs();
      File service=new File("META-INF/services/"+fn);
      service.createNewFile();  // create the file if it not already exists

      BufferedReader in=new BufferedReader(new FileReader(service));
      String line;
      while((line=in.readLine())!=null){
        if(spi.equals(line.trim())){ return;}
      }
      in.close();
      FileWriter out=new FileWriter(service,true);//append
      out.write(spi+"\n");
      out.flush();
      out.close();      
    }catch(Exception e){
      log.err(e);
    }
  }

  public void metainf(){
    metainf("javax.imageio.spi.ImageReaderSpi","uk.co.mmscomputing.imageio.bmp.BMPImageReaderSpi");    
    metainf("javax.imageio.spi.ImageReaderSpi","uk.co.mmscomputing.imageio.sff.SFFImageReaderSpi");    
    metainf("javax.imageio.spi.ImageReaderSpi","uk.co.mmscomputing.imageio.ppm.PPMImageReaderSpi");    
    metainf("javax.imageio.spi.ImageReaderSpi","uk.co.mmscomputing.imageio.tiff.TIFFImageReaderSpi");    

    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.bmp.BMPImageWriterSpi");    
    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.sff.SFFImageWriterSpi");    
    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.ppm.PBMImageWriterSpi");    
    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.ppm.PGMImageWriterSpi");    
    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.ppm.PPMImageWriterSpi");    
    metainf("javax.imageio.spi.ImageWriterSpi","uk.co.mmscomputing.imageio.tiff.TIFFImageWriterSpi");    

    metainf("javax.sound.sampled.spi.MixerProvider","uk.co.mmscomputing.sound.provider.MixerProvider");        

//    metainf("javax.sound.sampled.spi.MixerProvider","uk.co.mmscomputing.device.capi.sound.CapiMixerProvider");        
  }
}