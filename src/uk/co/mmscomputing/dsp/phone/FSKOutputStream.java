package uk.co.mmscomputing.dsp.phone;

import java.io.*;

// 1200baud FSK => 1200bit/sec => 1200/8000 = 6.666 samples per bit => 3 bits = 7+6+7 samples

public class FSKOutputStream extends FilterOutputStream{

  static private final int[] mark2space={
     0, 1,66,59,52,45,38,31,24,17,10,37,76,69,62,55,48,41,34,27,
    20,13, 6,22,72,65,58,51,44,37,30,23,24, 9, 2,45,68,61,54,47,
    40,41,26,19,12, 5,78,71,64,57,50,77,36,29,22,15, 8, 1,74,67,
    60,53,46,62,32,25,18,11, 4,77,70,63,64,49,42, 5,28,21,14, 7,
  };

  static private final int[] space2mark={
     0,57,34,11,68,45,22,79,56,33,10,67,44,21,78,55,32, 9,66,43,
    20,77,54,31, 8,65,42,19,76,53,30, 7,64,41,18,45,52,29, 6,63,
    40,17,74,51,28, 5,62,39,16,73,50,27, 4,61,38,15,72,49,26, 3,
    60,37,14,71,48,25, 2,59,36,13,70,47,24, 1,58, 5,12,69,46,23,
  };

  static private int[][] amps=new int[2][80];

         private int     counter=1,phase,lastsymbol=1;
         private byte[]  pcm=new byte[2];

  public FSKOutputStream(OutputStream out){
    super(out);
    phase=0;
  }

  protected void writeSample(int sample)throws IOException{
    pcm[0]=(byte) sample;
    pcm[1]=(byte)(sample>>8);
    out.write(pcm);                                    // write sample to pcm stream
  }

  public void writeDelay(int milliseconds)throws IOException{
    int samples=milliseconds<<3;                       // samples to send = 8000Hz * 0.001s
    for(int i=0;i<samples;i++){
      writeSample(0);
    }
  }

  public void writeBit(int symbol)throws IOException{  // symbol: SPACE = 0; MARK = 1
    int   samples;
    int[] amps    = this.amps[symbol&0x01];

    if(symbol!=lastsymbol){
      phase=(lastsymbol==0)?space2mark[phase]:mark2space[phase];
    }
    counter++;
    if(counter==3){                                    // 7,6,7,7,6,7,...
      samples = 6;counter = 0;
    }else{
      samples = 7;      
    }
    for(int i=0;i<samples;i++){
      writeSample(amps[phase]);
      phase=(phase+1)%amps.length;
    }    
    lastsymbol=symbol;
  }

  public void writeBits(int b,int bits)throws IOException{
    for(int i=0;i<bits;i++){                           // send first bit 0 then bit 1 .. and last bit 7
      writeBit(b&0x01);
      b>>=1;                                           // ROR
    }    
  }

  public void write(int b)throws IOException{
    writeBit(0);                                       // start bit
    writeBits(b,8);
    writeBit(1);                                       // stop bit
  }

  public void write(byte[] b)throws IOException{
    write(b,0,b.length);
  }

  public void write(byte[] b,int off,int len)throws IOException{
    for(int i=0;i<len;i++){write(b[off+i]);}
  }

  public void writeMarkSignal(int len)throws IOException{
    writeBits(-1,len);    // mark signal; begin of message signal
  }

  static{
    for(int i=0;i<80;i++){
      amps[0][i]=(int)(8192.0*Math.sin(2.0*Math.PI*((double)i)*2100.0/8000.0-0.001));
      amps[1][i]=(int)(8192.0*Math.sin(2.0*Math.PI*((double)i)*1300.0/8000.0-0.001));
    }
  }

  public static void main(String[] argv){
    try{
      String file="uk/co/mmscomputing/dsp/phone/fsk.raw";

      FSKOutputStream out=new FSKOutputStream(new FileOutputStream(file));

      out.writeMarkSignal(80); // mark signal; begin of message signal

      out.write("Hello world!\n".getBytes());
      out.write("ABCDEFGHIJKLMNOPQRSTUVWXYZ.".getBytes());

      out.writeBits(-1,10);    // end of message signal

      out.flush();
      out.close();

    }catch(Exception e){
      e.printStackTrace();
    }
  }      
}

// [1] ETS 300 659-1/2         (1997-02)
