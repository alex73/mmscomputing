package uk.co.mmscomputing.device.phone;

import java.io.*;

public interface PhoneCallHandler{

  // pin and pout are expected to be 8kHz 16 bit mono pcm streams
  // static public final AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);

  public void run(String local,String remote,InputStream pin,OutputStream pout);
}