package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import uk.co.mmscomputing.device.capi.*;

public class FaxSender {

  int    timeout;
  String faxheader;

  public FaxSender(){
    timeout    = 60000;                                   // timeout in milliseconds (including speed negotiation)
    faxheader  = "my fax header";
  }

  public void send(String remoteno,String faxfile)throws IOException{
    InputStream fin     = new FileInputStream(faxfile);
    CapiSystem  capi    = CapiSystem.getSystem();         // use capi system
    try{                                                  // try to connect
      CapiChannel channel = capi.getCaller().faxconnect(remoteno,timeout,faxheader); 
          
      System.err.println("START SENDING file "+faxfile);
      try{
        OutputStream out = channel.getOutputStream();
        channel.getInputStream().close();                 // waste input data
        channel.writeToOutput(fin);
      }finally{
        channel.close();                                  // initiate disconnect
      }
      System.err.println("STOPPED SENDING file "+faxfile);
    }finally{
      capi.close();                                       // release capi resources
    }
    fin.close();
  }

  public static void main(String[] args){
    System.err.println("\nSTART FaxSender\n");

    try{
      String no="**30";
      if(args.length>0){no=args[0];}                       // phone number first
      String file="uk/co/mmscomputing/device/capi/samples/testfax.sff";
      if(args.length>1){file=args[1];}                     // file to send

      FaxSender sender=new FaxSender();
      sender.send(no,file);

    }catch(Exception e){
      System.err.println(e);
    }
    System.err.println("\nFINISHED FaxSender\n");
  }
}