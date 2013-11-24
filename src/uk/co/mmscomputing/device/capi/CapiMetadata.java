package uk.co.mmscomputing.device.capi;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.device.capi.protocol.*;
import uk.co.mmscomputing.device.capi.exception.*;

public class CapiMetadata extends Metadata implements CapiConstants, Serializable{

  public CapiMetadata(){
  }

  public CapiMetadata(Class main){
    super(main);
  }

  public void useALaw(){putInt(capiSpeechCodingID,LAYER1USERINFO_ALAW);}
  public void useuLaw(){putInt(capiSpeechCodingID,LAYER1USERINFO_uLAW);}

  public void use64kBit(){putInt("capi.bandwidth",KBIT64);}
  public void use56kBit(){putInt("capi.bandwidth",KBIT56);}

  public void useController(int no){putInt("capi.controller.id",no);}
  public void setController(int no){putInt("capi.controller.id",no);}
  public int  getController(){return getInt("capi.controller.id");}

  public void useMaxLogicalConnections(int max){putInt("capi.maxlogicalcon",max);}
  public void useB3DataBlocks(int max){putInt("capi.maxb3datablocks",max);}
  public void useB3DataBlockSize(int max){putInt("capi.maxb3datablocksize",max);}
  public void acceptAllCalls(){putInt("capi.cipmask",CIP_ACCEPTALL);}
  public void useLocalNo(String no){put("capi.localno",no);}

  public void useFaxHeader(String header){put("capi.faxheader",header);}

  public void setMaxIllegalLineCodings(int max){putInt("capi.fax.maxIllegalLineCodings",max);}
  public int  getMaxIllegalLineCodings(){return getInt("capi.fax.maxIllegalLineCodings",-1);}

  static abstract public class State{
    final  int line;

    public State(int line){this.line=line;}
    public int getLine(){return line;}
    public abstract String getName();
  }

  static public class Indication extends State{
    public  String     localno;
    public  String     remoteno;
    public  String     name;
    public  boolean    accept=false;
//    private int        speechcoding=-1;
    private int        cntl=-1;
    public  int        rejectFlag=0;
    public  int        waitformsecs=0;
    public  Semaphore  blocker=null;
    public  boolean    gotPickedUp=false;
    public  BProtocol  protocol=null;

    private CapiPlugin plugin=null;

    public Indication(int line){super(line);}
    public String getName(){return "INDICATION";};

    public boolean isAccepted(){return accept;}

    public void setProtocol(BProtocol protocol){this.protocol=protocol;}
    public BProtocol getProtocol(){return protocol;}

    public void setPlugin(CapiPlugin plugin){this.plugin=plugin;}
    CapiPlugin  getPlugin(){return plugin;}

//    public void setSpeechCoding(int sc){speechcoding=sc;}
//    public int  getSpeechCoding(){return speechcoding;}

    public void setController(int cntl){this.cntl=cntl;}
    public int  getController(){return cntl;}

    public void setAccept(int msecs,Semaphore b){
      accept=true;
      rejectFlag=ACCEPT;
      waitformsecs=msecs;  

      blocker=null;
      if(msecs>0){
        if(b!=null){
          blocker=b;
        }else{
          blocker=new Semaphore(0,true);
        }
      }
    }

    public void setAccept(int msecs){
      setAccept(msecs,null);
    }

    public void pickUp(){
      if(blocker!=null){
        gotPickedUp=true;
        blocker.release();
        blocker=null;
      }
    }

    public void setIgnore(){
      this.accept=false;
      rejectFlag=IGNORE;
    }
  }
}
