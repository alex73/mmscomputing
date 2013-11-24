package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;
import uk.co.mmscomputing.device.capi.ctrl.ConnectReq;

public class SimpleModemDataSender{

  private CapiCallApplication capi=null;

  public SimpleModemDataSender(CapiCallApplication caller)throws IOException{
    capi=caller;
  }

  public void send(String remote,File file){
    // physical layer : 8 : modem async start/stop byte framing
    // data link layer: 1 : transparent
    // network layer  : 7 : modem

    // network layer  : 7 : modem with all negotiations
    // data link layer: 7 : modem with full negotiation V.42
    // network layer  : 7 : modem

    try{
      BProtocol protocol=new BProtocol(8,1,7,StructOut.empty,StructOut.empty,StructOut.empty,StructOut.empty);

      int ctrlid = 1;                                                    // use first controller
      int cip    = 4;                                                    // 3.1 kHz audio (boecko: no echo cancellation, better for data transfer :) )
      ConnectReq msg = new ConnectReq(capi.getApplID(),ctrlid,cip,remote,"","","",protocol);

      CapiChannel channel = capi.connect(msg,120000);                    // give modem negotiation 120 sec

//      CapiChannel channel = capi.connect(cip,remote,120000,protocol);  // give modem negotiation 120 sec (cip = 1, speech)

      System.err.println("Connection successful.");

      InputStream in   = new FileInputStream(file);
      int datasize=(int)file.length();

      OutputStream out = channel.getOutputStream();

      System.out.println("File size is : "+datasize+"[0x"+Integer.toHexString(datasize)+"] bytes");

      byte[] size=new byte[4];                              // send file size first
      size[0]=(byte)((datasize>>24)&0x000000FF);
      size[1]=(byte)((datasize>>16)&0x000000FF);
      size[2]=(byte)((datasize>> 8)&0x000000FF);
      size[3]=(byte)((datasize>> 0)&0x000000FF);

      out.write(size,0,4);

//      channel.writeToOutput(in);
      int    count=datasize,len;
///*
      byte[] buffer=new byte[512];
      while((len=in.read(buffer))!=-1){
        count-=len;
        System.err.println("send "+len+" [0x"+Integer.toHexString(len)+"] => left to send : "+count);
        out.write(buffer,0,len);
      }
//*/
      in.close();
      in = channel.getInputStream();
      count = in.read()&0x00FF;
      count|= (in.read()&0x00FF)<<8;
      count|= (in.read()&0x00FF)<<16;
      count|= (in.read()&0x00FF)<<24;

      System.err.println("peer received "+count+" bytes");

      if(datasize==count){
        System.err.println("Successful Transfer");
      }else{
        System.err.println("Unsuccessful Transfer "+datasize+" "+count);
      }

      channel.close();                                      // 
    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }
  }

  public static void main(String[] argv){
    CapiCallApplication caller=null;
    try{
      String no="**30";
      if(argv.length>0){no=argv[0];}
      String file="uk/co/mmscomputing/test.txt";
      if(argv.length>1){file=argv[1];}
      System.err.println("Try to send file "+file+" to "+no);

      CapiMetadata md=new CapiMetadata();
      md.useMaxLogicalConnections(1);                     // need only one connection
      md.useController(1);                                // use first controller
      md.useALaw();                                       // set some defaults
      md.use64kBit();                                

      caller=new CapiCallApplication(md);
      caller.start();

      SimpleModemDataSender s=new SimpleModemDataSender(caller);
      s.send(no,new File(file));

    }catch(IOException ioe){
      System.err.println(ioe.getMessage());
    }finally{
      if(caller!=null){caller.close();}
    }
  }
}