package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.sms.*;
import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.device.capi.protocol.*;

public class CapiSMSPlugin implements CapiPlugin{

  private SMSPluginReceiver receiver;

  public CapiSMSPlugin(SMSPluginReceiver receiver){
    this.receiver=receiver;
  }

  public void update(Object data, Metadata metadata){          // called from CapiServerApplication
    if(data instanceof CapiMetadata.Indication){               // called from PickUp.indicateCall
      CapiMetadata.Indication call=(CapiMetadata.Indication)data;

      if(!call.isAccepted()){
        SMSMetadata md=new SMSMetadata();
        md.setState(SMSConstants.smsRinging);
        md.setLocalNo(call.localno);
        md.setRemoteNo(call.remoteno);
        receiver.update(md.STATE,md);                          // tell application about call indication

        if(md.getAccept()){
          call.setPlugin(this);                                // let us 'serve' call
          call.setAccept(0);                                   // pick up now
          call.setProtocol(new SpeechProtocol());              // expect voice call

//        System.err.println("Accept sms call from "+call.remoteno);
        }
      }
    }
  }

  public void serve(CapiChannel channel){                      // from CapiSystem
    new SMSThread(channel).start();
  }

  private class SMSThread extends Thread{

    CapiChannel channel;

    public SMSThread(CapiChannel channel){
      this.channel=channel;
    }

    public void run(){
      try{
        SMSMetadata md=new SMSMetadata();

        md.setState(SMSConstants.smsConnected);
        receiver.update(md.STATE,md);                          // tell application accepted call

        SMSPluginHandler sh=md.getHandler();
        sh.run(
            channel.getLocalNo(),
            channel.getRemoteNo(),
            channel.getPCMInputStream(),
            channel.getPCMOutputStream()
        );
        md.setState(SMSConstants.smsDisconnected);
        receiver.update(md.STATE,md);                          // tell app we are done
      }catch(Exception e){
        System.out.println("9\b"+getClass().getName()+".run\n\tDisconnected call.\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          channel.close();
        }catch(Exception ioe){
          System.err.println(getClass().getName()+".run:\n\t"+ioe);
          System.out.println("3\b"+getClass().getName()+".run:\n\t"+ioe);
        }
      }
    }
  }
}