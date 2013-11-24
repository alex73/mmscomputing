package uk.co.mmscomputing.device.sane;

public class SaneNoDocumentsException extends SaneIOException{
  public SaneNoDocumentsException(String msg){ // Need this. JNI wouldn't find IOException constructor.
    super(msg);
  }
}