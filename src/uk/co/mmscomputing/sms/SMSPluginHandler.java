package uk.co.mmscomputing.sms;

import java.io.*;

public interface SMSPluginHandler{
  public void run(String local,String remote,InputStream pin,OutputStream pout);
}

