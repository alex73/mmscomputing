package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.util.JarLib;
import uk.co.mmscomputing.device.capi.exception.CapiRegisterException;
import uk.co.mmscomputing.device.capi.exception.CapiMsgFctException;

public class jcapi{

  static public  final  int ptrSize;
  static private native int getPtrSize();

// Capi

  static public  native void    checkInstalled()throws CapiMsgFctException;

// DataB3Req

  static public  native int     acquireNative32bitDataPtr(byte[] data);
  static public  native void    releaseNative32bitDataPtr(byte[] data,int cPtr);

  static public  native long    acquireNative64bitDataPtr(byte[] data);
  static public  native void    releaseNative64bitDataPtr(byte[] data,long cPtr);

// DataB3Ind

  static public  native byte[]  copyFromNative32bitDataPtr(byte[] data,int cPtr,int len);
  static public  native byte[]  copyFromNative64bitDataPtr(byte[] data,long cPtr,int len);

// Application

  static public  native int     register(int maxLogicalConnection,int maxBDataBlocks,int maxBDataLen)throws CapiRegisterException;
  static public  native void    release(int appid)throws CapiMsgFctException;

  static public  native void    putMessage(int appid,byte[] msg)throws CapiMsgFctException;
  static public  native byte[]  getMessage(int appid,byte[] msg)throws CapiMsgFctException;
  static public  native void    waitForMessage(int appid)throws CapiMsgFctException;

// Controller

  static public  native String  getManufacturer(int ctrl)throws CapiMsgFctException;
  static public  native int[]   getVersion(int ctrl)throws CapiMsgFctException;
  static public  native String  getSerialNumber(int ctrl)throws CapiMsgFctException;
  static public  native byte[]  getProfile(int ctrl)throws CapiMsgFctException;

  static public String getKernelDriverManufacturer()throws CapiMsgFctException{
    return getManufacturer(0);
  }

  static public int[] getKernelDriverVersion()throws CapiMsgFctException{
    return getVersion(0);
  }

  static public String getKernelDriverSerialNumber()throws CapiMsgFctException{
    return getSerialNumber(0);
  }

  static public int getNoOfControllers()throws CapiMsgFctException{
    byte[] buf=getProfile(0);		                  // 	if ctrl == 0 then get no of controllers
    return ((buf[1]&0x00FF)<<8)|(buf[0]&0x00FF);	//	little endian
  }

  static{
//  linux : load library 'libjcapi.so'
//  win : load library 'jcapi.dll'

    boolean loaded=JarLib.load(jcapi.class,"jcapi");
    ptrSize=(loaded)?getPtrSize():0;

    System.out.println(jcapi.class.getName()+": jcapi "+(ptrSize>>2)+" system loaded = "+loaded);
  }
}
