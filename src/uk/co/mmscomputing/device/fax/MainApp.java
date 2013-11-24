package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import uk.co.mmscomputing.device.capi.CapiVoicePlugin;

public class MainApp implements FaxConstants,FaxReceiverListener{

  private File parent;

  public MainApp(){
    parent = new File(System.getProperty("user.home"),"mmsc"+File.separator+"fax"+File.separator+"rec");
    parent.mkdirs();
  }

  public void update(FaxReceiverMetadata.Type type, FaxReceiverMetadata md){
    try{
      if(type.equals(FaxReceiverMetadata.STATE)){
        if(md.isState(faxRinging)){
          md.setPickUpTime(5);                    // wait for 5 sec
        }else if(md.isState(faxConnected)){
          md.setHandler(new Handler());
        }else if(md.isState(faxDisconnected)){
          Handler fch=(Handler)md.getHandler();
          System.out.println("New Fax: "+fch.file.getAbsolutePath());
        }
      }
    }catch(Exception e){
      System.out.println(getClass().getName()+".update:\n\t"+e);
      e.printStackTrace();
    }
  }

  static private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

  private class Handler implements FaxCallHandler{

    File file;

    public void run(String local,String remote,InputStream pin,OutputStream pout){
      OutputStream out=null;
      try{
        String rno=remote.replaceAll("\\*\\*","");    // PBX: internal dialling
        String destfile=sdf.format(new Date())+local+"_"+rno+".sff";
        file = new File(parent,destfile);
        out  = new FileOutputStream(file);

        int    count;
        byte[] buffer = new byte[2048];

        while((count=pin.read(buffer))!=-1){
          out.write(buffer,0,count);
        }

      }catch(Exception e){
        System.out.println(getClass().getName()+".run:\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          if(out!=null){out.flush();out.close();}
        }catch(Exception e){}        
      }
    }
  }

  public static void main(String[] argv){
    try{
      MainApp app=new MainApp();
      CapiVoicePlugin.getDefaultPlugin().addListener(app);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}