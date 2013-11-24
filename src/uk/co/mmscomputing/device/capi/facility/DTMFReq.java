package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class DTMFReq extends FacilityReq{

  static final public int DTMF_START=1;
  static final public int DTMF_STOP=2;
  static final public int DTMF_SEND=3;

  public DTMFReq(int appid, int ncci, StructOut param){
    super(appid,ncci,CAPI_FACILITY_DTMF,param);
  }

  public DTMFReq(int appid, int ncci, int fct, String digits){
    this(appid,ncci,getFacReqParam(fct,digits));
  }

  public DTMFReq(int appid, int ncci, int fct){
    this(appid,ncci,getFacReqParam(fct));
  }

  static private StructOut getFacReqParam(int fct){
    StructOut dtmf=new StructOut(10);
    dtmf.writeWord(fct);				        //	function 'stop listening'
    dtmf.writeWord(40);					        //	tone-duration
    dtmf.writeWord(40);					        //	gap-duration
    dtmf.writeByte(0);				      	  //	
    dtmf.writeByte(2);                  //  characteristics of DTMF recognition
    dtmf.writeWord(0);                  //    default selectivity
    return dtmf;					              //	facility request parameter
  }

  static private StructOut getFacReqParam(int fct,String digits){
    // assert(digits.length()<255);
    int len=digits.length();
    StructOut dtmf=new StructOut(10+len);
    dtmf.writeWord(fct);				              //	function 'stop listening'
    dtmf.writeWord(40);					              //	tone-duration
    dtmf.writeWord(40);					              //	gap-duration
    dtmf.writeByte(len);				      	      //	struct len digits
    dtmf.writeData(digits.getBytes(),0,len);  //  characters to be sent, '0'..'9','*','#','A'..'D'
    dtmf.writeByte(2);                        //  characteristics of DTMF recognition
    dtmf.writeWord(0);                        //    default selectivity
    return dtmf;					                    //	facility request parameter
  }

  static public DTMFReq getStartReq(int appid, int plci){ 
    return new DTMFReq(appid,plci&0x0000FFFF,DTMFReq.DTMF_START);
  }

  static public DTMFReq getStopReq(int appid, int plci){ 
    return new DTMFReq(appid,plci&0x0000FFFF,DTMFReq.DTMF_STOP);
  }

  static public DTMFReq getSendReq(int appid, int plci, String digits){ 
    return new DTMFReq(appid,plci&0x0000FFFF,DTMFReq.DTMF_SEND,digits);
  }

}