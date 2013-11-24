package uk.co.mmscomputing.device.capi.q931;

import java.util.Vector;
import java.util.Enumeration;

import uk.co.mmscomputing.device.capi.*;

public class Q931Message implements Q931Constants{

//  ITU Recommendation Q.931 (03/93)  p53  4.2.

  private int    callref;
  private int    type;
  private Vector list    = new Vector();

  public Q931Message(int callref,int type){
    this.callref=callref;
    this.type=type;
  }

  protected void add(Q931 ie){
    list.add(ie);
  }

  public String toString(){
    String s="Q931 Message\n";
    s+=" msg type = "+MessageTypeStrs[type]+"\n";
    s+=" call ref = "+callref+"\n";
    for(Enumeration e = list.elements() ; e.hasMoreElements() ;){
      s+=e.nextElement().toString()+"\n";
    }    
    return s;
  }
/*
  static public Q931Message create(byte[] data,int off){
    if(data[off++]!=0x08){throw new IllegalArgumentException();} // Q.931 protocol discriminator is 0x08
    int len = data[off++];                                       // octet 2 length call reference
    int callref = 0;
    for(int i=0;i<len;i++){                                      // octet 2+
      callref = (callref << 8) | (data[off++] & 0x00FF);
    }
    int type = data[off++]&0x00FF;                               // message type

    Q931Message msg = new Q931Message(callref,type);

    while(off<data.length){
      Q931 qmsg = Q931Factory.create(data,off);
      msg.add(qmsg);
      off+=2+qmsg.getLength();
    }
    return msg;
  }
*/
  static public Q931Message create(Rider r){
    if(r.read()!=0x08){throw new IllegalArgumentException();}    // Q.931 protocol discriminator is 0x08
    int len = r.read();                                          // octet 2 length call reference
    int callref = 0;
    for(int i=0;i<len;i++){                                      // octet 2+
      callref <<= 8;
      callref  |= r.read();
    }
    int type = r.read();                                         // message type

    Q931Message msg = new Q931Message(callref,type);
    try{
      while(true){msg.add(Q931Factory.create(r));}
    }catch(IndexOutOfBoundsException ioobe){}
    return msg;
  }

}
