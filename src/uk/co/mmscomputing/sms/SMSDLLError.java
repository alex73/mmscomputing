package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSDLLError extends SMSDataUnit{

  private int errcode;

  public SMSDLLError(){}
  public SMSDLLError(int err){
    errcode=err;
    put("DLL-Error",new Integer(errcode));
  }

  public int getErrorCode(){return errcode;}
  public void setErrorCode(int err){errcode=err;}

  public void readFrom(InputStream in)throws IOException{
    errcode=readOctet(in);
    put("DLL-Error",new Integer(errcode));
  }

  public void writeTo(OutputStream out)throws IOException{
    out.write(SMS_DLL_ERROR);
    out.write(errcode);
    out.flush();
  }

  public String toString(){
    String s=super.toString();
//    s+="Data Link Layer Error.\n";
    switch(errcode){

    case SMS_DLL_ERROR_CHECKSUM:    s+="Checksum is not correct.\n";break;
    case SMS_DLL_ERROR_LENGTH:      s+="Message length not correct.\n";break;
    case SMS_DLL_ERROR_TYPE:        s+="Unknown type.\n";break;
    case SMS_DLL_ERROR_EXTMECHANISM:s+="Extension mechanism not supported.\n";break;
    case SMS_DLL_ERROR_UNSPECIFIED: s+="Unspecified error cause.\n";break;

    default: s+="Unknown error code.\n";break;
    }
    return s;
  }
}