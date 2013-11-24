package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.sound.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.sound.*;
import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.parameter.*;
import uk.co.mmscomputing.device.capi.q931.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class KeypadCodeSender implements MetadataListener, Runnable{

  static AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);
  static int         bufsize   = 512;

  CapiCallApplication   appl;
  CapiMetadata          md;
  Thread                thread;
  String                keypadcode;

  public KeypadCodeSender(String localno, String keypadcode)throws IOException{
    this.keypadcode=keypadcode;                        // phone number to accept

    md=new CapiMetadata();

    md.useMaxLogicalConnections(1);                     // need only one connection
    md.useController(1);                                // use first controller
    md.useALaw();                                       // set some defaults
    md.use64kBit();                                

    md.addListener(this);                               // want to listen; 

    md.putString("capi.localno",localno);               // your local number

    appl=new CapiCallApplication(md);
    appl.start();                                       // start capi thread
  }

  private CapiChannel connectKeyPad(String keypadcode)throws IOException{

    int bandwidth   = md.getInt("capi.bandwidth");
    int    ctrlid   = md.getInt("capi.controller.id");
    String localno  = md.getString("capi.localno");

    System.out.println("msn = "+localno+" dial keypad code = "+keypadcode);

    KeypadFacility.Out kpf=new KeypadFacility.Out(keypadcode);
    AdditionalInfo.Out addInfo=new AdditionalInfo.Out(kpf);

    StructOut calling=new CallingPartyNumber.Out(localno);

    ConnectReq msg=new ConnectReq(
      appl.getApplID(),ctrlid,appl.CIP_SPEECH,
      StructOut.empty,
      calling,
      StructOut.empty,
      StructOut.empty,
      new SpeechProtocol(),
      StructOut.empty,StructOut.empty,StructOut.empty,
      addInfo
    );
    return appl.connect(msg,20000);
  }

  public void run(){
    try{
      System.err.println("Try connecting to "+keypadcode+". Will wait for 20 sec.");
      final CapiChannel channel=connectKeyPad(keypadcode); // send connect request and wait for connection (max 20 sec.)
      System.err.println("Connected to "+keypadcode);
      try{
        SourceDataLine out=SoundMixerEnumerator.getOutputLine(pcmformat,bufsize);  // get default sound output i.e. speaker
        CapiInputStream input=channel.getInputStream();
        InputStream in=new PCMInputStream(input,channel.isALaw());;
        int    count, bytesWritten=0;
        byte[] buffer = new byte[bufsize];

        out.start();
        while(channel.isOpen()&&((count=in.read(buffer))!=-1)){
          out.write(buffer,0,count);
          bytesWritten+=count;
        }
        in.close();
        out.drain();
        out.stop();
        out.close();
        System.err.println("wrote input "+bytesWritten+" byte(s)");        
      }catch(Exception e){
        e.printStackTrace();
        System.err.println(e.getMessage());
      }
      channel.close();
    }catch(Exception e){
      e.printStackTrace();
      System.err.println(e.getMessage());
    }finally{
      appl.close();
    }
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof DisconnectInd){      // disconnected -> close application
    }else if(data instanceof Exception){
      System.err.println(data);
//      System.err.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
    }else{
      System.err.println(data);
    }
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

  /*
    BT-Land: PSTN
    *#001#                    "No services are in operation on this line"
             ISDN
    keypad facility does not seem to exist

    Germany: ISDN
    *21*NumberToForwardTo#    program & activate call forwarding
    *21#                      activate call forwarding
    #21#                      deactivate call forwarding
    *#21#                     interrogate about call forwarding

    many more
  */

  public static void main(String[] argv){
    try{
      String localno="12345";
      if(argv.length>0){localno=argv[0];}
      String keypadcode="*#001#";
      if(argv.length>1){keypadcode=argv[1];}
      System.err.println("\nStart KeypadCodeSender : msn="+localno+" keypad code="+keypadcode);
      System.err.println("\n\tType 'quit' on command line to quit:)\n");
      KeypadCodeSender phone=new KeypadCodeSender(localno,keypadcode);
      new Thread(phone).start();
      phone.checkInput();
    }catch(ArrayIndexOutOfBoundsException aioobe){
      System.err.println("\n\nInvalid arguments:\nUsage: java uk.co.mmscomputing.device.capi.samples.KeypadCodeSender localno keypadcode\n\n");
    }catch(Exception e){
      e.printStackTrace();
    }    
    System.err.println("End KeypadCodeSender.");
  }
}
