/*

[1] SANE Standard Version 1.03
    (Scanner Access Now Easy)
    2002-10-10
    http://www.sane-project.org

[2] The Java Native Interface
    Sheng Liang
    1999-12
    Addison-Wesley
*/

// cc g++ -Wall -fPIC -pthread -D_REENTRANT -D_GNU_SOURCE -shared -o @d/libjsane.so @d/jsane.cpp @d/jdesc.cpp @d/jnu.cpp

// cc	g++ -g3 -gdwarf-2 -shared -m32 @d/jsane.cpp @d/jdesc.cpp @d/jnu.cpp -o @d/libjsane.so

//#include <sane/sane.h>                                // /usr/include/sane/sane.h
//#include <sane/saneopts.h>                            // /usr/include/sane/saneopts.h
#include "sane.h"                                       // use my own version here, due to dynamic loading
//#include <string.h>
#include <dlfcn.h>

#include "jnu.h"                                      // some JNI utilitiy functions
#include "../uk_co_mmscomputing_device_sane_jsane.h"  // uk/co/mmscomputing/device/sane/uk_...h

psane_init                   sane_init=NULL;
psane_exit                   sane_exit=NULL;
psane_get_devices            sane_get_devices=NULL;
psane_open                   sane_open=NULL;
psane_close                  sane_close=NULL;
psane_get_option_descriptor  sane_get_option_descriptor=NULL;
psane_control_option         sane_control_option=NULL;
psane_get_parameters         sane_get_parameters=NULL;
psane_start                  sane_start=NULL;
psane_read                   sane_read=NULL;
psane_cancel                 sane_cancel=NULL;
psane_set_io_mode            sane_set_io_mode=NULL;
psane_get_select_fd          sane_get_select_fd=NULL;
psane_strstatus              sane_strstatus=NULL;

bool loadLib(void){
  const char* dlError;

  void*  hLibDLL=dlopen("libsane.so",RTLD_NOW);
  if(hLibDLL==NULL){ 
    fprintf(stderr,"\nCould not load libsane.so.\n");
    dlError = dlerror();
    if(dlError!=NULL){fprintf(stderr,"Error: %s\n\n",dlError);}
    return false; 
  }
  if( (sane_init                  = (psane_init)dlsym(hLibDLL,"sane_init")) == NULL
  ||  (sane_exit                  = (psane_exit)dlsym(hLibDLL,"sane_exit")) == NULL
  ||  (sane_get_devices           = (psane_get_devices)dlsym(hLibDLL,"sane_get_devices")) == NULL
  ||  (sane_open                  = (psane_open)dlsym(hLibDLL,"sane_open")) == NULL
  ||  (sane_close                 = (psane_close)dlsym(hLibDLL,"sane_close")) == NULL
  ||  (sane_get_option_descriptor = (psane_get_option_descriptor)dlsym(hLibDLL,"sane_get_option_descriptor")) == NULL
  ||  (sane_control_option        = (psane_control_option)dlsym(hLibDLL,"sane_control_option")) == NULL
  ||  (sane_get_parameters        = (psane_get_parameters)dlsym(hLibDLL,"sane_get_parameters")) == NULL
  ||  (sane_start                 = (psane_start)dlsym(hLibDLL,"sane_start")) == NULL
  ||  (sane_read                  = (psane_read)dlsym(hLibDLL,"sane_read")) == NULL
  ||  (sane_cancel                = (psane_cancel)dlsym(hLibDLL,"sane_cancel")) == NULL
  ||  (sane_strstatus             = (psane_strstatus)dlsym(hLibDLL,"sane_strstatus")) == NULL
  ){
    fprintf(stderr,"\nCould not load libsane.so.\n");
    dlError = dlerror();
    if(dlError!=NULL){fprintf(stderr,"Error: %s\n\n",dlError);}
    return false;
  }
  return true;
}

static SANE_Int version=0;

jboolean busy=JNI_FALSE;                              // Applets: It is possible that two applet instances try to access same native library
jobject  syncObj=NULL;

jboolean setBusy(JNIEnv* env){

  if(syncObj==NULL){
    jstring jstr=env->NewStringUTF("mmsc");           // any object will do
    if(jstr==NULL){return JNI_FALSE;}
    syncObj=env->NewGlobalRef(jstr);
    env->DeleteLocalRef(jstr);
    if(syncObj==NULL){return JNI_FALSE;}
  }

  if(env->MonitorEnter(syncObj)!=JNI_OK){return JNI_FALSE;}

  jboolean success=JNI_FALSE;

  if(busy==JNI_FALSE){
    busy=JNI_TRUE;
    success=JNI_TRUE;
  }

  if(env->MonitorExit(syncObj)!=JNI_OK){return JNI_FALSE;}

  if(success==JNI_FALSE){
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to init sane\n\tAnother applet is using this library.\n");
  }
  return success;
}

/* ----- utility functions ----- */

jboolean checkStatus(JNIEnv* env,jboolean* hasException,jint status){
  if((*hasException)==JNI_FALSE){
    if(env->ExceptionCheck()==JNI_FALSE){
      if(status==SANE_STATUS_GOOD){ 
        return JNI_TRUE;
      }
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException",sane_strstatus((SANE_Status)status));
//      fprintf(stderr,"\njsane.cpp - STATUS Exception %s \n",sane_strstatus((SANE_Status)status));
    }
    (*hasException)=JNI_TRUE;
  }
  return JNI_FALSE;
}

/* ----- jsane.java --> jsane.c ----- */

void authCallback(SANE_String_Const resource,
				    SANE_Char username[SANE_MAX_USERNAME_LEN],
				    SANE_Char password[SANE_MAX_PASSWORD_LEN]){
  fprintf(stderr,"jsane.authCallback resource=%s. Not implemented.",resource);
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_init(JNIEnv* env,jclass cls
){
  if(setBusy(env)==JNI_TRUE){
    jboolean hasException=JNI_FALSE;
    try{
      int status=sane_init(&version,&authCallback);
      if(checkStatus(env,&hasException,status)){
        if(SANE_VERSION_MAJOR(version)!=SANE_CURRENT_MAJOR){
          try{
            sane_exit();
          }catch(...){
          }
          fprintf(stderr,"Failed to init SANE.\n    Due to SANE major version mismatch.\n");
          fprintf(stderr,"    This version of libjsane.so was build for SANE version [%i].\n",SANE_CURRENT_MAJOR);
          fprintf(stderr,"    But the installed SANE version is [%i].\n",SANE_VERSION_MAJOR(version));
          JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to init sane\n\tDue to SANE major version mismatch.\n");
        }
      }
    }catch(...){
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to init sane\n\tDue to exception in native code.\n");  
    }
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_exit(JNIEnv* env,jclass cls
){
  while(env->ExceptionCheck()){
    jthrowable exc = env->ExceptionOccurred();
    env->DeleteLocalRef(exc);
  }

  try{
    sane_exit();
  }catch(...){
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to exit sane\n\tDue to exception in native code.\n");
  }
  busy=JNI_FALSE;
}

JNIEXPORT jobjectArray JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getDevices(JNIEnv* env,jclass cls,
    jboolean local_only
){
  jboolean      hasException=JNI_FALSE;
  jobjectArray  array=NULL;
  int           size=0;

  try{
    const SANE_Device** devlist=NULL;
    SANE_Status status=sane_get_devices (&devlist,(local_only)?SANE_TRUE:SANE_FALSE);
    if(checkStatus(env,&hasException,status)){
      for(size=0;devlist[size]!=NULL;++size){}                        // devlist is a NULL terminated list
      jclass strcls=env->FindClass("java/lang/String");
      if(strcls!=NULL){
        array=(jobjectArray)env->NewObjectArray(size,strcls,NULL);
        if(array!=NULL){
          for(int i=0;i<size;++i){
            jstring str=env->NewStringUTF(devlist[i]->name);
            if(str==NULL){break;}
            env->SetObjectArrayElement(array,i,str);
          }
        }
      }
      env->DeleteLocalRef(strcls);
    }
  }catch(...){
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to get devices.\n\tDue to exception in native code.\n");
  }
  return array;                                    
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_open(JNIEnv* env,jclass cls,
    jstring jdevice
){
  if(env->ExceptionCheck()==JNI_FALSE){

    const char* device=env->GetStringUTFChars(jdevice,NULL);
    if(device!=NULL){
      jboolean    hasException=JNI_FALSE;
      SANE_Handle handle=0;
      jint        status=0;
      try{
//      fprintf(stderr,"DEVICE [%s] 0x%X",device,device[0]);              // zero length string bit fails [1] p25 4.3.4, need a device name
        status=sane_open(device,&handle);                                 // get sane device handle
//      fprintf(stderr,"DEVICE [%s] [%s]",device,sane_strstatus(status)); // With zero length device name, I get an Out OF MEMORY error
        if(!checkStatus(env,&hasException,status)){handle=0;}

      }catch(...){
        hasException=JNI_TRUE;
        JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to open device.\n\tDue to exception in native code.\n");
      }
      env->ReleaseStringUTFChars(jdevice,device);
      return (jint)handle;
    }
  }
  return 0;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_close(JNIEnv* env,jclass cls,
    jint handle
){
  try{
    sane_close((SANE_Handle)handle);
  }catch(...){
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to close device.\n\tDue to exception in native code.\n");
  }
}

/*
  Make sure jbuf is big enough. 
  Consult getOptionDescriptor(option).size

  [2] p35 3.3.3
*/

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getControlOption(JNIEnv* env,jclass cls,
    jint handle,jint option,jbyteArray jbuf
){
  if(env->ExceptionCheck()==JNI_FALSE){
    jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
    if(cbuf!=NULL){
      jboolean    hasException=JNI_FALSE;
      try{
        SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,cbuf,NULL);
        checkStatus(env,&hasException,status);
      }catch(...){
        hasException=JNI_TRUE;
        JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to get control option.\n\tDue to exception in native code.\n");
      }
      env->ReleaseByteArrayElements(jbuf,cbuf,0);
    }
  }
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setControlOption(JNIEnv* env,jclass cls,
    jint handle,jint option,jbyteArray jbuf
){
  if(env->ExceptionCheck()==JNI_FALSE){

    jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
    if(cbuf!=NULL){
      jboolean    hasException=JNI_FALSE;
      SANE_Int    info  =0;
      try{
        SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_SET_VALUE,cbuf,&info);
        if(!checkStatus(env,&hasException,status)){info=0;}
      }catch(...){
        hasException=JNI_TRUE;
        JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to set control option.\n\tDue to exception in native code.\n");
      }
      env->ReleaseByteArrayElements(jbuf,cbuf,0);
      return info;
    }
  }
  return 0;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setAutoControlOption(JNIEnv* env,jclass cls,
    jint handle,jint option
){
  if(env->ExceptionCheck()==JNI_FALSE){
    jboolean    hasException=JNI_FALSE;
    try{
      SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_SET_AUTO,NULL,NULL);
      checkStatus(env,&hasException,status);
    }catch(...){
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to set auto control option.\n\tDue to exception in native code.\n");
    }
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getParameters(JNIEnv* env,jclass cls,
    jint handle,jobject jparam
){
  if(env->ExceptionCheck()==JNI_FALSE){

    SANE_Parameters param={(SANE_Frame)0,0,0,0,0,0};    
//    memset(&param,0,sizeof(SANE_Parameters));
    try{
      SANE_Status     status=sane_get_parameters((SANE_Handle)handle,&param);
  
      jboolean hasException=JNI_FALSE;
      if(checkStatus(env,&hasException,status)){
        JNU_SetIntField(env,&hasException,jparam,"format",param.format);
        JNU_SetIntField(env,&hasException,jparam,"lastFrame",param.last_frame);
        JNU_SetIntField(env,&hasException,jparam,"lines",param.lines);
        JNU_SetIntField(env,&hasException,jparam,"depth",param.depth);
        JNU_SetIntField(env,&hasException,jparam,"pixelsPerLine",param.pixels_per_line);
        JNU_SetIntField(env,&hasException,jparam,"bytesPerLine",param.bytes_per_line);
      }
    }catch(...){
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to get image parameters.\n\tDue to exception in native code.\n");
    }
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_start(JNIEnv* env,jclass cls,
    jint handle
){
  if(env->ExceptionCheck()==JNI_FALSE){
    try{
      SANE_Status status=sane_start((SANE_Handle)handle);
      if(status == SANE_STATUS_NO_DOCS){                  // ADF scanner signal.
        JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneNoDocumentsException",sane_strstatus((SANE_Status)status));
      }else{
        jboolean hasException =JNI_FALSE;
        checkStatus(env,&hasException,status);
      }
    }catch(...){
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to start device.\n\tDue to exception in native code.\n");
    }
  }
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_read(JNIEnv* env,jclass cls,
    jint handle,jbyteArray jbuf, jint off, jint len
){
  if(env->ExceptionCheck()==JNI_FALSE){

//  java.io.InputStream :
//    Need to check for valid buffer and bounds

    if(jbuf==NULL){ 
      JNU_ThrowByName(env,"java/lang/NullPointerException","jsane.c:read: null pointer exception. jbuf = null");
      return -1;
    }
    int maxlen=env->GetArrayLength(jbuf);
    if((off<0)||(len<0)||(maxlen<(off+len))){
      JNU_ThrowByName(env,"java/lang/IndexOutOfBoundsException","jsane.c:read: index out of bounds");
      return -1;
    }
    jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
    if(cbuf!=NULL){
      try{
        SANE_Status status= sane_read((SANE_Handle)handle,(SANE_Byte*)&cbuf[off],len,&len);
        if(status!=SANE_STATUS_GOOD){
          len=-1;
          if(status==SANE_STATUS_EOF){
          }else if(status==SANE_STATUS_CANCELLED){
//            fprintf(stderr,"Sane.read cancelled.\n");
          }else{
//            fprintf(stderr,"Sane.read : %s\n",sane_strstatus(status));
            jboolean hasException=JNI_FALSE;
            checkStatus(env,&hasException,status);
          }
        }
      }catch(...){
        JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to read from device.\n\tDue to exception in native code.\n");
      }
      env->ReleaseByteArrayElements(jbuf,cbuf,0);
      return len;
    }
  }
  return -1;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_cancel(JNIEnv* env,jclass cls,
    jint handle
){
// [1] p.30 4.3.11
// Sends only a signal to backend. Cancellation completes when cancelled call (i.e. sane_read) returns

  try{
    sane_cancel((SANE_Handle)handle);
  }catch(...){
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","Failed to cancel scan.\n\tDue to exception in native code.\n");
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setIOMode(JNIEnv* env,jclass cls,
    jint handle, jboolean mode
){
  if(env->ExceptionCheck()==JNI_FALSE){
    SANE_Status status=sane_set_io_mode((SANE_Handle)handle,mode); 

    jboolean        hasException=JNI_FALSE;
    checkStatus(env,&hasException,status);
  }
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getSelectFD(JNIEnv* env,jclass cls,
    jint handle
){
  if(env->ExceptionCheck()==JNI_FALSE){
    SANE_Int    fd=0;
    SANE_Status status=sane_get_select_fd((SANE_Handle)handle,&fd);

    jboolean hasException=JNI_FALSE;
    if(checkStatus(env,&hasException,status)){
      return fd;
    }
  }
  return 0;
}

JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_sane_jsane_strStatus(JNIEnv* env,jclass cls,
    jint status
){
  if(env->ExceptionCheck()==JNI_FALSE){
    return env->NewStringUTF(sane_strstatus((SANE_Status)status));
  }
  return NULL;
}

// mmsc additions

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getVersion(JNIEnv* env, jclass cls
){
  return version;
}

/* ----- executed when jvm loads library  */

// ----- executed when jvm loads library

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* pjvm, void* reserved){
  if(loadLib()&& (sane_init(&version,&authCallback)==SANE_STATUS_GOOD)){
    sane_exit();
    if(SANE_VERSION_MAJOR(version)==SANE_CURRENT_MAJOR){
      return JNI_VERSION_1_4;	   // might work with lower versions; only tested with 1.4 though
    }
    fprintf(stderr,"FAILED TO LOAD libjsane.so\n    Due to SANE major version mismatch.\n");
    fprintf(stderr,"    This version of libjsane.so was build for SANE version [%i].\n",SANE_CURRENT_MAJOR);
    fprintf(stderr,"    But the installed SANE version is [%i].\n",SANE_VERSION_MAJOR(version));
  }
  return JNI_ERR;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* pjvm, void* reserved){
//  fprintf(stderr,"Unloaded java SANE wrapper.");
}
