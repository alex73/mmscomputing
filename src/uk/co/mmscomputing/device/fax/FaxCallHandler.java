package uk.co.mmscomputing.device.fax;

import java.io.*;

public interface FaxCallHandler{
  public void run(String local,String remote,InputStream pin,OutputStream pout);
}