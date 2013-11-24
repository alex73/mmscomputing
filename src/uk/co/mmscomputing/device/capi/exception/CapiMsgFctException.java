package uk.co.mmscomputing.device.capi.exception;

public class CapiMsgFctException extends CapiIOException{

  // This exception will be thrown in jcapi.cpp except when dealing with CAPI_REGISTER

  public CapiMsgFctException(int errno){
    super(errno);
  }
}