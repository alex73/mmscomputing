package uk.co.mmscomputing.sms;

import java.io.*;

public interface SMSDataUnitListener{
  public void send(SMSDataUnit unit)throws IOException;
  public void received(SMSDataUnit unit)throws IOException;
}