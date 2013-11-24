package uk.co.mmscomputing.sms;

import java.io.*;
import java.util.*;
import java.text.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class SMSServiceCenter implements MetadataListener, Runnable{

  static public  CapiMetadata           md          = new CapiMetadata();

  static SimpleDateFormat sdfd=new SimpleDateFormat("/yyyy/MM/dd/");
  static SimpleDateFormat sdff=new SimpleDateFormat("/yyyy/MM/dd/yyyyMMdd-HmsS");

  private CapiServerApplication server=null;

  public SMSServiceCenter()throws IOException{
//    CapiEnumerator e=new CapiEnumerator();
//    System.out.println("CapiEnumerator:\n\n"+e.toString()+"\n\n");
  }

  private void answer(final CapiChannel phone,final String destfile)throws IOException{
    final InputStream  pin  =phone.getPCMInputStream();
    final OutputStream pout =phone.getPCMOutputStream();

    new Thread(){                                           
      public void run(){
        SMSSCChannel channel=new SMSSCChannel(pout,pin);
        try{
          channel.send(new SMSDLLEstablished());                // mobile station (MS) called us; SC acknowledges

          InputStream in = channel.getInputStream();
          int len;byte[] bytes=new byte[256];
          while((len=in.read(bytes))!=-1){                      // read until connection is closed
            System.out.println(new String(bytes,0,len));
          }
        }catch(Exception e){
          System.out.println(getClass().getName()+".answer:\n"+e);
          e.printStackTrace();
        }finally{
          try{
            channel.close();
          }catch(IOException ioe){
            System.out.println(getClass().getName()+".answer:\n"+ioe);
          }
        }
      }
    }.start();
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof CapiMetadata.Indication){
      handleIndication((CapiMetadata.Indication)data);
    }else if(data instanceof CapiException){
      System.out.println(data);
    }else if(data instanceof Exception){
      System.out.println(data);
//      System.out.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
    }else{
//      System.out.println(data);
    }
  }

  public void handleIndication(CapiMetadata.Indication mdi){

    System.out.println("local  no = "+mdi.localno);
    System.out.println("remote no = "+mdi.remoteno);

    mdi.setAccept(md.getInt("pickupTime"));
  }

  public void run(){

    System.out.println("running...");

    md.useMaxLogicalConnections(CapiEnumerator.getNoOfBChannels());
    md.useALaw();                               // set some defaults
    md.use64kBit();
    md.acceptAllCalls();
    md.useController(1);

    md.useLocalNo("0123456789");
    md.putInt("pickupTime",1000);

    md.addListener(this);

    try{
      System.out.println("create server...");
      server=new CapiServerApplication(md);
      new Thread(server).start();

      while(true){
        System.out.println("wait for call...");
        CapiChannel channel=server.accept();
        if(channel==null){break;}

          System.out.println(sdff.format(new Date())+": accepted call...");
          new File("uk/co/mmscomputing/sms"+sdfd.format(new Date())).mkdirs();

          String rno=channel.getRemoteNo();
          rno=rno.replaceAll("\\*\\*","");    // PBX: internal dialling

          BProtocol protocol=channel.getProtocol();
          if(protocol instanceof SpeechProtocol){
            answer(channel,"uk/co/mmscomputing/sms"+sdff.format(new Date())+"-"+channel.getLocalNo()+"-"+rno+".wav");        
          }else{
            System.err.println("Unsupported protocol.");
          }

      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  static private void RedirectSystemOut(String logfilename){
    try{
      PrintStream ps=new PrintStream(new FileOutputStream(logfilename));
//      System.setErr(ps);
      System.setOut(ps);
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

  void checkInput()throws IOException{
    BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
    String inputline;
    while((inputline=is.readLine())!=null){
//      System.out.println("input: "+inputline);
      if(inputline.equals("quit")){                      // type 'quit' on command line to quit:)
        server.close();
        break;
      }
    }
    is.close();
  }

  public static void main(String[] args){
//    RedirectSystemOut("/var/log/mmsc-sms.log");
    System.out.println(sdff.format(new Date())+": SMSServiceCenter");
    try{
      SMSServiceCenter app=new SMSServiceCenter();
      new Thread(app).start();
      app.checkInput();
    }catch(Exception e){
      System.out.println(e);
    }    
  }
}

