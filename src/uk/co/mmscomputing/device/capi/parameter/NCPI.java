package uk.co.mmscomputing.device.capi.parameter;

import uk.co.mmscomputing.device.capi.*;

public class NCPI implements CapiConstants{

  public int protocol;

  public NCPI(){}

  static public class Transparent extends NCPI{// protocol 0
    public Transparent(){}
  }  

  static public class T90NL extends NCPI{      // protocol 1
    public T90NL(){}
  }  

  static public class ISO8082 extends NCPI{    // protocol 2
    public int flags;
    public int group;
    public int channel;
    public byte[] contents;

    public ISO8082(Rider r){
      try{
        flags    = r.read();
        group    = r.read();
        channel  = r.read();
        contents = new byte[r.available()];
        for(int i=0;i<contents.length;i++){
          contents[i]=(byte)r.read();
        }
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }
    }

    public String toString(){
      String s=super.toString();
      s+="flags     = 0x"+Integer.toHexString(flags)+"\n\t";
      s+="group     = 0x"+Integer.toHexString(group)+"\n\t";
      s+="channel   = 0x"+Integer.toHexString(channel)+"\n\t";
      if(contents!=null){s+=MsgIn.toString(contents);}
      return s;
    }
  }

  static public class X25 extends NCPI{        // protocol 3
    public int options;
    public int group;
    public int channel;
    public byte[] contents;

    public X25(Rider r){
      try{
        options  = r.read();
        group    = r.read();
        channel  = r.read();
        contents = new byte[r.available()];
        for(int i=0;i<contents.length;i++){
          contents[i]=(byte)r.read();
        }
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }
    }

    public String toString(){
      String s=super.toString();
      s+="options   = 0x"+Integer.toHexString(options)+"\n\t";
      s+="group     = 0x"+Integer.toHexString(group)+"\n\t";
      s+="channel   = 0x"+Integer.toHexString(channel)+"\n\t";
      if(contents!=null){s+=MsgIn.toString(contents);}
      return s;
    }
  }

  static public class T30Fax extends NCPI{     // protocol 4
    public int bitrate;
    public int resolution;
    public int format;
    public int noofpages;
    public String remoteno;

    public T30Fax(Rider r){
      try{
        bitrate    = r.readWord();
        resolution = r.readWord();
        format     = r.readWord();
        noofpages  = r.readWord();
        remoteno   = r.readString();
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }
    }
  
    public String toString(){
      String s=super.toString();

      s+="bitrate     = "+bitrate+"\n\t";
      switch(resolution){
      case 0:    s+="resolution  = Standard\n\t";break;
      case 1:    s+="resolution  = High\n\t";break;
      default:   s+="resolution  = "+resolution+"\n\t";break;
      }
      switch(format){
      case 0:    s+="format      = SFF\n\t";break;
      case 1:    s+="format      = Plain\n\t";break;
      case 2:    s+="format      = PCX\n\t";break;
      case 3:    s+="format      = DCX\n\t";break;
      case 4:    s+="format      = TIFF\n\t";break;
      case 5:    s+="format      = ASCII\n\t";break;
      case 6:    s+="format      = Extended Ansi\n\t";break;
      case 7:    s+="format      = Binary file transfer\n\t";break;
      default:   s+="format      = "+format+"\n\t";break;
      }
      s+="noofpages   = "+noofpages+"\n\t";
      s+="remoteno    = \""+remoteno+"\"\n\t";

      return s;
    }
  }

  static public class T30FaxExt extends NCPI{  // protocol 5
    public int bitrate;
    public int options;
    public int format;
    public int noofpages;
    public String remoteno;

    public T30FaxExt(Rider r){
      try{
        bitrate    = r.readWord();
        options    = r.readWord();
        format     = r.readWord();
        noofpages  = r.readWord();
        remoteno   = r.readString();
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }
    }
  
    public String getOptionsDesc(){
      String s="";
      s+="options   = 0x"+Integer.toHexString(options)+"\n\t";
      if((options&(1<< 0))!=0){s+="Enable high resolution.\n\t";}
      if((options&(1<< 1))!=0){s+="Fax-polling request / indication.\n\t";}
      if((options&(1<< 2))!=0){s+="Req/Ind to send/poll another document after current document.\n\t";}
      if((options&(1<<10))!=0){s+="JPEG colour images.\n\t";}
      if((options&(1<<11))!=0){s+="JBIG grayscale/colour images.\n\t";}
      if((options&(1<<12))!=0){s+="JBIG progressive bi-level images.\n\t";}
      if((options&(1<<13))!=0){s+="MR compressed images.\n\t";}
      if((options&(1<<14))!=0){s+="MMR compressed images.\n\t";}
      if((options&(1<<15))!=0){s+="Not an ECM (Error Correction Mode) connection.\n\t";}
      return s;
    }

    public String toString(){
      String s=super.toString();

      s+="bitrate     = "+bitrate+"\n\t";
      s+=getOptionsDesc();
      switch(format){
      case 0:    s+="format      = SFF\n\t";break;
      case 1:    s+="format      = Plain\n\t";break;
      case 2:    s+="format      = PCX\n\t";break;
      case 3:    s+="format      = DCX\n\t";break;
      case 4:    s+="format      = TIFF\n\t";break;
      case 5:    s+="format      = ASCII\n\t";break;
      case 6:    s+="format      = Extended Ansi\n\t";break;
      case 7:    s+="format      = Binary file transfer\n\t";break;
      default:   s+="format      = "+format+"\n\t";break;
      }
      s+="noofpages   = "+noofpages+"\n\t";
      s+="remoteno    = \""+remoteno+"\"\n\t";

      return s;
    }
  }

  static public class Modem extends NCPI{
    
    public int rate;
    public int protocol;
  
    public Modem(Rider r){
      try{
        rate     =r.readWord();
        protocol =r.readWord();
      }catch(IndexOutOfBoundsException ioobe){
        ioobe.printStackTrace();
      }
    }
  
    public String getProtocolDesc(){
      String s="";
      s+="protocol   = 0x"+Integer.toHexString(protocol)+"\n\t";
      if((protocol&(1<<0))!=0){s+="V.42/V.42 bis successfully negotiated.\n\t";}
      if((protocol&(1<<1))!=0){s+="MNP4/MNP5 successfully negotiated.\n\t";}
      if((protocol&(1<<2))!=0){s+="Transparent mode successfully negotiated.\n\t";}
      if((protocol&(1<<4))!=0){s+="Compression successfully negotiated.\n\t";}
      return s;
    }

    public String toString(){
      String s=super.toString();
      s+="rate   = "+rate+"\n\t";
      s+=getProtocolDesc();
      return s;
    }
  }

  static public NCPI create(int b3protocol,Rider r){
    switch(b3protocol){
    case CAPI_PROTOCOL_TRANSPARENT: return new Transparent();
    case CAPI_PROTOCOL_T90NL:       return new T90NL();
    case CAPI_PROTOCOL_ISO8208:     return new ISO8082(r);
    case CAPI_PROTOCOL_X25:         return new X25(r);
    case CAPI_PROTOCOL_T30_FAX:     return new T30Fax(r);
    case CAPI_PROTOCOL_T30_FAX_EXT: return new T30FaxExt(r);
    case CAPI_PROTOCOL_MODEM:       return new Modem(r);
    default:                        return new NCPI();
    }
  } 
}