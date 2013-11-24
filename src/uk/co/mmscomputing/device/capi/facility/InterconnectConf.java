package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class InterconnectConf extends FacilityConf{

  public static final int GetSupportedServices=0;
  public static final int ConnectActive       =1;
  public static final int Disconnect          =2;

  protected int function;
  protected int services;
  protected int interconnectsCtrl;
  protected int participantsCtrl;
  protected int interconnectsAll;
  protected int participantsAll;

  public InterconnectConf(Rider r){
    super(r);
    function=r.readWord();
    r.structBegin();                      // Line Interconnect Confirmation parameter
    switch(function){
    case GetSupportedServices: setSupportedServices(r);break;
    case ConnectActive:        setConnectActive(r);    break;
    case Disconnect:           setDisconnect(r);       break;
    }
  }

  private void setSupportedServices(Rider r){
    if(info!=0){return;}
    info              =r.readWord();
    services          =r.readDWord();
    interconnectsCtrl =r.readDWord();
    participantsCtrl  =r.readDWord();
    interconnectsAll  =r.readDWord();
    participantsAll   =r.readDWord();
  }

  private void setConnectActive(Rider r){
    if(info!=0){return;}
    info              =r.readWord();
//    r.setStructLength();                  // Line Interconnect Connect Confirmation Participant
//    while(index<data.length){             // sequence of structs ?
//    }
//    r.skip();
  }

  private void setDisconnect(Rider r){
    if(info!=0){return;}
    info              =r.readWord();
//    r.setStructLength();                  // Line Interconnect Disconnect Confirmation Participant
//    while(index<data.length){             // sequence of structs ?
//    }
//    r.skip();
  }

  public String toString(){
    String s=super.toString();
    s+="function : "+function+"\n\t";
    s+="function : "+info+"\n\t";
    if(function==GetSupportedServices){
      s+="function : "+services+"\n\t";
      s+="function : "+interconnectsCtrl+"\n\t";
      s+="function : "+participantsCtrl+"\n\t";
      s+="function : "+interconnectsAll+"\n\t";
      s+="function : "+participantsAll+"\n\t";
    }else if(function==ConnectActive){
//      s+="Line Interconnect Connect Confirmation Participant\n\t"+toString(getBytes());
    }else if(function==Disconnect){
//      s+="Line Interconnect Disconnect Confirmation Participant\n\t"+toString(getBytes());
    }
    return s;
  }
}

