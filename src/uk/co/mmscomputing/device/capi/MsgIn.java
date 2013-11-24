package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.device.capi.ctrl.*;
import uk.co.mmscomputing.device.capi.plc.*;
import uk.co.mmscomputing.device.capi.ncc.*;
import uk.co.mmscomputing.device.capi.facility.*;
import uk.co.mmscomputing.device.capi.man.*;               // manufacturer specific features

public class MsgIn implements CapiConstants{

  public int len;
  public int appid;
  public int cmd;
  public int scmd;
  public int msgno;
  public int lineid;

  public MsgIn(Rider r){
    r.reset();
    len    =r.readWord();           // 0:  len message
    appid  =r.readWord();           // 2:  application id
    cmd    =r.read();               // 4:  command
    scmd   =r.read();               // 5:  sub command (conf/ind)
    msgno  =r.readWord();           // 6:  message number
    lineid =r.readDWord();          // 8:  ncci(2),pcli(1),ctrl(1)
  }                                 // 12:

  public String toString(){
    String s=getClass().getName()+"\n";
    s+="length = "+len+"\n";
    s+="appid  = "+appid+"\n";
    s+="cmd    = 0x"+Integer.toHexString(cmd)+"\n";
    s+="scmd   = 0x"+Integer.toHexString(scmd)+"\n";
    s+="msgno  = "+msgno+"\n";
    s+="lineid = 0x"+Integer.toHexString(lineid)+"\n";
    return s;
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

  static public MsgIn createConf(Rider r,int cmd){
    switch(cmd){
    case CAPI_CONNECT:            return new ConnectConf(r);
    case CAPI_CONNECT_B3:         return new ConnectB3Conf(r);
    case CAPI_DISCONNECT_B3:      return new DisconnectB3Conf(r);
    case CAPI_DISCONNECT:         return new DisconnectConf(r);
    case CAPI_DATA_B3:            return new DataB3Conf(r);
    case CAPI_LISTEN:             return new ListenConf(r);
    case CAPI_ALERT:              return new AlertConf(r);
    case CAPI_INFO:               return new InfoConf(r);
    case CAPI_FACILITY:           return FacilityConf.create(r);
    case CAPI_RESET_B3:           return new ResetB3Conf(r);
    case CAPI_SELECT_B_PROTOCOL:  return new SelectBProtocolConf(r);
    case CAPI_MANUFACTURER:       return ManufacturerConf.create(r);
    }
    System.out.println("3\bMsgIn.createConf:\n\tUnknown CONF command in CAPI confirmation ["+Integer.toHexString(cmd)+"]");  
    return new CapiConfMsg(r);
  }

  static public MsgIn createInd(Rider r,int cmd){
    switch(cmd){
    case CAPI_CONNECT_ACTIVE:         return new ConnectActiveInd(r);
    case CAPI_CONNECT_B3_ACTIVE:      return new ConnectB3ActiveInd(r);
    case CAPI_CONNECT_B3_T90_ACTIVE:  return new ConnectB3T90ActiveInd(r);
    case CAPI_DISCONNECT:             return new DisconnectInd(r);
    case CAPI_DISCONNECT_B3:          return new DisconnectB3Ind(r);
    case CAPI_CONNECT:		            return new ConnectInd(r);
    case CAPI_DATA_B3:		            return new DataB3Ind(r);
    case CAPI_CONNECT_B3:	            return new ConnectB3Ind(r);
    case CAPI_INFO:	  	              return new InfoInd(r);
    case CAPI_FACILITY:			          return FacilityInd.create(r);
    case CAPI_RESET_B3:               return new ResetB3Ind(r);
    case CAPI_MANUFACTURER:	          return ManufacturerInd.create(r);
    }
    System.out.println("bMsgIn.createInd:\n\tUnknown IND command in CAPI indication ["+Integer.toHexString(cmd)+"]");  
    throw new IllegalArgumentException();
  }

  static public MsgIn create(Rider r){
    r.skip(4);
    int cmd =r.read();
    int scmd=r.read();
    r.skip(6);
    try{
      switch(scmd){                 // sub command
      case CAPI_CONF:	return createConf(r,cmd);
      case CAPI_IND:	return createInd(r,cmd);
      }
      System.out.println("3\bMsgIn.create : Unknown sub command ["+Integer.toHexString(scmd)+"]");
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }
    return new MsgIn(r);
  }

  static public MsgIn create(byte[] data){
    return create(new Rider(data,0,(data[0]&0x00FF)|((data[0]&0x00FF)<<8)));
  }
}

