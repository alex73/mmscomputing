package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;

public class SimpleModemDataReceiver implements MetadataListener, Runnable{

  static CapiMetadata md = new CapiMetadata();
  static final private int DefaultB3DataBlockSize=128;

  CapiServerApplication server;

  public SimpleModemDataReceiver()throws IOException{
    CapiEnumerator e=new CapiEnumerator();
//    System.out.println("CapiEnumerator:\n\n"+e.toString()+"\n\n");
  }

  private void receive(final CapiChannel channel,final String destfile){
    new Thread(){                                       // read capi input as byte data
      public void run(){
        try{
          int size;

          InputStream  in =channel.getInputStream();
          OutputStream out=new FileOutputStream(destfile);

          size  = (in.read()&0x00FF)<<24;
          size |= (in.read()&0x00FF)<<16;
          size |= (in.read()&0x00FF)<< 8;
          size |= (in.read()&0x00FF);

          int    maxBlocks = size/DefaultB3DataBlockSize;
          int    residue   = size%DefaultB3DataBlockSize;

          System.out.println("Expect "+size+" bytes 0x"+Integer.toHexString(size));

          int    len;
          int    count     = 0;
          byte[] buffer    = new byte[DefaultB3DataBlockSize];

          for(int i=0;(i<maxBlocks)&&((len=in.read(buffer))!=-1);i++){
            out.write(buffer,0,len);
            count+=len;
            System.out.println("Received "+count+" bytes of "+size);
          }
          len=in.read(buffer,0,residue);
          out.write(buffer,0,len);
          count+=len;
          System.out.println("Received "+count+" bytes of "+size);

          System.err.println((count==size)?"Successful Transfer":"Unsuccessful Transfer");
          out.close();

          out=channel.getOutputStream();
          out.write(count&0x00FF);count>>=8;
          out.write(count&0x00FF);count>>=8;
          out.write(count&0x00FF);count>>=8;
          out.write(count&0x00FF);
          out.flush();
          out.close();
//          channel.close();
        }catch(Exception ioe){
          ioe.printStackTrace();
        }
      }
    }.start();
  }

  public void update(Object data, Metadata metadata){
    if(data instanceof CapiMetadata.Indication){
      handleIndication((CapiMetadata.Indication)data);
    }else if(data instanceof Exception){
      System.err.println(data);
//      System.err.println(((Exception)data).getMessage());
      ((Exception)data).printStackTrace();
    }else{
      System.err.println(data);
    }
  }

  public void handleIndication(CapiMetadata.Indication mdi){

    mdi.setAccept(0);                 // 0=don't wait

    // network layer  : 7 : modem with all negotiations
    // data link layer: 7 : modem with full negotiation V.42
    // network layer  : 7 : modem


    /*Eicon Diva -> Eicon Diva
        uk.co.mmscomputing.device.capi.parameter.NCPI$Modem
        rate       = 33600
        protocol   = 0x11
        V.42/V.42 bis successfully negotiated.
        Compression successfully negotiated.

      AVM Fritz doesn't support this
    */

    // physical layer : 8 : modem async start/stop byte framing
    // data link layer: 1 : transparent
    // network layer  : 7 : modem

    /*Eicon Diva -> Eicon Diva
        uk.co.mmscomputing.device.capi.parameter.NCPI$Modem
        rate       = 33600
        protocol   = 0x4
        Transparent mode successfully negotiated.

      AVM Fritz -> Eicon Diva
        uk.co.mmscomputing.device.capi.parameter.NCPI$Modem
        rate       = 14400
        protocol   = 0x4
        Transparent mode successfully negotiated.

      Eicon Diva -> AVM Fritz
        Doesn't seem to work

      AVM Fritz -> AVM Fritz

        uk.co.mmscomputing.device.capi.parameter.NCPI$Modem
        rate       = 22284
        protocol   = 0x1
        V.42/V.42 bis successfully negotiated.
    */

    mdi.protocol=new BProtocol(8,1,7,StructOut.empty,StructOut.empty,StructOut.empty,StructOut.empty);
  }

  public void run(){

    md.useMaxLogicalConnections(CapiEnumerator.getNoOfBChannels());
    md.useALaw();                               // set some defaults
    md.use64kBit();
    md.acceptAllCalls();
    md.useController(1);

    md.addListener(this);

    try{
      server=new CapiServerApplication(md);
      server.start();

      while(true){
        CapiChannel channel=server.accept();
        if(channel==null){break;}
        receive(channel,"uk/co/mmscomputing/SMDRTest.txt");        
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void checkInput()throws IOException{
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
    System.err.println("SimpleModemDataReceiver");
    try{
      SimpleModemDataReceiver app=new SimpleModemDataReceiver();
      new Thread(app).start();
      app.checkInput();
    }catch(Exception e){
      System.err.println(e);
    }    
  }
}

