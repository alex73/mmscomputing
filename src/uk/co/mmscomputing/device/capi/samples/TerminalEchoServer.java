package uk.co.mmscomputing.device.capi.samples;

import java.io.*;

import uk.co.mmscomputing.util.metadata.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.ncc.DisconnectB3Req;
import uk.co.mmscomputing.device.capi.protocol.BProtocol;

public class TerminalEchoServer implements MetadataListener, Runnable{

  private CapiServerApplication server;

  public TerminalEchoServer()throws IOException{
//    CapiEnumerator e=new CapiEnumerator();
//    System.out.println("CapiEnumerator:\n\n"+e.toString()+"\n\n");
  }

  private void receive(final CapiChannel channel,final String destfile){
    new Thread(){                                       // read capi input as byte data
      public void run(){
        try{
          InputStream  in =channel.getInputStream();
          OutputStream out=channel.getOutputStream();

          OutputStream fout=new FileOutputStream(destfile);

          int count=0,b;
          while((b=in.read())!=-1){
            if(b==0x1B){break;}                         // ESC quit
            System.out.println("Received Byte ["+count+"]"+((char)b)+" "+Integer.toHexString(b));
            out.write(b);out.flush();                   // send received characters straight back
            fout.write(b);                              // save data to file
            count++;
          }

          fout.close();

          channel.close();
        }catch(Exception e){
          e.printStackTrace();
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
    System.out.println(data);
  }

  public void handleIndication(CapiMetadata.Indication mdi){

    // minicom default is 8N1 8bit; no parity; 1 stop bit

    mdi.setAccept(0);                         // 0=don't wait

    // physical layer : 8 : modem async start/stop byte framing
    // data link layer: 1 : transparent
    // network layer  : 7 : modem

    StructOut b1=new StructOut(12);           // B1 configuration protocol 8; Modem asynch start/stop byte frames; [1]p.113
    b1.writeWord(0);                          // max bit rate 1200,2400...; 0=adaptive
    b1.writeWord(8);                          // bits per character
    b1.writeWord(0);                          // parity 0=>None 1=>Odd 2=>even
    b1.writeWord(0);                          // stop bits 0=>1bit, 1=>2bit
    b1.writeWord(2);                          // options
    b1.writeWord(3);                          // 0: none; 2: V.100; 3: speed negotiation - V.8

    /*
      If I use 8,1,7 I get a lot of random data first.
    */
 
//    mdi.protocol=new BProtocol(8,1,7,b1,StructOut.empty,StructOut.empty,StructOut.empty);
//    mdi.protocol=new BProtocol(8,1,7,StructOut.empty,StructOut.empty,StructOut.empty,StructOut.empty);

    // network layer  : 7 : modem with all negotiations
    // data link layer: 7 : modem with full negotiation V.42
    // network layer  : 7 : modem

//    mdi.protocol=new BProtocol(7,7,7,StructOut.empty,StructOut.empty,StructOut.empty,StructOut.empty);
    /*
      If I use 7,7,7 default I do not get any random data.

       uk.co.mmscomputing.device.capi.parameter.NCPI$Modem
        rate       = 33600
        protocol   = 0x11
        V.42/V.42 bis successfully negotiated.
        Compression successfully negotiated.
    */
    mdi.protocol=new BProtocol(7,7,7,b1,StructOut.empty,StructOut.empty,StructOut.empty);
  }

  public void run(){
    CapiMetadata md = new CapiMetadata();

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
        receive(channel,"uk/co/mmscomputing/TESTest.txt");        
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
    System.err.println("TerminalEchoServer");
    try{
      TerminalEchoServer app=new TerminalEchoServer();
      new Thread(app).start();
      app.checkInput();
      System.err.println("Stopped TerminalEchoServer");
    }catch(Exception e){
      e.printStackTrace();
    }    
  }
}

