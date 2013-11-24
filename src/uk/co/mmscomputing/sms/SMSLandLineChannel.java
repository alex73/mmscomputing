package uk.co.mmscomputing.sms;

import java.io.*;
import uk.co.mmscomputing.concurrent.*;

public class SMSLandLineChannel implements SMSDataUnitListener{

  static final private int T12 = 4000;                     // [1] 5.3.2.3 p.16 timeout

  private SMSLandLineOutputStream out;
  private SMSLandLineInputStream  in;

  private boolean    isopen;
  private int        errs=0;
  private SMSDataUnit lastMsg=null;
  private SMSReceiver listener=null;

  private boolean    timedout;
  private Thread     timer=null;
  private Semaphore  blocker=new Semaphore(0,true);

  public SMSLandLineChannel(OutputStream pcmout,InputStream pcmin,SMSReceiver l){   
    out = new SMSLandLineOutputStream(pcmout);
    in  = new SMSLandLineInputStream(pcmin,this,true);
    listener = l;

    this.isopen=true;
  }

  public OutputStream getOutputStream(){return out;}
  public InputStream getInputStream(){return in;}

  private void setTimer(){
    if(timer==null){
      timer=new Thread(){
        public void run(){
          try{
            while(isopen){
              timedout=true;
              blocker.tryAcquire(T12,TimeUnit.MILLISECONDS);
              if(timedout){
                System.out.println(getClass().getName()+".run:\nTransfer timed out.");
                send(new SMSDLLReleased());
                close();
              }
            }
          }catch(InterruptedException e){
          }catch(Exception e){
            System.err.println(getClass().getName()+".run:\n"+e);
          }
          timer=null;                                          // System.out.println("STOPPED TIMER THREAD");
        }
      };
      timer.start();
    }
  }

  private void resetTimer(){
    timedout=false;
    blocker.release();
  }

  // TE sends Submit, Command, DeliverReport

  public void send(SMSDataUnit msg)throws IOException{
    if(isopen){
      lastMsg=msg;                             // cache last message
      msg.writeTo(out);                        // send message
      resetTimer();
    }
  }

  // TE receives Deliver, StatusReport, SubmitReport

  public void received(SMSDataUnit msg)throws IOException{
//    System.err.println(msg.toString());

    resetTimer();
    
    if(msg instanceof SMSDLLEstablished){        // data link layer messages
      errs=0;
    }else if(msg instanceof SMSDLLError){
      errs++;
      if(errs<2){                                // [1] 5.3.2.2 p.16
        send(lastMsg);                           // send last sent message again
      }else{
        send(new SMSDLLReleased());
        close();                                 // release connection, if message is confirmed negative twice
      }
    }else if(msg instanceof SMSDLLReleased){
      close();
    }else{                                       // SMS transport layer messages
      errs=0;
      if(msg instanceof SMSDeliver){            // received valid SMS message
        send(new SMSDeliverAckReport());
      }else if(msg instanceof SMSStatusReport){ // ditto
        send(new SMSDeliverAckReport());
      }else if(msg instanceof SMSSubmitAckReport){
      }else if(msg instanceof SMSSubmitErrReport){ // we'll close connection
        send(new SMSDLLReleased());
        close();                                 // [1] 5.3.3.2 p.17
      }
    }
    listener.received(msg);
  }

  public void close()throws IOException{
    if(isopen){
      isopen=false;
      errs=0;
      resetTimer();
      out.close();
      in.close();
    }
  }
}

// [1] ETSI ES 201 912 V1.2.1 (2004-06)


