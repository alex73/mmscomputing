#include <windows.h>

#include "..\uk_co_mmscomputing_device_capi_jcapi.h"

// cc	@cdk\bin\bcc32 -w-par -tWD -I"@cdk\include" -L"@cdk\lib;@cdk\lib\psdk" -n"@d" "@d\jcapi.cpp"

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getPtrSize(JNIEnv *, jclass){
  return sizeof(void*);
}

#define CAPI_DATA_B3	  0x86
#define CAPI_REQ        0x80
#define CAPI_IND        0x82

typedef DWORD (APIENTRY *pCAPI_REGISTER)(DWORD MessageBufferSize, DWORD maxLogicalConnection, DWORD maxBDataBlocks, DWORD maxBDataLen, DWORD *pApplID);
typedef DWORD (APIENTRY *pCAPI_RELEASE)(DWORD ApplID);
typedef DWORD (APIENTRY *pCAPI_PUT_MESSAGE)(DWORD ApplID, PVOID pCAPIMessage);
typedef DWORD (APIENTRY *pCAPI_GET_MESSAGE)(DWORD ApplID, PVOID *ppCAPIMessage);
typedef DWORD (APIENTRY *pCAPI_WAIT_FOR_SIGNAL)(DWORD ApplID);
typedef VOID  (APIENTRY *pCAPI_GET_MANUFACTURER)(PVOID SzBuffer);
typedef DWORD (APIENTRY *pCAPI_GET_VERSION)(DWORD *pCAPIMajor, DWORD *pCAPIMinor, DWORD *pManufacturerMajor, DWORD *pManufacturerMinor);
typedef DWORD (APIENTRY *pCAPI_GET_SERIAL_NUMBER)(PVOID SzBuffer);
typedef DWORD (APIENTRY *pCAPI_GET_PROFILE)(PVOID SzBuffer, DWORD CtrlNr);
typedef DWORD (APIENTRY *pCAPI_INSTALLED)(void);

pCAPI_REGISTER          CAPI_REGISTER=NULL;
pCAPI_RELEASE           CAPI_RELEASE=NULL;
pCAPI_PUT_MESSAGE       CAPI_PUT_MESSAGE=NULL;
pCAPI_GET_MESSAGE       CAPI_GET_MESSAGE=NULL;
pCAPI_WAIT_FOR_SIGNAL   CAPI_WAIT_FOR_SIGNAL=NULL;
pCAPI_GET_MANUFACTURER  CAPI_GET_MANUFACTURER=NULL;
pCAPI_GET_VERSION       CAPI_GET_VERSION=NULL;
pCAPI_GET_SERIAL_NUMBER CAPI_GET_SERIAL_NUMBER=NULL;
pCAPI_GET_PROFILE       CAPI_GET_PROFILE=NULL;
pCAPI_INSTALLED         CAPI_INSTALLED=NULL;

bool loadCapiLib(void){
  HINSTANCE  hLibDLL=LoadLibrary("CAPI2032.DLL");
  if( hLibDLL == NULL){ return false; }
  if(  (CAPI_REGISTER = (pCAPI_REGISTER)GetProcAddress(hLibDLL,"CAPI_REGISTER")) == NULL
  ||  (CAPI_RELEASE = (pCAPI_RELEASE)GetProcAddress(hLibDLL,"CAPI_RELEASE")) == NULL
  ||  (CAPI_PUT_MESSAGE = (pCAPI_PUT_MESSAGE)GetProcAddress(hLibDLL,"CAPI_PUT_MESSAGE")) == NULL
  ||  (CAPI_GET_MESSAGE = (pCAPI_GET_MESSAGE)GetProcAddress(hLibDLL,"CAPI_GET_MESSAGE")) == NULL
  ||  (CAPI_WAIT_FOR_SIGNAL = (pCAPI_WAIT_FOR_SIGNAL)GetProcAddress(hLibDLL,"CAPI_WAIT_FOR_SIGNAL")) == NULL
  ||  (CAPI_GET_MANUFACTURER = (pCAPI_GET_MANUFACTURER)GetProcAddress(hLibDLL,"CAPI_GET_MANUFACTURER")) == NULL
  ||  (CAPI_GET_VERSION = (pCAPI_GET_VERSION)GetProcAddress(hLibDLL,"CAPI_GET_VERSION")) == NULL
  ||  (CAPI_GET_SERIAL_NUMBER = (pCAPI_GET_SERIAL_NUMBER)GetProcAddress(hLibDLL,"CAPI_GET_SERIAL_NUMBER")) == NULL
  ||  (CAPI_GET_PROFILE = (pCAPI_GET_PROFILE)GetProcAddress(hLibDLL,"CAPI_GET_PROFILE")) == NULL
  ||  (CAPI_INSTALLED = (pCAPI_INSTALLED)GetProcAddress(hLibDLL,"CAPI_INSTALLED")) == NULL
  ){
    return false;
  }
  return true;
}

char CapiIOException[]       ="uk/co/mmscomputing/device/capi/exception/CapiIOException";
char CapiMsgFctException[]   ="uk/co/mmscomputing/device/capi/exception/CapiMsgFctException";
char CapiRegisterException[] ="uk/co/mmscomputing/device/capi/exception/CapiRegisterException";

int  ERRCannotThrowException = 1;

JNIEXPORT jobject JNU_NewObject(JNIEnv* env,jboolean* hasException,const char* classname,const char* descriptor,jint errno){
  jclass     clazz; 
  jmethodID  mid;
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
  jthrowable ioe = (jthrowable)JNU_NewObject(   // new CapiIOException(errno);
    env,hasException,exceptionClass,"(I)V",errno
  );
  if(ioe!=NULL){                                // if NULL some exception has already been thrown
    int err = env->Throw(ioe);
    if(err!=0){                                 // disaster, cannot tell java application something is wrong. Shutdown jvm.
      fprintf(stderr,"jcapi.cpp : Cannot throw %s(%d)\n\tError [%d]",exceptionClass,errno,err); 
      exit(ERRCannotThrowException);                   
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

// ----- end: executed when jvm loads library

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_checkInstalled(JNIEnv* env, jclass cl){
  jint errno=CAPI_INSTALLED();
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_register(JNIEnv* env, jclass cl, jint mlc, jint mbdb, jint mbdl){
  jboolean hasException=JNI_FALSE;
  DWORD appid=0;
  DWORD errno=CAPI_REGISTER(1024+1024*mlc,mlc, mbdb, mbdl,&appid);
  if(errno!=0){ JNU_ThrowCapiIOException(env,&hasException,CapiRegisterException,errno);}
  return (jint)appid;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_release(JNIEnv* env, jclass cl, jint appid){
  DWORD errno=CAPI_RELEASE(appid);
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}

// Next two procedures are only used in ..device.capi.ncc.DataB3Req class

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_acquireNative32bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf){                                             // Get a c-pointer to data buffer.
  return (jint)env->GetByteArrayElements(jdatabuf,NULL);              // We need to keep c-ptr valid until DataB3Conf
}

JNIEXPORT jlong JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_acquireNative64bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf){
  JNU_ThrowCapiIOException(env,-4);
  return 0;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_releaseNative32bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf,jint cdatabuf){                               // call when DataB3Conf received
  env->ReleaseByteArrayElements(jdatabuf,(jbyte*)cdatabuf,JNI_ABORT); // JNI_ABORT: don't need to copy back cdatabuf to jdatabuf
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_releaseNative64bitDataPtr(JNIEnv* env, jclass cl, 
    jbyteArray jdatabuf,jlong cdatabuf){
  JNU_ThrowCapiIOException(env,-4);
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_putMessage(JNIEnv* env, jclass cl, 
    jint appid, jbyteArray jbuf){
  if(jbuf==NULL){return;}                                             // No message buffer ? Error in program
  jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
  if(cbuf!=NULL){

    if((cbuf[4]==((jbyte)0x86))&&(cbuf[5]==((jbyte)0x80))             // DataB3Req
    && (env->GetArrayLength(jbuf)>30)                                 // via network
    ){
        cbuf[0]=30;cbuf[1]=0;
        jint* add=(jint*)(cbuf+12);
        add[0]=((jint)(cbuf+30));
    }

    jint errno = CAPI_PUT_MESSAGE(appid,(unsigned char*)cbuf);
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
  jint errno=CAPI_GET_MESSAGE(appid,(void**)&cbuf);
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
  DWORD errno = CAPI_WAIT_FOR_SIGNAL(appid);           //      no timeout possible with win xp
  if(errno!=0){ JNU_ThrowCapiIOException(env,errno);}
}

JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getManufacturer(JNIEnv* env, jclass cl, jint ctrl){
  char cbuf[64]={0};
  CAPI_GET_MANUFACTURER(cbuf);
  return env->NewStringUTF(cbuf);
}

JNIEXPORT jintArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getVersion(JNIEnv* env, jclass cl, jint ctrl){
  jboolean hasException=JNI_FALSE;
  unsigned long cbuf[4]={0};
  if(CAPI_GET_VERSION(&cbuf[0], &cbuf[1], &cbuf[2], &cbuf[3])!=0){
    JNU_ThrowCapiIOException(env,&hasException,CapiIOException,-2);
    return NULL;
  }
  jintArray jbuf=env->NewIntArray(4);
  if(jbuf==NULL){ return NULL;}        // out of memory exception already thrown
  env->SetIntArrayRegion(jbuf,0,4,(jint*)cbuf); 
  return jbuf;
}

JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getSerialNumber(JNIEnv* env, jclass cl, jint ctrl){
  jboolean hasException=JNI_FALSE;
  char cbuf[9]={0};
  if(CAPI_GET_SERIAL_NUMBER((char*)cbuf)!=0){
    JNU_ThrowCapiIOException(env,&hasException,CapiIOException,-3);
    return NULL;
  }
  cbuf[8]=0;
  return env->NewStringUTF(cbuf);
}

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_capi_jcapi_getProfile(JNIEnv* env, jclass cl, jint ctrl){
  jbyte cbuf[64]={0};
  DWORD errno=CAPI_GET_PROFILE((PVOID*)cbuf,ctrl);
  if(errno!=0){ 
    JNU_ThrowCapiIOException(env,errno);
    return NULL;
  }
  jbyteArray jbuf=env->NewByteArray(64);
  if(jbuf==NULL){ return NULL;}        // out of memory exception already thrown
  env->SetByteArrayRegion(jbuf,0,64,cbuf); 
  return jbuf;
}

int WINAPI DllEntryPoint(HINSTANCE hinst, unsigned long reason, void*){
  return 1;
}

/*
  [1] Sheng Liang (1999), The Java Native Interface, SUN, Palo Alto
*/
