package uk.co.mmscomputing.sms;

import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.protocol.*;

// A capi plugin that acts as a service centre. For test purposes.

public class SMSSCCapiPlugin implements CapiPlugin{

  private String centre="";                                    // service centre (SC) number + subaddress

  public SMSSCCapiPlugin(String scn,String sub){centre=scn+sub;}

  public void setServiceCentreNumber(String scn,String sub){centre=scn+sub;}

  public void update(Object data, Metadata metadata){          // called from CapiServerApplication
    if(data instanceof CapiMetadata.Indication){               // called from PickUp.indicateCall
      CapiMetadata.Indication call=(CapiMetadata.Indication)data;
      if(!call.isAccepted()&&call.localno.equals(centre)){     // if caller calls our service centre
        call.setPlugin(this);                                  // let us 'serve' call
        call.setAccept(0);                                     // pick up now
        call.setProtocol(new SpeechProtocol());                // expect voice call

        System.err.println("Accept call from "+call.remoteno);
      }
    }
  }

  public void serve(CapiChannel channel){                      // from CapiSystem
    BProtocol protocol=channel.getProtocol();
    if(protocol instanceof SpeechProtocol){                    // if speech then call handler
      new Thread(new SMSSCCapiReceiver(channel)).start();
    }else{                                                     // this shouldn't happen!
      try{
        channel.close();
      }catch(Exception e){
        System.out.println("3\b"+getClass().getName()+".serve:\n\t"+e);
      }
    }
  }

  public static void main(String[] args){
    try{
      String scn="22";
      String sub="0";
      if(args.length>0){scn=args[0];}
      if(args.length>1){sub=args[1];}

      System.out.println("Start listening for calls on local number "+scn+sub);
      SMSSCCapiPlugin plugin=new SMSSCCapiPlugin(scn,sub);
      CapiSystem.getSystem().addPlugin(plugin);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

// [1] ETSI ES 201 912 V1.2.1 (2004-06)
// [2] BT SIN 413
