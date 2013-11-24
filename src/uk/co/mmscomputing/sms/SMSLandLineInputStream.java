package uk.co.mmscomputing.sms;

import java.io.*;
import uk.co.mmscomputing.dsp.phone.FSKInputStream;

public class SMSLandLineInputStream extends FilterInputStream implements SMSConstants{

  // Data Link Layer [1] 5.3.2

  protected boolean       sc2ms;                      // Service Center to Mobile Station or vice versa

  protected SMSDataUnitListener listener;

  private     byte[]     bytes;                       // buffer data
  private     int        len,index;

  public SMSLandLineInputStream(InputStream in,SMSDataUnitListener listener,boolean direction){
    super(new FSKInputStream(in));
    this.listener = listener;
    this.sc2ms    = direction;
  }

  protected byte[] readDataUnit()throws IOException{
    byte[]  bytes=null;
    boolean isSegmented;
    int     type,hlen,alen,csum;

    if(((FSKInputStream)in).readMarkSignal(55)){      // MARK signal; 55 .. 80 .. 105 MARK bits; [1] p.15
      byte[]  buf=new byte[176];
      ByteArrayOutputStream data=new ByteArrayOutputStream(buf.length);
      type=in.read()&0x00FF;    
      data.write(type&0x007F);                        // cache type at beginning of data array
      do{
        isSegmented=((type&0x80)==0);
        hlen=in.read()&0x00FF;
        if((hlen+1)!=in.available()){                 // available bytes: payload plus checksum
          throw new SMSException.Length();
        }
        alen=in.read(buf,0,hlen);
        csum=in.read()&0x00FF;                                   

        int checksum=type+hlen;                       // evaluate checksum
        for(int i=0;i<alen;i++){                      // [1] 5.3.2.1 p.16
          checksum+=buf[i]&0x00FF;
        }
        checksum=(-checksum)&0x00FF;                  // checksum: Two's complement modulo 256 of sum of bytes (excluding checksum byte).
        if(checksum!=csum){throw new SMSException.Checksum();}

        data.write(buf,0,alen);

        if(isSegmented){

        // to do: send acknowledgement here

          if(!((FSKInputStream)in).readMarkSignal(55)){
            throw new SMSException.Unspecified("Corrupt segmented message: Missing data chunk.");
          }
          type=in.read()&0x00FF;                      
        }
      }while(isSegmented);
      bytes=data.toByteArray();

      System.out.println(toString(bytes,bytes.length));
    }
    return bytes;
  }

  public int read()throws IOException{
    while(!(index<len)){
      try{
        bytes=readDataUnit();
        if(bytes==null){return -1;}
        SMSDataUnit tpdu=SMSDataUnitFactory.decode(sc2ms,bytes);
        listener.received(tpdu);                          // Tell listener about new data unit.
        bytes=tpdu.getUserData();                         // data: i.e. the actual text message
        len  =bytes.length;                               // length of message
        index=0;
      }catch(SMSException smse){
        System.err.println(getClass().getName()+".read:\n\t"+smse);

        smse.printStackTrace();

        if(smse instanceof SMSException.Length){
          listener.send(new SMSDLLError(SMS_DLL_ERROR_LENGTH));
        }else if(smse instanceof SMSException.Checksum){
          listener.send(new SMSDLLError(SMS_DLL_ERROR_CHECKSUM));
        }else if(smse instanceof SMSException.Type){
          listener.send(new SMSDLLError(SMS_DLL_ERROR_TYPE));
        }else if(smse instanceof SMSException.Unspecified){
          listener.send(new SMSDLLError(SMS_DLL_ERROR_UNSPECIFIED));
        }
        in.read(new byte[in.available()],0,in.available());// waste message
        index=len;                                        // read next message
      }
    }
    return bytes[index++];
  }

  public int read(byte[] buf, int off, int len)throws IOException{
    if(buf==null){
      throw new NullPointerException(getClass().getName()+".read(byte[] buf, int off, int len): buf is null");
    }
    if((off<0)||(len<0)||(buf.length<(off+len))){
      throw new IndexOutOfBoundsException(getClass().getName()+".read(byte[] buf, int off, int len): index off ["+off+"] or len ["+len+"] out of bounds ["+buf.length+"].");
    }
    int  b;
    int  count=0;
    while(count<len){
      b=read();
      if(b==-1){return (count==0)?-1:count;}
      buf[off++]=(byte)b;
      count++;
    }
    return count;
  }

  static String[] hexs={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

  static public String toString(byte[] bytes,int len){
    String s="\n";

    int i=0;
    while(i<len){
      s+=" ";
      s+=hexs[(bytes[i]>>4)&0x0F];
      s+=hexs[(bytes[i]   )&0x0F];
      if(((i+1)%8)==0){s+="\n";}
      i++;
    }
    if(((i+1)%8)!=0){s+="\n";}

    return s;
  }

}


// [1] ETSI ES 201 912 V1.2.1 (2004-06)
// [2] ETS 300 659-1/2         (1997-02)

