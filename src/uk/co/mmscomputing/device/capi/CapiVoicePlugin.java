package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.device.phone.*;

public class CapiVoicePlugin implements CapiPlugin{

  private PhoneAnswerer phoneAnswerer = null;
  private FaxReceiver   faxReceiver   = null;

  public CapiVoicePlugin(){
  }

  public void addListener(PhoneAnswererListener listener){
    if(phoneAnswerer==null){phoneAnswerer=new PhoneAnswerer();}
    phoneAnswerer.addListener(listener);
  }

  public void addListener(FaxReceiverListener listener){
    if(faxReceiver==null){faxReceiver=new FaxReceiver();}
    faxReceiver.addListener(listener);
  }

  public void update(Object data, Metadata metadata){          // called from CapiServerApplication

    if(data instanceof CapiMetadata.Indication){               // called from PickUp.indicateCall
      CapiMetadata.Indication call=(CapiMetadata.Indication)data;
      if(!call.isAccepted()){                                  // if another plugin has not yet accepted call
        try{
          if(phoneAnswerer!=null){
            call.setPlugin(this);

            PhoneAnswererMetadata md=new PhoneAnswererMetadata();

            md.setController(call.getController());
            md.setLocalNo(call.localno);
            md.setRemoteNo(call.remoteno);
            md.setState(PhoneConstants.phoneRinging);
            phoneAnswerer.fireListenerUpdate(md.STATE,md);     // tell application about state change

            int pickuptime=md.getPickUpTime();
            if(pickuptime>=0){
              call.setAccept(pickuptime*1000,md.getBlocker()); // accept call
//            call.setProtocol(new SpeechProtocol());          // SpeechProtocol is default; No need for this;
            }
          }
        }catch(Exception e){
          System.out.println("9\b"+getClass().getName()+".update\n\t"+e);
          System.err.println(getClass().getName()+".update\n\t"+e);
          e.printStackTrace();
        }
      }
      if(!call.isAccepted()){                                  // if another plugin has not yet accepted call
        try{
          if(faxReceiver!=null){
            call.setPlugin(this);

            FaxReceiverMetadata md=new FaxReceiverMetadata();

            md.setLocalNo(call.localno);
            md.setRemoteNo(call.remoteno);
            md.setState(FaxConstants.faxRinging);
            faxReceiver.fireListenerUpdate(md.STATE,md);       // tell application about state change
            int    pickuptime=md.getPickUpTime();
            String header=md.getHeader();
            String local=md.getLocalNo();

            if(pickuptime>=0){
              call.setAccept(pickuptime*1000,md.getBlocker()); // accept call
              call.setProtocol(new FaxBProtocol(0,0,local,header));// answer as fax
            }
          }
        }catch(Exception e){
          System.out.println("9\b"+getClass().getName()+".update\n\t"+e);
          System.err.println(getClass().getName()+".update\n\t"+e);
          e.printStackTrace();
        }
      }
    }
  }

  public void serve(CapiChannel channel){                      // from CapiSystem
    BProtocol protocol=channel.getProtocol();
    if(protocol instanceof SpeechProtocol){                    // if speech then call handler
      if(faxReceiver!=null){
        new CapiFaxDetector(channel).start();                  // start fax detection
      }
      new VoiceThread(channel).start();
    }else if(protocol instanceof FaxBProtocol){                // if fax then call fax handler
      new FaxThread(channel).start();
    }else{                                                     // this shouldn't happen!
      System.out.println("3\b"+getClass().getName()+".serve:\n\tUnknown Protocol.");
      try{
        channel.close();
      }catch(Exception e){
        System.out.println("3\b"+getClass().getName()+".serve:\n\t"+e);
      }
    }
  }

  private class VoiceThread extends Thread{

    CapiChannel             channel;
    PhoneCallHandler        pch;

    VoiceThread(CapiChannel channel){
      this.channel=channel;
      pch=null;
    }

    public void run(){
      try{
        PhoneAnswererMetadata md=new PhoneAnswererMetadata();

        md.setState(PhoneConstants.phoneConnected);
        phoneAnswerer.fireListenerUpdate(md.STATE,md);  // tell application accepted call
        pch=md.getHandler();                            // get a phone call handler

//        channel.put(new EchoCancellerReq.GetSupportedServicesReq(channel.getApplID(),channel.getLineID()));

        pch.run(
            channel.getLocalNo(),
            channel.getRemoteNo(),
            channel.getPCMInputStream(),
            channel.getPCMOutputStream()
        );

        md.setHandler(pch);
        md.setChangedProtocol(channel.getChangedProtocol());
        md.setState(PhoneConstants.phoneDisconnected);
        phoneAnswerer.fireListenerUpdate(md.STATE,md);  // tell app we are done
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
        System.err.println(getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          channel.close();
        }catch(Exception ioe){
          System.out.println("3\b"+getClass().getName()+".run:\n\t"+ioe);
        }
      }
    }
  }

  private class FaxThread extends Thread{

    CapiChannel         channel;

    public FaxThread(CapiChannel channel){
      this.channel=channel;
    }

    public void run(){
      try{
        FaxReceiverMetadata md=new FaxReceiverMetadata();

        md.setState(FaxConstants.faxConnected);
        faxReceiver.fireListenerUpdate(md.STATE,md);    // tell application accepted call

        FaxCallHandler fch=md.getHandler();             // get a fax call handler
        fch.run(
            channel.getLocalNo(),
            channel.getRemoteNo(),
            channel.getInputStream(),
            channel.getOutputStream()
        );

        md.setHandler(fch);
        md.setState(FaxConstants.faxDisconnected);
        faxReceiver.fireListenerUpdate(md.STATE,md);    // tell app we are done
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          channel.close();
        }catch(Exception ioe){System.out.println(getClass().getName()+".run:\n\t"+ioe);
          System.out.println("3\b"+getClass().getName()+".run:\n\t"+ioe);
        }
      }
    }
  }

  static private CapiVoicePlugin defaultPlugin=null;

  static public CapiVoicePlugin getDefaultPlugin(){
    if(defaultPlugin!=null){return defaultPlugin;}
    try{
      String osname=System.getProperty("os.name");
      if(osname.startsWith("Linux")
      || osname.startsWith("Windows")){
        defaultPlugin=new CapiVoicePlugin();
        CapiSystem.getSystem().addPlugin(defaultPlugin);
      }else{
        System.out.println("9\buk.co.mmscomputing.device.capi.CapiVoicePlugin.getDevice:\n\tUnsupported Operating System.");
      }
    }catch(Exception e){
      e.printStackTrace();
      System.out.println("9\buk.co.mmscomputing.device.capi.CapiVoicePlugin.getDevice:\n\tCould not open CapiVoicePlugin\n\t"+e);
    }
    return defaultPlugin;
  }
}
