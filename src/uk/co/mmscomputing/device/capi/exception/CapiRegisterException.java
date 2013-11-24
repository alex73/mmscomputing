package uk.co.mmscomputing.device.capi.exception;

public class CapiRegisterException extends CapiIOException{

  // This exception will be thrown in jcapi.cpp only when we call CAPI_REGISTER

  public CapiRegisterException(int errno){
    super(errno);
  }
}