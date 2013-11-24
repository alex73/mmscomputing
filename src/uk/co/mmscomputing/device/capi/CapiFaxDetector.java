package uk.co.mmscomputing.device.capi;

import java.io.*;

public class CapiFaxDetector extends Thread{

  CapiChannel             channel;

  public CapiFaxDetector(CapiChannel channel){
    this.channel=channel;
  }

  public void run(){

    Thread.currentThread().setName(getClass().getName()+".0x"+Integer.toHexString(channel.getLineID()));

    try{
      // if we receive 3 fax cng-tones then assume analogue group 3 fax
 
      CapiController ctrl=CapiEnumerator.getController(channel);
      if(ctrl.isSupportingDTMF()){

        //  cng tone = 1100Hz 0.5s long every 3 sec, capi sends 'X'.
        //  assume it is a fax if we have received at least 3 cngs within timeToWaitForCNGs [30] secs

        channel.startDTMF();
        String dtmf=channel.getDTMFDigits(3,30000);         // try for 30 secs to collet 3 Xs
        channel.stopDTMF();

//      System.out.println("DTMF = "+dtmf);

        if(dtmf.startsWith("XXX")){
          if(ctrl.isSupportingGroup3Fax()){

            ((CapiNCC)channel).selectFaxProtocol();         // switch protocol from speech to fax

//          System.out.println(getClass().getName()+".run\n\tSwitched to FAX Protocol.");

            // this will switch protocol of physical line
            // the logical ncc (here: channel) will be torn down 
            // and a new ncc channel will wait at server.accept

          }else{
            System.out.println("3\bController "+ctrl.getName()+" does not support Group 3 FAX. Handle as sound data.");
          }
        }
      }else{
        System.out.println("3\bController "+ctrl.getName()+" does not support DTMF detection. Handle as sound data.");
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
