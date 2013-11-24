package uk.co.mmscomputing.device.capi;

public class Rider{

  private int[]  stack = new int[32];
  private byte[] data;
  private int    off,sp,index;

  public Rider(){
    this.data      = new byte[0];
    this.off       = 0;
    this.sp        = 0;
    this.stack[sp] = 0;
    this.index     = 0;
  }

  public void set(byte[] data,int off,int len){
    this.data      = data;
    this.off       = off;
    this.sp        = 0;
    this.stack[sp] = len;
    this.index     = 0;    
  }

  public void set(byte[] data){
    set(data,0,(data[0]&0x00FF)|((data[1]&0x00FF)<<8));
  }

  public Rider(byte[] data){
    set(data,0,data.length);
  }

  public Rider(byte[] data,int off,int len){
    set(data,off,len);
  }

  public void setLength(){                           // read and set length of content
    int len=read();                                  // read 1 byte length
    stack[++sp]=len+index;                           // set len to current byte position plus value of byte read
  }

//  public int getLength(){return stack[sp];}

/*
  public void setStructLength(){                     // read and set length of struct content; return old length
    int len=read();                                  // read 1 byte length
    if(len==255){len=readWord();}                    // if escape code read 2 byte length
    stack[++sp]=len;
  }
*/

  public void skip(int i){
    if(stack[sp]<=(index+i)){throw new IndexOutOfBoundsException(""+(index+i));}
    index+=i;
  }

  public void skip(){
    index=stack[sp--];
  }

  public int available(){
    return stack[sp]-index;
  }

  public void reset(){sp=0;index=0;}

  public void structBegin(){                         // read and set length of struct content
    int len=read();                                  // read 1 byte length
    if(len==255){len=readWord();}                    // if escape code read 2 byte length
    stack[++sp]=len+index;
  }

  public void structEnd(){
    index=stack[sp--];
  }

  public int read(){
    if(index<stack[sp]){                             // len can be smaller than data.length
      int b = data[off+index]&0x00FF;
      index++;
      return b;
    }
    throw new IndexOutOfBoundsException();
  }
/*
  public int readByte(){
    if(index<stack[sp]){                             // len can be smaller than data.length
      int b = data[off+index]&0x00FF;
      index++;
      return b;
    }
    throw new IndexOutOfBoundsException();
  }
*/
  public int  readWord() { 
    return read()|(read()<<8);
  }

  public int  readDWord(){ 
    return read()|(read()<<8)|(read()<<16)|(read()<<24);
  }

  public long readQWord(){ 
    return read()|(read()<<8)|(read()<<16)|(read()<<24)|(read()<<32)|(read()<<40)|(read()<<48)|(read()<<56);
  }

  public String readString(){
    String s = new String(data,off+index,stack[sp]-index);
    index=stack[sp];
    return s;
  }

  public byte[] getBytes(){
    byte[] buf=new byte[stack[sp]-index];
    System.arraycopy(data,off+index,buf,0,buf.length);
    return buf;
  }

  public byte[] readStruct(){
    structBegin();
    byte[] buf=getBytes();
    structEnd();
    return buf;
  }

  static String[] hexs={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

  public String toString(){
    String s="Rider data - \n index = "+index+" sp = "+stack[sp]+"\n";

    int    v;
    int i = index;
    while(i<stack[sp]){
      s+=" ";
      s+=hexs[(data[i]>>4)&0x0F];
      s+=hexs[(data[i]   )&0x0F];
      if(((i+1)%8)==0){s+="\n";}
      i++;
    }
    if(((i+1)%8)!=0){s+="\n";}
    return s;
  }
}
