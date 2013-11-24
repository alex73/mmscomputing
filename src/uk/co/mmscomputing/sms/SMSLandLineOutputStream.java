package uk.co.mmscomputing.sms;

import java.io.*;
import uk.co.mmscomputing.dsp.phone.FSKOutputStream;

public class SMSLandLineOutputStream extends FilterOutputStream{

  static private int T10 = 300;                             // BT SIN413 p.6; T10min = n * 100 ms  [1] 5.3.1   p.14 

  private     byte[]     bytes=new byte[178];               // buffer data
  private     int        count;

  public SMSLandLineOutputStream(OutputStream out){
    super(new FSKOutputStream(out));
    count=0;
  }

  private void writeChecksum()throws IOException{
    int checksum=0;
    for(int i=0;i<count;i++){                               // [1] 5.3.2.1 p.16
      checksum+=bytes[i]&0x00FF;
    }
    checksum=(-checksum)&0x00FF;                            // checksum: Two's complement modulo 256 of sum of bytes (excluding checksum byte).
    out.write(checksum);
  }

  // Data Link Layer message structure
  // Mark Signal | Message Type | Message Length | Payload (Transfer Layer Message) | Checksum

  protected void writeMessage(boolean segmented)throws IOException{
    if(bytes[0]==SMSConstants.SMS_DLL_EST){
      ((FSKOutputStream)out).writeDelay(T10);               // T10min = n * 100 ms  [1] 5.3.1   p.14 
    }else{
      ((FSKOutputStream)out).writeDelay(100);               // T11min = 100 ms  [1] 5.3.1   p.14 
    }
    ((FSKOutputStream)out).writeMarkSignal(80);             // 55 .. 80 .. 105; [1] 5.3.2.1 p.15
    if(segmented){ bytes[0]&=(byte)0x007F;                  // if segmented clear bit 7
    }else{         bytes[0]|=(byte)0x0080;                  // else set bit 7
    }
    bytes[1]=(byte)(count-2);                               // set length of message excluding checksum
    out.write(bytes,0,count);                               // write type length payload
    writeChecksum();                                        // write checksum
    ((FSKOutputStream)out).writeBits(-1,10);
  }

  public void write(int b)throws IOException{
    if(count==bytes.length){                                // send as segmented message
      writeMessage(true);
      count=2;                                              // byte 0: type needs to be send again; byte 1: length
    }
    bytes[count++]=(byte)b;                                 // buffer message data
    if(count==1){                                           // expect first byte to be type of message !
      count++;                                              // skip length here, set in writeMessage
    }
  }

  public void flush()throws IOException{                    // flush to send transport protocol data unit
    if(count>0){                                            
      writeMessage(false);
      count=0;
    }
    out.flush();
  }
}

// [1] Final draft ETSI ES 201 912 V1.2.1 (2004-06)
