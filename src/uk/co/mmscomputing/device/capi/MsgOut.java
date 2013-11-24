package uk.co.mmscomputing.device.capi;

public class MsgOut extends StructOut implements CapiConstants{

  static private final int hdlen     =12;    // header length
  static private       int msgnumber =1;

                            // 0..1   len capi message
  public int appid;         // 2..3
  public int cmd;           // 4
  public int scmd;          // 5
  public int msgno;         // 6..7
  public int lineid;        // 8..11

  public MsgOut(int len){
    super(hdlen+len);
  }

  public MsgOut(int len,int appid, int cmd, int scmd, int lineid){
    super(hdlen+len);
    encode(len,appid,cmd,scmd,lineid);
  }

  public void encode(int len,int appid, int cmd, int scmd, int lineid){
    setIndex(0);
    writeWord(hdlen+len);                    //  capi message length including length word

    this.appid=appid;
    this.cmd=cmd;
    this.scmd=scmd;
    this.msgno=(msgnumber++)&0x0000FFFF;
    this.lineid=lineid;

    writeWord(appid);
    writeByte(cmd);
    writeByte(scmd);
    writeWord(msgno);
    writeDWord(lineid);
  }

  public String toString(){
    String s=getClass().getName()+"\n";
    s+="length = "+getIndex()+"\n";
    s+="appid  = "+appid+"\n";
    s+="cmd    = 0x"+Integer.toHexString(cmd)+"\n";
    s+="scmd   = 0x"+Integer.toHexString(scmd)+"\n";
    s+="msgno  = "+msgno+"\n";
    s+="lineid = 0x"+Integer.toHexString(lineid)+"\n";
    return s;
  }
}