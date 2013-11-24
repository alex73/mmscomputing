package uk.co.mmscomputing.sms;

import java.io.*;

public class SMSException extends IOException implements SMSConstants{

  public SMSException(String msg){
    super(msg);
  }

  public static class Checksum extends SMSException{
    public Checksum(){
      super("Corrupt message: Checksum does not match.");
    }
  }

  public static class Length extends SMSException{
    public Length(){
      super("Corrupt message: Actual length and header length are not equal.");
    }
  }

  public static class Type extends SMSException{
    public Type(){
      super("Corrupt message: Unknown SMS data unit type.");
    }
  }

  public static class Unspecified extends SMSException{
    public Unspecified(String msg){
      super(msg);
    }
  }

}