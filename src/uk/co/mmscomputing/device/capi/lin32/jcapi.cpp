#include <dlfcn.h>
#include "../uk_co_mmscomputing_device_capi_jcapi.h"

// cc	g++ -Wall -fPIC -pthread -D_REENTRANT -D_GNU_SOURCE -shared @d/@f -o @d/libjcapi.so

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getPtrSize(JNIEnv *, jclass){
  return sizeof(void*);
}

JNIEXPORT int processMessage(void* msg){
 return 0;
}

typedef unsigned (*pcapi20_register)(unsigned MaxLogicalConnection,unsigned MaxBDataBlocks,unsigned MaxBDataLen,unsigned *ApplIDp);
typedef unsigned (*pcapi20_release)(unsigned ApplID);
typedef unsigned (*pcapi20_put_message)(unsigned ApplID, unsigned char *Msg);
typedef unsigned (*pcapi20_get_message)(unsigned ApplID, unsigned char **Buf);
typedef unsigned (*pcapi20_waitformessage)(unsigned ApplID, struct timeval *TimeOut);
typedef unsigned char* (*pcapi20_get_manufacturer)(unsigned Ctrl, unsigned char *Buf);
typedef unsigned char* (*pcapi20_get_version)(unsigned Ctrl, unsigned char *Buf);
typedef unsigned char* (*pcapi20_get_serial_number)(unsigned Ctrl, unsigned char *Buf);
typedef unsigned (*pcapi20_get_profile)(unsigned Controller, unsigned char *Buf);
typedef unsigned (*pcapi20_isinstalled)(void);

pcapi20_register          capi20_register=NULL;
pcapi20_release           capi20_release=NULL;
pcapi20_put_message       capi20_put_message=NULL;
pcapi20_get_message       capi20_get_message=NULL;
pcapi20_waitformessage    capi20_waitformessage=NULL;
pcapi20_get_manufacturer  capi20_get_manufacturer=NULL;
pcapi20_get_version       capi20_get_version=NULL;
pcapi20_get_serial_number capi20_get_serial_number=NULL;
pcapi20_get_profile       capi20_get_profile=NULL;
pcapi20_isinstalled       capi20_isinstalled=NULL;

bool loadCapiLib(void){
  const char* dlError;

  void*  hLibDLL=dlopen("libcapi20.so",RTLD_NOW/*RTLD_LAZY*/);
  if( hLibDLL == NULL){ 
    fprintf(stderr,"\nCould not load libcapi20.so.\n");
    dlError = dlerror();
    if(dlError!=NULL){fprintf(stderr,"Error: %s\n\n",dlError);}
    return false; 
  }
  if(  (capi20_register = (pcapi20_register)dlsym(hLibDLL,"capi20_register")) == NULL
  ||  (capi20_release = (pcapi20_release)dlsym(hLibDLL,"capi20_release")) == NULL
  ||  (capi20_put_message = (pcapi20_put_message)dlsym(hLibDLL,"capi20_put_message")) == NULL
  ||  (capi20_get_message = (pcapi20_get_message)dlsym(hLibDLL,"capi20_get_message")) == NULL
  ||  (capi20_waitformessage = (pcapi20_waitformessage)dlsym(hLibDLL,"capi20_waitformessage")) == NULL
  ||  (capi20_get_manufacturer = (pcapi20_get_manufacturer)dlsym(hLibDLL,"capi20_get_manufacturer")) == NULL
  ||  (capi20_get_version = (pcapi20_get_version)dlsym(hLibDLL,"capi20_get_version")) == NULL
  ||  (capi20_get_serial_number = (pcapi20_get_serial_number)dlsym(hLibDLL,"capi20_get_serial_number")) == NULL
  ||  (capi20_get_profile = (pcapi20_get_profile)dlsym(hLibDLL,"capi20_get_profile")) == NULL
  ||  (capi20_isinstalled = (pcapi20_isinstalled)dlsym(hLibDLL,"capi20_isinstalled")) == NULL
  ){
    fprintf(stderr,"\nCould not load libcapi20.so.\n");
    dlError = dlerror();
    if(dlError!=NULL){fprintf(stderr,"Error: %s\n\n",dlError);}
    return false;
  }
  return true;
}

char CapiIOException[]       ="uk/co/mmscomputing/device/capi/exception/CapiIOException";
char CapiMsgFctException[]   ="uk/co/mmscomputing/device/capi/exception/CapiMsgFctException";
char CapiRegisterException[] ="uk/co/mmscomputing/device/capi/exception/CapiRegisterException";

JNIEXPORT jobject JNU_NewObject(JNIEnv* env,jboolean* hasException,const char* classname,const char* descriptor,jint errno){
  jclass     clazz =NULL; 
  jmethodID  mid   =0;
  jobject    result=NULL;

  if((*hasException)==JNI_FALSE){
    if(env->EnsureLocalCapacity(2)==JNI_OK){
      clazz=env->FindClass(classname);
      if(clazz!=NULL){
        mid=env->GetMethodID(clazz,"<init>",descriptor);
        if(mid!=NULL){
          result=env->NewObject(clazz,mid,errno);
        }
      }
      env->DeleteLocalRef(clazz);
    }
    (*hasException)=env->ExceptionCheck();
    if((*hasException)==JNI_TRUE){
      env->DeleteLocalRef(result);
      result=NULL;
    }
  }
  return result;
}

JNIEXPORT void JNU_ThrowCapiIOException(JNIEnv* env, jboolean* hasException, char* exceptionClass, jint errno){
  if(env->ExceptionCheck()){return;}

  jthrowable ioe = (jthrowable)JNU_NewObject(   // new CapiIOException(errno);
    env,hasException,exceptionClass,"(I)V",errno
  );
  if(ioe!=NULL){                                // if NULL some exception has already been thrown
    int err = env->Throw(ioe);
    if(err!=0){                                 // disaster, cannot tell java application something is wrong.
      fprintf(stderr,"jcapi.cpp : Cannot throw %s(%d)\n\tError [%d]\n",exceptionClass,errno,err); 
    }
  }
  (*hasException)=JNI_TRUE;
}

JNIEXPORT void JNU_ThrowCapiIOException(JNIEnv* env, jint errno){
  jboolean hasException=JNI_FALSE;
  JNU_ThrowCapiIOException(env,&hasException,CapiMsgFctException,errno);
}

// ----- executed when jvm loads library

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* pjvm, void* /*reserved*/){
  if(loadCapiLib()){
    return JNI_VERSION_1_4;	// might work with lower versions; only tested with 1.4 though
  }
  return JNI_ERR;
}

// ----- java to cpp

/*
JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_capiGetErrMsg(JNIEnv* env, jclass cl, jint errno){
  char* msg=capi_info2str(errno);
  if(msg==NULL){ return env->NewStringUTF("No additional Information available.");}
  return env->NewStringUTF(msg);
}
*/
JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_checkInstalled(JNIEnv* env, jclass cl){
  jint errno=capi20_isinstalled();
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_register(JNIEnv* env, jclass cl, jint mlc, jint mbdb, jint mbdl){
  jboolean hasException=JNI_FALSE;
  jint appid=-1;
  jint errno=capi20_register(mlc, mbdb, mbdl,(unsigned int*)&appid);
  if(errno!=0){JNU_ThrowCapiIOException(env,&hasException,CapiRegisterException,errno);}
  return appid;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_release(JNIEnv* env, jclass cl, jint appid){
  jint errno=capi20_release(appid);
  if(errno!=0){JNU_ThrowCapiIOException(env,errno);}
}

// Next two procedures are only used in ..device.capi.ncc.DataB3Req class

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_acquireNative32bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf){
  return (jint)env->GetByteArrayElements(jdatabuf,NULL);              // need to keep c-ptr valid until DataB3Conf
}

JNIEXPORT jlong JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_acquireNative64bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf){
  JNU_ThrowCapiIOException(env,-4);
  return 0;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_releaseNative32bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf,jint cdatabuf){
  env->ReleaseByteArrayElements(jdatabuf,(jbyte*)cdatabuf,JNI_ABORT); // call when DataB3Conf received
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_releaseNative64bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf,jlong cdatabuf){
  JNU_ThrowCapiIOException(env,-4);
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_putMessage(JNIEnv* env, jclass cl, 
    jint appid, jbyteArray jbuf){
  if(jbuf==NULL){return;}                                             // No message ? Error in program
  jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
  if(cbuf!=NULL){
    
    if((cbuf[4]==((jbyte)0x86))&&(cbuf[5]==((jbyte)0x80))){           // DataB3Req
      jint* add=(jint*)(cbuf+12);                                     // pointer to data
      if(add[0]==0){                                                  // no data ptr => assume via network data after capi msg
        cbuf[0]=22;cbuf[1]=0;                                         // 32 bit set msg size to 22
        add[0]=((jint)(cbuf+30));                                     // set data pointer to byte after capi msg
      }
    }

    jint errno=capi20_put_message(appid,(unsigned char*)cbuf);
    env->ReleaseByteArrayElements(jbuf,cbuf,JNI_ABORT);               // JNI_ABORT: don't need to copy back
    if(errno!=0){JNU_ThrowCapiIOException(env,errno);}
  }
}

// Next procedure is only used in ..device.capi.ncc.DataB3Ind class

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_copyFromNative32bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jbuf,jint cbuf,jint clen){
  if((cbuf==0)||(clen==0)){return NULL;}

  if(jbuf==NULL){                                // If buffer is null or smaller than needed make new one
    jbuf=env->NewByteArray(clen);
  }else{
    jint jlen=env->GetArrayLength(jbuf);
    if(jlen<clen){
      jbuf=env->NewByteArray(clen);
    }
  }
  if(jbuf!=NULL){
    env->SetByteArrayRegion(jbuf,0,clen,(jbyte*)cbuf);
  }
  return jbuf;
}

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_copyFromNative64bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jbuf,jlong cbuf,jint clen){
  JNU_ThrowCapiIOException(env,-4);
  return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getMessage(JNIEnv* env, jclass cl, 
    jint appid,jbyteArray jbuf){

  unsigned char* cbuf=NULL;
  jint errno=capi20_get_message(appid,&cbuf);
  if(errno==0){
    jint clen=(((unsigned int)cbuf[0])&0x000000FF)|((((unsigned int)cbuf[1])&0x000000FF)<<8);

    if(jbuf==NULL){                              // If buffer is null or smaller than needed make new one
      jbuf=env->NewByteArray(clen);
    }else{
      jint jlen=env->GetArrayLength(jbuf);
      if(jlen<clen){
        jbuf=env->NewByteArray(clen);
      }
    }

    if(jbuf!=NULL){
      env->SetByteArrayRegion(jbuf,0,clen,(jbyte*)cbuf);
    }
    return jbuf;
  }else{
    JNU_ThrowCapiIOException(env,errno);
    return NULL;
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_waitForMessage(JNIEnv* env, jclass cl, jint appid){
  jint errno=capi20_waitformessage(appid,NULL);	           // no time out
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}

/*
JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_waitForMessage(JNIEnv* env, jclass cl, jint appid, jint millisecs){
  jint errno=0;
  if(millisecs<0){
    errno=capi20_waitformessage(appid,NULL);	             // no time out
  }else{
    timeval time={0};
    time.tv_sec  =(millisecs / 1000); 
    time.tv_usec =(millisecs % 1000); 
    errno=capi20_waitformessage(appid,&time);
  }
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}
*/
JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getManufacturer(JNIEnv* env, jclass cl, jint ctrl){
  jboolean hasException=JNI_FALSE;
  char cbuf[64]={0};
  if(capi20_get_manufacturer(ctrl,(unsigned char*)cbuf)==NULL){
    JNU_ThrowCapiIOException(env,&hasException,CapiIOException,-1);
    return NULL;
  }
  return env->NewStringUTF(cbuf);
}

JNIEXPORT jintArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getVersion(JNIEnv* env, jclass cl, jint ctrl){
  jboolean hasException=JNI_FALSE;
  int cbuf[4];
  if(capi20_get_version(ctrl,(unsigned char*)cbuf)==NULL){
    JNU_ThrowCapiIOException(env,&hasException,CapiIOException,-2);
    return NULL;
  }
  jintArray jbuf=env->NewIntArray(4);
  if(jbuf==NULL){ return NULL;}        // out of memory exception already thrown
  env->SetIntArrayRegion(jbuf,0,4,cbuf); 
  return jbuf;
}

JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getSerialNumber(JNIEnv* env, jclass cl, jint ctrl){
  jboolean hasException=JNI_FALSE;
  unsigned char cbuf[8]={0};
  if(capi20_get_serial_number(ctrl,cbuf)==NULL){
    JNU_ThrowCapiIOException(env,&hasException,CapiIOException,-3);
    return NULL;
  }
  cbuf[8]=0;
  return env->NewStringUTF((char*)cbuf);
}

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getProfile(JNIEnv* env, jclass cl, jint ctrl){
  jbyte cbuf[64]={0};
  jint errno=capi20_get_profile(ctrl,(unsigned char*)cbuf);
  if(errno!=0){ 
    JNU_ThrowCapiIOException(env,errno);
    return NULL;
  }
  jbyteArray jbuf=env->NewByteArray(64);
  if(jbuf==NULL){ return NULL;}        // out of memory exception already thrown
  env->SetByteArrayRegion(jbuf,0,64,cbuf); 
  return jbuf;
}

/*
  [1] Sheng Liang (1999), The Java Native Interface, SUN, Palo Alto
*/

