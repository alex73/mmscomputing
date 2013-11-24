package uk.co.mmscomputing.device.capi.parameter;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.*;

public class AdditionalInfo{

  // use: uk.co.mmscomputing.device.capi.ctrl.ConnectInd

  static public class In{

    private  byte[]          BChannelInfo     =null;
    private  KeypadFacility  kpf              =null;
    private  UserUser        uu               =null;
    private  byte[]          FacilityDataArray=null;
    private  byte[]          SendingComplete  =null;

    public In(Rider r){
      r.structBegin();
      try{
        BChannelInfo=r.readStruct();         // BChannelInfo
        kpf = new KeypadFacility(r);
        uu  = new UserUser(r);
        FacilityDataArray=r.readStruct();    // FacilityDataArray
      }catch(IndexOutOfBoundsException ioobe){
      }finally{
        r.structEnd();
      }
    }

    public String getKeypadInformation(){return kpf.getInformation();}
    public String getUserUserInformation(){return uu.getInformation();}

    public String toString(){
      String s="Additional Information";
      if(kpf!=null){s+="\n\n"+kpf.toString();}
      if(uu !=null){s+="\n\n"+uu.toString();}
      s+="\n\n";
      return s;
    }
  }

  // use: uk.co.mmscomputing.device.capi.plc.AlertReq
  // use: uk.co.mmscomputing.device.capi.ctrl.ConnectReq
  // use: uk.co.mmscomputing.device.capi.ctrl.ConnectResp
  // use: uk.co.mmscomputing.device.capi.ctrl.DisconnectReq
  // use: uk.co.mmscomputing.device.capi.ctrl.InfoReq

  static public class Out extends StructOut{

    static private StructOut defaultSendingComplete;

    public Out(
      StructOut BChannelInfo, 
      StructOut KeyPadFacility, 
      StructOut UserUserData, 
      StructOut FacilityDataArray,
      StructOut SendingComplete
    ){
      super(
         BChannelInfo.getLength()
        +KeyPadFacility.getLength()
        +UserUserData.getLength()
        +FacilityDataArray.getLength()
        +SendingComplete.getLength()
      );
      writeStruct(BChannelInfo);
      writeStruct(KeyPadFacility);
      writeStruct(UserUserData);
      writeStruct(FacilityDataArray);
      writeStruct(SendingComplete);
    }

    public Out(KeypadFacility.Out kpf){
      super(3+kpf.getLength()+defaultSendingComplete.getLength());
      writeStruct();
      writeStruct(kpf);
      writeStruct();
      writeStruct();
      writeStruct(defaultSendingComplete);
    }

    public Out(UserUser.Out uu){
      super(3+uu.getLength()+defaultSendingComplete.getLength());
      writeStruct();                       // +1
      writeStruct();                       // +1
      writeStruct(uu);                     // +uu length
      writeStruct();                       // +1
      writeStruct(defaultSendingComplete); // +2
    }

    static{
      defaultSendingComplete=new StructOut(2);
      defaultSendingComplete.writeWord(0);
    }
  }
}