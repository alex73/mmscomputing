package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.sound.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class EchoPhone implements Runnable{

  CapiCallApplication   appl;
  String                phoneno;

  public EchoPhone(String phoneno)throws IOException{
    this.phoneno=phoneno;                               // phone number to call

    CapiMetadata md=new CapiMetadata();

    md.useMaxLogicalConnections(1);                     // need only one connection
    md.useController(1);                                // use first controller
    md.useALaw();                                       // set some defaults
    md.use64kBit();                                

    appl=new CapiCallApplication(md);
    appl.start();                                       // start capi thread
  }

  public void run(){
    try{
      try{
        System.err.println("Try connecting to "+phoneno+". Will wait for 10 sec.");
        CapiChannel channel=appl.connect(phoneno,10000);  // send connect request and wait for connection (max 10 sec.)
        System.err.println("Connected to "+phoneno);
/*
//      send pure data back

        InputStream in=channel.getInputStream();                   // echo everthing that we get back
        in.skip(in.available());                                   // discard data we couldn't process in time
        channel.writeToOutput(in);         
        in.close();
*/
//      convert into PCM and back, then send back;  raw -> PCM -> raw

///*
        InputStream in=channel.getPCMInputStream();                // echo everthing that we get back
        in.skip(in.available());                                   // discard data we couldn't process in time
        channel.writeToPCMOutput(in);         
        in.close();
//*/
        System.err.println("close capi channel");
        channel.close();
      }catch(Exception e){
        System.err.println(e);
      }finally{
        System.err.println("close capi application");
        appl.close();
      }
    }catch(Exception e){
      System.err.println(e);
    }
    System.err.println("Type 'quit' to end program.");             // to quit checkInput
  }

  public void checkInput()throws IOException{
    BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
    String inputline;
    while((inputline=is.readLine())!=null){
//      System.out.println("input: "+inputline);
      if(inputline.equals("quit")){                      // type 'quit' on command line to quit:)
        appl.close();
        break;
      }
    }
    is.close();
  }

  public static void main(String[] argv){
    String no="**20";
    if(argv.length>0){no=argv[0];}
    System.err.println("\nStart Echo Phone\n\tcall: "+no);
    System.err.println("\n\tType 'quit' on command line to quit:)\n");
    try{
      EchoPhone phone=new EchoPhone(no);
      new Thread(phone).start();
      phone.checkInput();
    }catch(Exception e){
      System.err.println(e);
    }    
    System.err.println("End Echo Phone.");
  }
}

