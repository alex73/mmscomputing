#include <windows.h>
#include "twain.h"
#include "bmp.h"
#include "..\uk_co_mmscomputing_device_twain_jtwain.h"

// Windows XP 32 bit
// cc	@cdk\bin\bcc32 -w-par -tWD -I"@cdk\include" -L"@cdk\lib;@cdk\lib\psdk" -n"@d"  -e"jtwain.dll" "@d\*"

// Windows XP 64 bit
// SDK 2003: Targeting Windows Server 2003 Win XP 64 bit DEBUG
// cl /LD jtwain.cpp jnu.cpp bmp.cpp user32.lib bufferoverflowU.lib

// Windows Vista 64 bit
// SDK 2008: Targeting Windows Server 2008 x64 DEBUG
// cl /LD jtwain.cpp jnu.cpp bmp.cpp user32.lib

// TWAINDSM.DLL will not load the Data Source Manager if it cannot find {WindowsFolder}\twain_64 !

HWND           hWnd        =NULL;
TW_IDENTITY    appID       ={0};              // storage for App states

JavaVM*        gjvm        =NULL;             // cache virtual machine we are running in.
jclass         jtwain      =NULL;
jmethodID      jexecute    =NULL;

HINSTANCE      hLibDLL     = NULL;
DSMENTRYPROC   entry       = NULL;            // entry point to the DSM <  2.0

//-- Twain 2.0 Entry Points Start

TW_ENTRYPOINT entryPoints  = {0};             // entry point to the DSM >= 2.0
jmethodID     jcbmethod20  = NULL;
jobject       jdsm         = NULL;

TW_UINT16 FAR PASCAL DSM_Entry(pTW_IDENTITY origin,pTW_IDENTITY dest,TW_UINT32 DG,TW_UINT16 DAT,TW_UINT16 MSG,TW_MEMREF data){
  if(entryPoints.DSM_Entry!=NULL){            // twain >= 2.0
    return entryPoints.DSM_Entry(origin,dest,DG,DAT,MSG,data);
  }else{
    return entry(origin,dest,DG,DAT,MSG,data);
  }
}

TW_HANDLE FAR PASCAL DSM_Alloc(TW_UINT32 len){
  if(entryPoints.DSM_MemAllocate!=NULL){      // twain >= 2.0
    return entryPoints.DSM_MemAllocate(len);
  }else{
//    return GlobalAlloc(GPTR, len);
    return GlobalAlloc(GHND, len);
  }
}

void FAR PASCAL DSM_Free(TW_HANDLE ptr){
  if(entryPoints.DSM_MemFree!=NULL){          // twain >= 2.0
    entryPoints.DSM_MemFree(ptr);
  }else{
    GlobalFree(ptr);
  }
}

TW_MEMREF FAR PASCAL DSM_Lock(TW_HANDLE memory){
  if(entryPoints.DSM_MemLock!=NULL){          // twain >= 2.0
    return entryPoints.DSM_MemLock(memory);
  }else{
    return GlobalLock(memory);
  }
}

void FAR PASCAL DSM_Unlock(TW_MEMREF memory){
  if(entryPoints.DSM_MemUnlock!=NULL){        // twain >= 2.0
    return entryPoints.DSM_MemUnlock(memory);
  }else{
    GlobalUnlock(memory);
  }
}


bool DSM_IsTwain20(){
  return (appID.SupportedGroups&DF_DSM2)==DF_DSM2;
}
/*
JNIEXPORT jboolean JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_nDsmIsTwain20(JNIEnv* env, jclass clazz){
  return DSM_IsTwain20();
}
*/
//-- Twain 2.0 Entry Points End

bool loadTwainLib(JNIEnv* env){     
  if(hLibDLL!=NULL){return true;}

  int err;
  __try{
    hLibDLL = LoadLibrary("TWAINDSM.DLL");                                    // try to load new 2.0 DSM application folder
    if(hLibDLL == NULL){hLibDLL=LoadLibrary("TWAIN_32.DLL");}                 // if new twain dsm is not available load old pre 2.0 DSM
/*
    if(sizeof(void*)==4){                                                     // if 32bit
//      if(hLibDLL == NULL){ hLibDLL=LoadLibrary("uk\\co\\mmscomputing\\device\\twain\\win32\\TWAINDSM.DLL");}
    }else if(sizeof(void*)==8){                                               // if 64bit
//      if(hLibDLL == NULL){ hLibDLL=LoadLibrary("uk\\co\\mmscomputing\\device\\twain\\win64\\TWAINDSM.DLL");}
    }
*/
    if(hLibDLL==NULL){
      while(env->ExceptionCheck()){env->ExceptionClear();}
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException","Cannot load twaindsm.dll");
      return false;
    }
    entry = (DSMENTRYPROC)GetProcAddress(hLibDLL,"DSM_Entry");
    if(entry==NULL){
      while(env->ExceptionCheck()){env->ExceptionClear();}
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException","Cannot find twain's DSM_Entry function.");
      return false;
    }
    return true;                                        // twain state 1 -> 2
  }__except(err=GetExceptionCode(),EXCEPTION_EXECUTE_HANDLER){
          // source or source manager throws an exception somewhere but does not catch it before it returns.
          // Very bad: We cannot recover from this. Hence disable jtwain and ask user to restart application.
          // If we do not catch these exceptions the jvm would (crash) exit anyway.

    char str[512]={0};
    sprintf(str,"jtwain: EXCEPTION 0x%X thrown while loading twain subsystem.",err);
          //      MessageBox(NULL,str,"jtwain.loadTwainLib:",MB_OK);
    while(env->ExceptionCheck()){env->ExceptionClear();}
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
    return false;
  }
}

jobject openDataSourceManager(JNIEnv* env){
  if(!loadTwainLib(env)){return NULL;}
  if(appID.Id!=0){return NULL;}

  int err;
  __try{

//    memset(&appID, 0, sizeof(TW_IDENTITY));

    appID.Id = 0;                                       // init to 0, Source Manager will assign real value
    appID.Version.MajorNum = TWON_PROTOCOLMAJOR;
    appID.Version.MinorNum = TWON_PROTOCOLMINOR;
    appID.Version.Language = TWLG_USA;
    appID.Version.Country  = TWCY_USA;
    lstrcpy(appID.Version.Info, "2010-03-02");
    lstrcpy(appID.ProductName, "jtwain");

    appID.ProtocolMajor    = TWON_PROTOCOLMAJOR;
    appID.ProtocolMinor    = TWON_PROTOCOLMINOR;
    appID.SupportedGroups  = DF_APP2 | DG_IMAGE | DG_CONTROL;
    lstrcpy(appID.Manufacturer, "mm's computing");
    lstrcpy(appID.ProductFamily, "java twain wrapper");

    TW_UINT16 rc=DSM_Entry(&appID,NULL,DG_CONTROL,DAT_PARENT,MSG_OPENDSM,(TW_MEMREF)&hWnd);
    if(rc==TWRC_SUCCESS){                               // twain state 2 -> 3
      boolean hasException = false;
      jdsm=(jobject)JNU_NewObject(
          env,&hasException,
          "uk/co/mmscomputing/device/twain/TwainSourceManager",
          "(JZ)V",
          (jlong)hWnd,
          (jboolean)DSM_IsTwain20()
      );
      if(jdsm!=NULL){
        jdsm = (jobject)env->NewGlobalRef(jdsm);
      }
    }else{
      if(rc==TWRC_FAILURE){
        TW_STATUS status={0};
        rc =DSM_Entry(&appID,NULL,DG_CONTROL,DAT_STATUS,MSG_GET,&status);
        if(rc==TWRC_SUCCESS){
          char str[256]={0};
          sprintf(str,"Cannot open Data Source Manager: status = %d.\n",status.ConditionCode);
          JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
          return NULL;
        }
      }
      JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException","Cannot open Data Source Manager.");
      return NULL;
    }

    if((jdsm!=NULL)&&DSM_IsTwain20()){
//    fprintf(stderr,"\n***\nHave Twain 2.0 Data Source Manager\n***\n");

      entryPoints.Size=sizeof(entryPoints);
      rc =DSM_Entry(&appID,NULL,DG_CONTROL,DAT_ENTRYPOINT,MSG_GET,&entryPoints);
      if(rc==1){
        TW_STATUS status={0};
        rc =DSM_Entry(&appID,NULL,DG_CONTROL,DAT_STATUS,MSG_GET,&status);
        if(rc!=TWRC_SUCCESS){
          JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException","Cannot retrieve Twain 2.0 Entry Points and cannot retrieve status.");
          return NULL;
        }else{
          char str[256]={0};
          sprintf(str,"Cannot retrieve Twain 2.0 Entry Points: status = %d.\n",status.ConditionCode);
          JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
          return NULL;
        }
      }
    }
  }__except(err=GetExceptionCode(),EXCEPTION_EXECUTE_HANDLER){
    char str[512]={0};
    sprintf(str,"jtwain: EXCEPTION 0x%X thrown while loading twain datamanager.",err);
    //      MessageBox(NULL,str,"jtwain.openDataSourceManager:",MB_OK);
    while(env->ExceptionCheck()){env->ExceptionClear();}
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
    return NULL;
  }

  return jdsm;
}

jint callDSMEntry(JNIEnv* env,jbyte* csource,jint dg,jint dat,jint msg,jbyte* cbuf){
  if(jdsm==NULL){return -1;}
  int err;
  __try{
    int rc=DSM_Entry(&appID,(TW_IDENTITY*)csource,(TW_UINT32)dg,(TW_UINT16)dat,(TW_UINT16)msg,(TW_MEMREF)cbuf);
          // If the user reloads an applet while the select or acquire GUIs are still up
          // the applet throws ThreadDeath error. Happens only once per browser window 
          // when page has been loaded initially.
          // This is the windows thread. We do not want it to be killed!
    while(env->ExceptionCheck()){env->ExceptionClear();}
    return rc;
  }__except(err=GetExceptionCode(),EXCEPTION_EXECUTE_HANDLER){
          // source or source manager throws an exception somewhere but does not catch it before it returns.
          // Very bad: We cannot recover from this. Hence disable jtwain and ask user to restart application.
          // If we do not catch these exceptions the jvm would (crash) exit anyway.

    char str[512]={0};
    sprintf(str,"jtwain: EXCEPTION 0x%X thrown in twain source or source manager.\nThis may have left the twain subsystem in an unstable state.\nPlease restart application or web-browser.",err);
          //      MessageBox(NULL,str,"jtwain.callDSMEntry:",MB_OK);
    while(env->ExceptionCheck()){env->ExceptionClear();}
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
    return -1;
  }
//  JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException","jtwain: EXCEPTION thrown in twain source or source manager.\nThis may have left the twain subsystem in an unstable state.\nPlease restart application or web-browser.");
//  return -1;
}

JNIEnv* attachCurrentThread(){
  JNIEnv* env=NULL;gjvm->AttachCurrentThread((void**)&env,NULL);return env;
}

JNIEXPORT jobject JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ntransferImage(JNIEnv* env, jclass clazz, 
    jlong imageHandle
){
//fprintf(stderr,"ntransferImage: 0x%X\n",imageHandle);
  int err;
  __try{
    jboolean hasException=JNI_FALSE;
    return BMP_transferImage(env,clazz,&hasException,(HGLOBAL)imageHandle);
  }__except(err=GetExceptionCode(),EXCEPTION_EXECUTE_HANDLER){
    char str[512]={0};
    sprintf(str,"jtwain: EXCEPTION 0x%X thrown in twain source or source manager.\nThis may have left the twain subsystem in an unstable state.\nPlease restart application or web-browser.",err);
    while(env->ExceptionCheck()){env->ExceptionClear();}
    JNU_ThrowByName(env,"uk/co/mmscomputing/device/twain/TwainException",str);
    return NULL;
  }
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ntrigger(JNIEnv* env, jclass clazz,
    jobject caller, jint cmd
){
  PostMessage(hWnd,WM_USER,(WPARAM)cmd,(LPARAM)env->NewGlobalRef(caller));
}

LRESULT FAR PASCAL wndProc(HWND hWnd,UINT iMessage,WPARAM wParam,LPARAM lParam){

// fprintf(stderr,"wndProc iMessage: %d\n",iMessage);

  switch(iMessage){
  case WM_CLOSE:
  case WM_ENDSESSION:
  case WM_DESTROY:
    PostQuitMessage(0);
    DestroyWindow(hWnd);
    break;
  case WM_USER:{
    JNIEnv* env=attachCurrentThread();
    jobject obj=(jobject)lParam;
    env->CallStaticVoidMethod(jtwain,jexecute,obj,wParam);
    env->DeleteGlobalRef(obj);
    while(env->ExceptionCheck()){env->ExceptionClear();} // this runs in native thread, ALWAYS DO CLEAR Exceptions
   }break;
  default:
    return DefWindowProc (hWnd, iMessage, wParam, lParam);
  }
  return 0;
}

char szAppName[256]={0};

boolean createWindow(void){
  sprintf(szAppName,"JTWAIN_%d",GetTickCount());      // Because of applets: I.e. If two different applets use this library
  HINSTANCE hInstance=(HINSTANCE)NULL;
  WNDCLASS  wc={0};                                   // Set up application's main window
  wc.style         = NULL;                            // no style bits
  wc.lpfnWndProc   = (WNDPROC)wndProc;                // name of window proc
  wc.cbClsExtra    = 0;                               // no extra bits
  wc.cbWndExtra    = 0;
  wc.hInstance     = hInstance;
  wc.hIcon         = NULL;                            // load resources
  wc.hCursor       = NULL;                            // load mouse icon
  wc.hbrBackground = NULL;                            // use white backgnd
  wc.lpszMenuName  = NULL;                            // no menu
  wc.lpszClassName = szAppName;                       // class named

  if(!RegisterClass(&wc)){
    fprintf(stderr,"Failed to register window class \"%s\"\n",szAppName);
    return false;
  }
  hWnd = CreateWindow( szAppName,szAppName,WS_MAXIMIZE,CW_USEDEFAULT,CW_USEDEFAULT,CW_USEDEFAULT,CW_USEDEFAULT,NULL,NULL,NULL,NULL);

//  fprintf(stderr,"HWnd[%d] 0x%X\n",sizeof(hWnd),hWnd);

  return (hWnd!=NULL);
}

// ----- executed when jvm loads library

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* /*reserved*/){
  gjvm=jvm;
//  we'll load this when we really need it [2007-07-07]
//  if(!loadTwainLib(attachCurrentThread())){return JNI_ERR;}
  return JNI_VERSION_1_4;	                            // might work with lower versions; only tested with 1.4 though
}

// ----- executed when jvm removes library

JNIEXPORT void JNICALL JNI_OnUnLoad(JavaVM* pjvm, void* /*reserved*/){
  if(callDSMEntry(attachCurrentThread(),NULL,DG_CONTROL,DAT_PARENT,MSG_CLOSEDSM,(jbyte*)&hWnd)!=TWRC_SUCCESS){
    fprintf(stderr,"jtwain.cpp: [DG_CONTROL/DAT_PARENT/MSG_CLOSEDSM]: TWRC_FAILURE\n");
  }
}

JNIEXPORT jobject JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ngetSourceManager(JNIEnv* env, jclass clazz){
//  fprintf(stderr,"jtwain.cpp: getSourceManager 0x%X - 0x%X - 0x%X\n", env, clazz, jdsm);

  jtwain      = (jclass)env->NewGlobalRef(clazz);
  jexecute    = env->GetStaticMethodID(jtwain,"cbexecute","(Ljava/lang/Object;I)V");

//  random crashes if cached here
//  jcbmethod20 = env->GetStaticMethodID(jtwain,"cbmethod20","([B[BIIIJ)I");

  if((jexecute!=NULL) /*&& (jcbmethod20!=NULL)*/  &&  createWindow()){
    return openDataSourceManager(env);
  }
  return NULL;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_nstart(JNIEnv* env,jclass clazz){
  MSG       msg ={0};

  jmethodID cbhandleGetMessage =env->GetStaticMethodID(jtwain,"cbhandleGetMessage","(J)I");
  if(cbhandleGetMessage!=NULL){
    while(GetMessage((LPMSG)&msg, NULL, 0, 0)){
      jint rc=env->CallStaticIntMethod(jtwain,cbhandleGetMessage,&msg);
      while(env->ExceptionCheck()){env->ExceptionClear();} // this runs in native thread, ALWAYS DO CLEAR Exceptions
      if(rc!=TWRC_DSEVENT){                                // if event wasn't handled by source let windows handle it
        TranslateMessage((LPMSG)&msg);
        DispatchMessage((LPMSG)&msg);
      }
    }
  }
}

// java virtual machine -> native: call only within native thread !!

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ncallSourceManager(JNIEnv* env,jclass clazz, 
    jint dg, jint dat, jint msg, jbyteArray jbuf
){
  int rc=TWRC_FAILURE;
  if(jbuf==NULL){
    rc=callDSMEntry(env,NULL,dg,dat,msg,NULL);
  }else{
    jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
    if(cbuf!=NULL){
      if((dg==DG_CONTROL)&&(dat==DAT_IDENTITY)&&(msg==MSG_USERSELECT)){  // show user select dialog
        SetForegroundWindow(hWnd);
      }
      rc=callDSMEntry(env,NULL,dg,dat,msg,cbuf);
      env->ReleaseByteArrayElements(jbuf,cbuf,0);
    }
  }
  return rc;
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ncallSource(JNIEnv* env, jclass clazz, 
    jbyteArray jsource, jint dg, jint dat, jint msg, jbyteArray jbuf
){
  if(jsource==NULL){
    return Java_uk_co_mmscomputing_device_twain_jtwain_ncallSourceManager(env,clazz,dg,dat,msg,jbuf);
  }

  int      rc      = TWRC_FAILURE;
  jbyte*   csource = env->GetByteArrayElements(jsource,NULL);
  if(csource!=NULL){
    if(jbuf==NULL){
      rc=callDSMEntry(env,csource,dg,dat,msg,NULL);
    }else{
      jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
      if(cbuf!=NULL){
        if((dg==DG_CONTROL)&&(dat==DAT_USERINTERFACE)&&(msg==MSG_ENABLEDS)){  // show scan dialog
          SetForegroundWindow(hWnd);
        }
        rc=callDSMEntry(env,csource,dg,dat,msg,cbuf);
        env->ReleaseByteArrayElements(jbuf,cbuf,0);
      }
    }
    env->ReleaseByteArrayElements(jsource,csource,JNI_ABORT);  // JNI_ABORT: don't copy back, it's not necessary
  }
  return rc;
}

//-- container start

static int DCItemSize[]={
  sizeof(TW_INT8),sizeof(TW_INT16),sizeof(TW_INT32),
  sizeof(TW_UINT8),sizeof(TW_UINT16),sizeof(TW_UINT32),
  sizeof(TW_BOOL),sizeof(TW_FIX32),sizeof(TW_FRAME),
  sizeof(TW_STR32),sizeof(TW_STR64),sizeof(TW_STR128),sizeof(TW_STR255),
  sizeof(TW_STR1024),sizeof(TW_UNI512),
};

JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ngetContainer(JNIEnv* env, jclass clazz, 
    jbyteArray jcapbuf
){
  if(jcapbuf==NULL){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ngetContainer: Illegal argument (Null Pointer).");return NULL;}

  unsigned long len = env->GetArrayLength(jcapbuf);
  if(len!=sizeof(TW_CAPABILITY)){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ngetContainer: Illegal argument (Cap Array Length not sizeof(TW_CAPABILITY)).");return NULL;}

  TW_CAPABILITY* ccapbuf = (TW_CAPABILITY*)env->GetByteArrayElements(jcapbuf,NULL);
  if(ccapbuf==NULL){return NULL;}

  HANDLE cbuf = ccapbuf->hContainer;
  if(cbuf==0){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ngetContainer: Illegal argument (Container Handle is NULL).");return NULL;}

  jbyteArray jbuf=NULL;
  switch(ccapbuf->ConType){
  case TWON_ARRAY:{
    TW_ARRAY* array=(TW_ARRAY*)DSM_Lock((void*)cbuf);
    if(array!=NULL){
      int size;
      if(array->ItemType<=TWTY_UNI512){
        size=6+array->NumItems*DCItemSize[array->ItemType];
      }else{                                         // unknown item type
        size=6;array->NumItems=0;
      }
      jbuf=env->NewByteArray(size);
      if(jbuf!=NULL){
        env->SetByteArrayRegion(jbuf,0,size,(jbyte*)array);
      }
      DSM_Unlock((void*)cbuf);
    }
   }break;
  case TWON_ENUMERATION:{
    TW_ENUMERATION* enumeration=(TW_ENUMERATION*)DSM_Lock((void*)cbuf);
    if(enumeration!=NULL){
      int size;
      if(enumeration->ItemType<=TWTY_UNI512){
        size=14+enumeration->NumItems*DCItemSize[enumeration->ItemType];
      }else{                                         // unknown item type
        size=14;enumeration->NumItems=0;
      }
      jbuf=env->NewByteArray(size);
      if(jbuf!=NULL){
        env->SetByteArrayRegion(jbuf,0,size,(jbyte*)enumeration);
      }
      DSM_Unlock((void*)cbuf);
    }
   }break;
  case TWON_ONEVALUE:{                            // TW_ONEVALUE 6 bytes
    TW_ONEVALUE* onevalue=(TW_ONEVALUE*)DSM_Lock((void*)cbuf);
    if(onevalue!=NULL){
      jbuf=env->NewByteArray(sizeof(TW_ONEVALUE));
      if(jbuf!=NULL){
        env->SetByteArrayRegion(jbuf,0,sizeof(TW_ONEVALUE),(jbyte*)onevalue);
      }
      DSM_Unlock((void*)cbuf);
    }
   }break;
  case TWON_RANGE:{                               // TW_RANGE   22 bytes
    TW_RANGE* range=(TW_RANGE*)DSM_Lock((void*)cbuf);
    if(range!=NULL){
      jbuf=env->NewByteArray(sizeof(TW_RANGE));
      if(jbuf!=NULL){
        env->SetByteArrayRegion(jbuf,0,sizeof(TW_RANGE),(jbyte*)range);
      }
      DSM_Unlock((void*)cbuf);
    }
   }break;
  default:
    JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ngetContainer: Illegal argument (Unknown Container Type).");
    return NULL;    
  }
  DSM_Free((void*)cbuf);
  return jbuf;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_nsetContainer(JNIEnv* env, jclass clazz, 
  jbyteArray jcapbuf, jbyteArray jconbuf
){
  int len;

  if((jcapbuf==NULL)||(jconbuf==NULL)){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nsetCapability: Illegal argument (Null Pointer).");return;}

  TW_CAPABILITY* ccapbuf = (TW_CAPABILITY*)env->GetByteArrayElements(jcapbuf,NULL);
  if(ccapbuf==NULL){return;}

  len = env->GetArrayLength(jcapbuf);
  if(len!=sizeof(TW_CAPABILITY)){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nsetCapability: Illegal argument (Cap Array Length not sizeof(TW_CAPABILITY)).");return;}

  len = env->GetArrayLength(jconbuf);
  if(len==0){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nsetCapability: Illegal argument (Container Array Zero Length).");return;}

  HANDLE container  = DSM_Alloc(len);
  if(container==NULL){ JNU_ThrowByName(env,"java/lang/OutOfMemoryError","jtwain.cpp nsetCapability : Out of memory.");return;}

  jbyte* nbuf       = (jbyte*)DSM_Lock(container);
  if(nbuf==NULL){      JNU_ThrowByName(env,"java/lang/OutOfMemoryError","jtwain.cpp nsetCapability : Out of memory.");return;}

  jbyte* cbuf = env->GetByteArrayElements(jconbuf,NULL);                     // fprintf(stderr,"nsetContainer cbuf: 0x%X\n",cbuf);
  if(cbuf==NULL){DSM_Unlock(container);DSM_Free(container);return;}

  memcpy(nbuf,cbuf,len);
  DSM_Unlock(container);
  ccapbuf->hContainer = container;                                           // fprintf(stderr,"nsetContainer container: 0x%X\n",container);

  env->ReleaseByteArrayElements(jconbuf,cbuf,JNI_ABORT);                     // JNI_ABORT: don't copy back, it's not necessary
  env->ReleaseByteArrayElements(jcapbuf,(jbyte*)ccapbuf,0);                  // make sure the changes are copied to the java buffer !!!
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_nfreeContainer(JNIEnv* env, jclass clazz, 
    jbyteArray jcapbuf
){

//  fprintf(stderr, "Java_uk_co_mmscomputing_device_twain_jtwain_nfreeContainer\nnfreeContainer jcapbuf = 0x%X\n",jcapbuf);

  if(jcapbuf==NULL){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nfreeContainer: Illegal argument (Null Pointer).");return;}

  unsigned long len = env->GetArrayLength(jcapbuf);
  if(len!=sizeof(TW_CAPABILITY)){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nfreeContainer: Illegal argument (Cap Array Length not sizeof(TW_CAPABILITY)).");return;}

  TW_CAPABILITY* ccapbuf = (TW_CAPABILITY*)env->GetByteArrayElements(jcapbuf,NULL);
  if(ccapbuf==NULL){return;}

  HANDLE handle = ccapbuf->hContainer;                                       // fprintf(stderr,"nfreeContainer container: 0x%X\n",handle);
  if(handle==0){JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nfreeContainer: Illegal argument (Handle is NULL).");return;}
  DSM_Free(handle);
}

//-- container end
//-- memory transfer start

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_nnew(JNIEnv* env, jclass clazz,
    jbyteArray jbuf,jint len){

  if(jbuf==NULL){    
    JNU_ThrowByName(env,"java/lang/NullPointerException","jtwain.cpp: nnew: jbuf is NULL.");
    return;
  }
  if(env->GetArrayLength(jbuf)!=(jint)sizeof(TW_IMAGEMEMXFER)){    
    JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nnew: Illegal argument (jbuf is the wrong size).");
    return;
  }
  jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
  if(cbuf!=NULL){
    HANDLE buffer = DSM_Alloc(len);
    if(buffer!=NULL){
      TW_IMAGEMEMXFER* imx=(TW_IMAGEMEMXFER*)cbuf;
      imx->Memory.Flags  = TWMF_APPOWNS | TWMF_HANDLE;
      imx->Memory.Length = len;
      imx->Memory.TheMem = buffer;
    }else{
      JNU_ThrowByName(env,"java/lang/OutOfMemoryError","jtwain.cpp [new] : Out of memory.");
    }
  }
  env->ReleaseByteArrayElements(jbuf,cbuf,0);
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ncopy(JNIEnv* env, jclass clazz, 
    jbyteArray jbuf, jbyteArray jimx, jint size){

  if((jbuf==NULL)||(jimx==NULL)){ 
    JNU_ThrowByName(env,"java/lang/NullPointerException","jtwain.cpp: ncopy: null pointer exception.");return -1;
  }
  int len=env->GetArrayLength(jbuf);                    // assume: len does match SETUPMEMXFER size!
  if((len==0)||(len<size)||(env->GetArrayLength(jimx)!=(jint)sizeof(TW_IMAGEMEMXFER))){    
    JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ncopy: Illegal argument.");return -1;
  }
  len=-1;
  TW_IMAGEMEMXFER* cimx=(TW_IMAGEMEMXFER*)env->GetByteArrayElements(jimx,NULL);
  if(cimx!=NULL){
    HANDLE handle = (HANDLE)cimx->Memory.TheMem;
    if(handle==NULL){
      JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: ncopy: Illegal argument.");
    }else{
      jbyte* cbuf=(jbyte*)DSM_Lock(handle);
      if(cbuf==NULL){
        JNU_ThrowByName(env,"java/lang/OutOfMemoryError","jtwain.cpp: ncopy: Cannot lock handle.");
      }else{
        env->SetByteArrayRegion(jbuf,0,size,cbuf);
        DSM_Unlock(handle);
        len=size;
      }
    }
  }
  env->ReleaseByteArrayElements(jimx,(jbyte*)cimx,0);
  return len;
}
/*
JNIEXPORT jbyteArray JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ncopy(JNIEnv* env, jclass clazz,
    jbyteArray jbuf,jint len){

  jbyteArray jmembuf=NULL;

  if(jbuf==NULL)){    
    JNU_ThrowByName(env,"java/lang/NullPointerException","jtwain.cpp: nnew: jbuf is NULL.");
    return;
  }
  if(env->GetArrayLength(jbuf)!=(jint)sizeof(TW_IMAGEMEMXFER)){    
    JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nnew: Illegal argument (jbuf is the wrong size).");
    return;
  }

  if(len>0){    
    jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
    if(cbuf!=NULL){
      TW_IMAGEMEMXFER* imx    = (TW_IMAGEMEMXFER*)cbuf;
      HANDLE           handle = (HANDLE)imx->Memory.TheMem;
      if(handle!=NULL){
        jbyte* cmembuf=(jbyte*)DSM_Lock(handle);
        if(cmembuf!=NULL){
          jmembuf=env->NewByteArray(len);
          if(jmembuf!=NULL){
            env->SetByteArrayRegion(jmembuf,0,len,cmembuf);
          }
          DSM_Unlock(handle);
        }
      }
      env->ReleaseByteArrayElements(jbuf,cbuf,0);
    }    
  }
  return jmembuf;
}
*/
JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ndelete(JNIEnv* env, jclass clazz,
    jbyteArray jbuf){
  
  if(jbuf==NULL){    
    JNU_ThrowByName(env,"java/lang/NullPointerException","jtwain.cpp: nnew: jbuf is NULL.");
    return;
  }
  if(env->GetArrayLength(jbuf)!=(jint)sizeof(TW_IMAGEMEMXFER)){    
    JNU_ThrowByName(env,"java/lang/IllegalArgumentException","jtwain.cpp: nnew: Illegal argument (jbuf is the wrong size).");
    return;
  }
  jbyte* cbuf=env->GetByteArrayElements(jbuf,NULL);
  if(cbuf!=NULL){
    TW_IMAGEMEMXFER* imx    = (TW_IMAGEMEMXFER*)cbuf;
    HANDLE           handle = (HANDLE)imx->Memory.TheMem;
    if(handle!=NULL){
      DSM_Free((void*)handle);
      imx->Memory.TheMem=NULL;
    }
  }    
  env->ReleaseByteArrayElements(jbuf,cbuf,0);
}

//-- memory transfer end

//-- Twain 2.0 Callback Start

/*
 * Class:     uk_co_mmscomputing_device_twain_jtwain
 * Method:    cbmethod20
 * Signature: ([B[BIIIJ)I
 */

// Called by twain 2.0 source if it has something to tell us!

TW_UINT16 FAR PASCAL DSM_Callback(pTW_IDENTITY corigin,pTW_IDENTITY cdest,TW_UINT32 DG,TW_UINT16 DAT,TW_UINT16 MSG,TW_MEMREF cdata){
  TW_UINT16 rc = TWRC_FAILURE;

  if(corigin==NULL){fprintf(stderr,"jtwain: DSM_Callback\n\tCannot call twain 2.0 dsm callback method!\n\tcorigin==NULL\n");return rc;}
  if(cdest  ==NULL){fprintf(stderr,"jtwain: DSM_Callback\n\tCannot call twain 2.0 dsm callback method!\n\tcdest==NULL\n");return rc;}

  JNIEnv* env=attachCurrentThread();

  jbyteArray jorigin = env->NewByteArray(sizeof(TW_IDENTITY));
  if(jorigin!=NULL){
    env->SetByteArrayRegion(jorigin,0,sizeof(TW_IDENTITY),(jbyte*)corigin);

    jbyteArray jdest = env->NewByteArray(sizeof(TW_IDENTITY));
    if(jdest!=NULL){
      env->SetByteArrayRegion(jdest,0,sizeof(TW_IDENTITY),(jbyte*)cdest);

      jcbmethod20 = env->GetStaticMethodID(jtwain,"cbmethod20","([B[BIIIJ)I");
      if(jcbmethod20!=NULL){
        rc = env->CallStaticIntMethod(jtwain,jcbmethod20,jorigin,jdest,DG,DAT,MSG,(jlong)cdata);
      }else{
        fprintf(stderr,"jtwain: DSM_Callback\n\tCannot call twain 2.0 dsm callback method!\n\tcbmethod20==NULL\n");
      }
      env->DeleteLocalRef(jdest);
    }
    env->DeleteLocalRef(jorigin);
  }
  while(env->ExceptionCheck()){env->ExceptionClear();}

//  fprintf(stderr,"jtwain: DSM_Callback    rc = %d",rc);

  return rc;
}

JNIEXPORT jlong JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ngetCallBackMethod(JNIEnv* env, jclass clazz
){
  TW_MEMREF callback = (TW_MEMREF)DSM_Callback;    
  return (jlong)callback;
}

//-- Twain 2.0 Callback  End

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_twain_jtwain_ngetPtrSize(JNIEnv* env, jclass clazz
){
  return sizeof(void*);
}

int WINAPI DllEntryPoint(HINSTANCE hInstance, unsigned long reason, void*){
  return 1;
}

//      if((dg==DG_IMAGE)&&(dat==DAT_IMAGEMEMXFER)&&(msg==MSG_GET)){cbuf=NULL;return cbuf[0];}
/*
  char str[256]={0};
  sprintf(str,"END MESSAGELOOP 0x%X",hWnd);
  MessageBox(NULL,str,"jtwain",MB_OK);
*/
