package uk.co.mmscomputing.device.capi.q931;

public class Q931 implements Q931Constants{

  static public boolean isExt(int o){return (o&0x0080)==0;}

  static public int getBits(int b, int e, int s){    // In Q.931 bits are numbered 8 .. 1
    s--;
    int mask=~(-1 << e)&(-1 << s);
    return (b & mask)>>s;
  }
}