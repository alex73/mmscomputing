package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import uk.co.mmscomputing.util.metadata.Metadata;
import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.protocol.FaxBProtocol;

public class FaxReceiver implements CapiPlugin{

  static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

  private File faxdir;

  public FaxReceiver(){
    File userhome=new File(System.getProperty("user.home"),"mmsc");
    faxdir = new File(userhome,"fax"+File.separator+"rec");
    faxdir.mkdirs();
  }

  public void update(Object data, Metadata metadata){          // called from CapiServerApplication
    if(data instanceof CapiMetadata.Indication){               // called from PickUp.indicateCall
      CapiMetadata.Indication call=(CapiMetadata.Indication)data;
      if(!call.isAccepted()){                                  // if another plugin has not yet accepted call
        try{
          call.setPlugin(this);                                // we will handle this call
          call.setProtocol(new FaxBProtocol(0,0,"",""));       // answer as fax
          call.setAccept(0);                                   // accept call now

          System.out.println("ACCEPTED CALL for "+call.localno+" from "+call.remoteno);
        }catch(Exception e){
          System.out.println("9\b"+getClass().getName()+".update\n\t"+e);
          System.err.println(getClass().getName()+".update\n\t"+e);
          e.printStackTrace();
        }
      }
    }
  }

  public void serve(CapiChannel channel){                      // called from CapiSystem
    new FaxThread(channel).start();
  }

  private class FaxThread extends Thread{

    private CapiChannel         channel;

    public FaxThread(CapiChannel channel){
      this.channel=channel;
    }

    public void run(){
      try{
        channel.getOutputStream().close();                     // we don't send anything

        String rno = channel.getRemoteNo();
        rno=rno.replaceAll("\\*\\*","");                       // PBX: internal dialling
        String nostr = channel.getLocalNo()+"_"+rno;

        File file = new File(faxdir,sdf.format(new Date())+nostr+".sff");
        FileOutputStream fout = new FileOutputStream(file);

        System.out.println("START RECEIVING "+file.getName());
        channel.writeInputTo(fout);
        fout.close();
        System.out.println("RECEIVED "+file.getName());
      }catch(Exception e){
        System.out.println(getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          channel.close();
        }catch(Exception ioe){System.out.println(getClass().getName()+".run:\n\t"+ioe);
          System.out.println(getClass().getName()+".run:\n\t"+ioe);
        }
      }
    }
  }

  public void checkInput()throws IOException{
    BufferedReader is=new BufferedReader(new InputStreamReader(System.in));
    String inputline;
    while((inputline=is.readLine())!=null){
      if(inputline.equals("quit")){                            // type 'quit' on command line to quit:)
        CapiSystem.getSystem().close();
        break;
      }
    }
    is.close();
  }

  public static void main(String[] args){
    System.err.println("\nSTART FaxReceiver\n");

    try{
      FaxReceiver fax=new FaxReceiver();
      CapiSystem.getSystem().addPlugin(fax);                   // tell capi system about our plugin
      fax.checkInput();
    }catch(Exception e){
      System.err.println(e);
    }
    System.err.println("\nFINISHED FaxReceiver\n");
  }
}