package uk.co.mmscomputing.device.capi;

import java.io.*;
import javax.swing.*;

import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.device.capi.protocol.*;

public class CapiFaxSender extends FaxSender{

  public CapiFaxSender(){
  }

  public void send()throws IOException{
    md.setState(faxRinging);
    fireListenerUpdate(md.STATE);

    String header = md.getHeader()+"-"+md.getLocalNo();
    String rno    = md.getRemoteNo();
    String lno    = md.getLocalNo();
    int    time   = md.getTimeOut();

    File         file    = new File(md.getFile());

//    long         filelen = file.length();
//    System.out.println("\n\nFax filelen = "+filelen);

    InputStream  in      = new FileInputStream(file);

    if(md.progressmonitor){in=new ProgressMonitorInputStream(null,"Fax: sending ... to "+rno+"\n"+md.getFile(),in);}
    in=new BufferedInputStream(in);

//    System.out.println("send fax rno="+rno+" file="+file);

    try{
      CapiSystem  capisystem = CapiSystem.getSystem();  // use capi system
      CapiChannel channel    =                          // try to connect
          capisystem.getCaller().faxconnect(rno,md.getBlocker(),time*1000,lno,header);

      md.setState(faxConnected);fireListenerUpdate(md.STATE);

      md.setInfo("Connected ...");
      fireListenerUpdate(md.INFO);
      try{
        int    count;
        byte[] buffer = new byte[CapiConstants.DefaultB3DataBlockSize];

        CapiOutputStream out = channel.getOutputStream();
        channel.getInputStream().close();               // waste input data

        while((count=in.read(buffer))!=-1){
          out.write(buffer,0,count);
        }
        out.close();
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".send:\n\t"+e);
        e.printStackTrace();
      }finally{
        channel.close();                                // initiate disconnect
      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".send:\n\tCould not send fax\n\t"+e);
      e.printStackTrace();
    }
    in.close();
    md.setState(faxDisconnected);fireListenerUpdate(md.STATE);
  }

  public boolean isAPIInstalled(){return true;/*capi.isInstalled();*/}

  static public FaxSender getDevice(){return new CapiFaxSender();}
}
