package uk.co.mmscomputing.sms;

import java.io.IOException;

public interface SMSReceiver{
  public void received(SMSDataUnit msg)throws IOException;
}