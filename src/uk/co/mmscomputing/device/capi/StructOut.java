package uk.co.mmscomputing.device.capi;

public class StructOut{

  static public final StructOut empty=new StructOut(0);

  private byte[] data;
  private int    index;

  public StructOut(int len){
    data=new byte[len];
    index=0;
  }

  public StructOut(byte[] buf){
    data=buf;
    index=buf.length;
  }

  public StructOut(String str){
    data=str.getBytes();
    index=str.length();
  }

  public byte[] getBytes(){return data;}
  public int    getIndex(){return index;}
  public void   setIndex(int i){index=i;}

  public int getLength(){                    // length of a struct (see writeStruct)
    return(index<255)?index+1:index+3;
  }

  public void writeByte(int i){
    try{
      data[index++]=(byte)i;
    }catch(IndexOutOfBoundsException ioobe){ // This shouldn't happen. Programming error.
      System.out.println(getClass().getName()+".writeByte:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      System.err.println(getClass().getName()+".writeByte:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      ioobe.printStackTrace();
    }
  }

  public void writeWord(int i){
    try{
      data[index++]=(byte)i;                 // LSB least significant byte
      data[index++]=(byte)(i>>8);            // MSB most significant byte
    }catch(IndexOutOfBoundsException ioobe){ // This shouldn't happen. Programming error.
      System.out.println(getClass().getName()+".writeWord:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      System.err.println(getClass().getName()+".writeWord:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      ioobe.printStackTrace();
    }
  }

  public void writeDWord(int i){
    try{
      data[index++]=(byte)i;                 // LSB least significant byte
      data[index++]=(byte)(i>>8);
      data[index++]=(byte)(i>>16);
      data[index++]=(byte)(i>>24);           // MSB most significant byte
    }catch(IndexOutOfBoundsException ioobe){ // This shouldn't happen. Programming error.
      System.out.println(getClass().getName()+".writeWord:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      System.err.println(getClass().getName()+".writeWord:\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      ioobe.printStackTrace();
    }
  }

  public void writeQWord(long l){
    writeDWord((int)(l&0x00000000FFFFFFFFL));
    writeDWord((int)((l>>32)&0x00000000FFFFFFFFL));
  }

  public void writeData(byte[] src, int off, int len){
    try{
      System.arraycopy(src,off,data,index,len);index+=len;
    }catch(IndexOutOfBoundsException ioobe){ // This shouldn't happen. Programming error.
      System.out.println(getClass().getName()+"\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  

      System.err.println("buffer.length = "+data.length);
      System.err.println("index         = "+index);
      System.err.println("length        = "+len);

      System.err.println(getClass().getName()+"\n\tINTERNAL ERROR !!! Please send error log to mmsc.\n\t"+ioobe);  
      ioobe.printStackTrace();
    }
  }

  public void writeStruct(){
    writeByte(0);                            //  empty struct
  }

  public void writeStruct(StructOut struct){
    int len = struct.getBytes().length;
    if(len==0){ 
      writeByte(0);
    }else{
      if(len<255){
        writeByte(len);
      }else{
        writeByte(255);                      //  escape
        writeWord(len);
      }
      writeData(struct.getBytes(),0,len);
    }
  }

  public void dump(){                        // print out bytes to command line
    System.err.println(getClass().getName()+".dump : ");
    for(int i=0;i<index;i++){
      int val=data[i]&0x000000FF;
      String sval=Integer.toHexString(val);
      if(sval.length()>2){
        sval=sval.substring(sval.length()-2);
      }else if(sval.length()<2){
        sval="0"+sval;
      }
      System.err.println("\t["+i+"] = 0x"+sval+" "+val);
    }
    System.err.println();
  }

  public void dump(java.io.OutputStream out)throws java.io.IOException{
    out.write((getClass().getName()+".dump : \r\n").getBytes());
    for(int i=0;i<index;i++){
      int val=data[i]&0x000000FF;
      String sval=Integer.toHexString(val);
      if(sval.length()>2){
        sval=sval.substring(sval.length()-2);
      }else if(sval.length()<2){
        sval="0"+sval;
      }
      out.write(("\t["+i+"] = 0x"+sval+" "+val+"\r\n").getBytes());
    }
    out.write('\r');out.write('\n');
  }

  public String toString(){
    return getClass().getName()+"\n";
  }

  static String[] hexs={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

  static public String toString(byte[] data){
    int    v;
    String s="\n";
    int i=0;
    while(i<data.length){
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