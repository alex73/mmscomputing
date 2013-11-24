package uk.co.mmscomputing.device.capi.exception;

import java.io.*;

/*

  super class to all capi exception classes

*/

public class CapiException extends IOException{

  public CapiException(String msg){
    super(msg);
  }
}
