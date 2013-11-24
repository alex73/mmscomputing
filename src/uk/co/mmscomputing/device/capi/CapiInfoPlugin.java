package uk.co.mmscomputing.device.capi;

import java.io.*;

import uk.co.mmscomputing.util.metadata.*;

class CapiInfoPlugin implements CapiPlugin{

  CapiInfoPlugin(){}

  public void update(Object data, Metadata metadata){          // called from CapiServerApplication
    if(data instanceof CapiMetadata.Indication){               // called from PickUp.indicateCall
      CapiMetadata.Indication call=(CapiMetadata.Indication)data;
      if(!call.isAccepted()){                                  // if another plugin has not yet accepted call
        call.setPlugin(this);
        call.setIgnore();                                      // if nobody wants it we ignore it.

        System.out.println("Ignore call from "+call.remoteno+" to "+call.localno);
      }
    }else if(data instanceof String){
      System.out.println((String)data);
    }
  }

  public void serve(CapiChannel channel){                      // from CapiSystem
    try{                                                       // shouldn't get here! We did not accept call!
      if(channel!=null){channel.close();}
    }catch(Exception e){
      System.out.println("3\b"+getClass().getName()+".serve:\n\t"+e);
    }
  }
}
