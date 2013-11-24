#include "jnu.h"
#include "util.h"
#include "monitor.h"

static char jprintmonitor[]   = "uk/co/mmscomputing/device/printmonitor/jprintmonitor";

bool Monitor_Init(LPWSTR registryKey){
//  Util_showMessage(L"Monitor_Init: registryKey = %s",registryKey);
//  HKU\System\CurrentControlSet\Control\Print\Monitors\jprintmonitor
//  HKEY key;
//  if(RegOpenKeyExW(HKEY_LOCAL_MACHINE,registryKey,0,KEY_QUERY_VALUE,&key)!=ERROR_SUCCESS){return false;}
//  RegCloseKey(key);
  return true;
}

BOOL WINAPI Monitor_OpenPort(LPWSTR portname,PHANDLE porthandle){
	(*porthandle)=NULL;

//  Util_showMessage(L"Open Port: portname = %s",portname);

  if(portname==NULL){return false;}

  jstring jportname = NULL;
  jobject port      = NULL;

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);
  if(env->ExceptionCheck()){env->ExceptionClear();}
  jclass clazz = env->FindClass(jprintmonitor);
  if(clazz==NULL){goto cleanup;}
  jportname=env->NewString((jchar*)portname,(jsize)wcslen(portname));
  if(jportname==NULL){goto cleanup;}
  port=JNU_CallStaticMethodByName(env,NULL,clazz,"openPort","(Ljava/lang/String;)Luk/co/mmscomputing/device/printmonitor/Port;",jportname).l;
  port=env->NewGlobalRef(port);                                       // need global reference
cleanup:
  if(env->ExceptionCheck()){env->ExceptionClear();}
  env->DeleteLocalRef(clazz);
  if(jportname!=NULL){env->ReleaseStringChars(jportname,(jchar*)portname);}
	(*porthandle)=(HANDLE)port;                                         // need this handle for startdoc,write,read
  return port!=NULL;
}

BOOL WINAPI Monitor_OpenPortEx(LPWSTR pszPortName,LPWSTR pszPrinterName,LPHANDLE pHandle,LPMONITOR pMonitor){
  Util_showMessage(L"Open Port Ex: portname = %s  printer = %s",pszPortName,pszPrinterName);
  return true;
}

BOOL WINAPI Monitor_StartDocPort(HANDLE port,LPWSTR printername,DWORD job,DWORD level,LPBYTE docInfo){
//  Util_showMessage(L"Start Document Port: printername = %s",printername);

  if(port==NULL){return false;}

  WCHAR* docname;
	switch(level){
	case 1: docname=((DOC_INFO_1W*)docInfo)->pDocName;break;
	case 2: docname=((DOC_INFO_2W*)docInfo)->pDocName;break;
  default: SetLastError(ERROR_INVALID_LEVEL);return false;
	}

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);
  if(env->ExceptionCheck()){env->ExceptionClear();}

  jstring jdocname     = NULL;

  jboolean res=false;
  jstring jprintername=env->NewString((jchar*)printername,(jsize)wcslen(printername));
  if(jprintername==NULL){goto cleanup;}
  jdocname=env->NewString((jchar*)docname,(jsize)wcslen(docname));
  if(jdocname==NULL){goto cleanup;}
  res=JNU_CallMethodByName(env,NULL,(jobject)port,"start","(Ljava/lang/String;ILjava/lang/String;)Z",jprintername,job,jdocname).z;
cleanup:
  if(env->ExceptionCheck()){env->ExceptionClear();}
  if((jprintername!=NULL)&&(printername!=NULL)){env->ReleaseStringChars(jprintername,(jchar*)printername);}
  if((jdocname!=NULL)&&(docname!=NULL))        {env->ReleaseStringChars(jdocname,(jchar*)docname);}
  return res;
}

BOOL WINAPI Monitor_WritePort(HANDLE port,LPBYTE cbuf,DWORD len,LPDWORD written){
  (*written)=0;
  if(port==NULL){return false;}

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);

  jint         job;
  jint         res=-1;
  jobject      jprintername=NULL;
  const jchar* printername=NULL;
  jbyteArray   jbuf=env->NewByteArray(len);
  if(jbuf==NULL){goto cleanup;}
  env->SetByteArrayRegion(jbuf,0,len,(jbyte*)cbuf);
  if(env->ExceptionCheck()){goto cleanup;}
  res=JNU_CallMethodByName(env,NULL,(jobject)port,"write","([B)I",jbuf,len).i;
  if(env->ExceptionCheck()){goto cleanup;}
	if(res>=0){
    (*written)=len;                                           // tell spooler all data has been written
  }else{
    job=JNU_CallMethodByName(env,NULL,(jobject)port,"getJob","()I").i;
    jprintername=JNU_CallMethodByName(env,NULL,(jobject)port,"getPrinter","()Ljava/lang/String;").l;
    if(env->ExceptionCheck()){goto cleanup;}                   
    printername=env->GetStringChars((jstring)jprintername,NULL);
    if(env->ExceptionCheck()){goto cleanup;}                   

    HANDLE printer;
		if(OpenPrinterW((WCHAR*)printername,&printer,NULL)){
		  SetJob(printer,job,0,NULL,JOB_CONTROL_RESTART);
			SetJob(printer,job,0,NULL,JOB_CONTROL_PAUSE);
		  CloseHandle(printer);
		}
	}
cleanup:
  env->ExceptionClear();
  env->DeleteLocalRef(jbuf);
  if((jprintername!=NULL)&&(printername!=NULL)){env->ReleaseStringChars((jstring)jprintername,printername);}
  return (res>=0);
}

BOOL WINAPI Monitor_ReadPort(HANDLE hPort,LPBYTE pBuffer,DWORD cbBuf,LPDWORD pcbRead){
  (*pcbRead)=0;
	return true;
}

BOOL WINAPI Monitor_EndDocPort(HANDLE port){
  if(port==NULL){return false;}

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);

  jboolean     res=false;
  const jchar* printername=NULL;
  jint         job=JNU_CallMethodByName(env,NULL,(jobject)port,"getJob","()I").i;
  jobject      jprintername=JNU_CallMethodByName(env,NULL,(jobject)port,"getPrinter","()Ljava/lang/String;").l;
  if(env->ExceptionCheck()){goto cleanup;}                   
  printername=env->GetStringChars((jstring)jprintername,NULL);
  if(env->ExceptionCheck()){goto cleanup;}                   

  HANDLE printer;
	if(OpenPrinterW((WCHAR*)printername,&printer,NULL)){             // Tell spooler/printer that we are done.
	  SetJob(printer,job,0,NULL,JOB_CONTROL_DELETE);
		CloseHandle(printer);
	}
  res=JNU_CallMethodByName(env,NULL,(jobject)port,"end","()Z").z;
cleanup:
  if(env->ExceptionCheck()){env->ExceptionClear();}
  if((jprintername!=NULL)&&(printername!=NULL)){env->ReleaseStringChars((jstring)jprintername,printername);}
  return res;
}

BOOL WINAPI Monitor_ClosePort(HANDLE port){
  if(port==NULL){return false;}

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);

  jboolean res=JNU_CallMethodByName(env,NULL,(jobject)port,"close","()Z").z;
  if(env->ExceptionCheck()){env->ExceptionClear();}
  env->DeleteGlobalRef((jobject)port);                       // release global reference, object can now be garbage collected
  return res;
}

BOOL WINAPI Monitor_GetPrinterDataFromPort(HANDLE hPort,DWORD ControlID,LPWSTR pValueName,LPWSTR lpInBuffer,
    DWORD cbInBuffer,LPWSTR lpOutBuffer,DWORD cbOutBuffer,LPDWORD lpcbReturned){
  MessageBoxW(NULL,L"Monitor_GetPrinterDataFromPort",L"jprintmonitor",MB_OK);
  return false;
}

BOOL WINAPI Monitor_SetPortTimeOuts(HANDLE hPort,LPCOMMTIMEOUTS lpCTO,DWORD reserved){
  MessageBoxW(NULL,L"Monitor_SetPortTimeOuts",L"jprintmonitor",MB_OK);
  return false;
}

// Printer Properties Port Tab: enum, add, configure and delete ports

BOOL WINAPI Monitor_EnumPorts(LPWSTR servername,DWORD dwLevel,LPBYTE pPorts,DWORD cbBuf,LPDWORD pdwNeeded,LPDWORD numberOfPorts){
  (*numberOfPorts)=0;                                            // seems that we need to set this even if cbBuf < (*pdwNeeded)

  if((dwLevel!=1)&&(dwLevel!=2)){                                // support only level 1 and 2
    Util_showMessage(L"Enum Port: Level = %d",dwLevel);
    SetLastError(ERROR_INVALID_LEVEL);return false;
  }

  JNIEnv* env      = NULL;                                       // get java environment
  jvm->AttachCurrentThread((void**)&env,NULL);
  if(env->ExceptionCheck()){env->ExceptionClear();}

  bool       res   = false;                         
  jclass     clazz = env->FindClass(jprintmonitor);              // get jprintmonitor class
  jbyteArray jbuf  = NULL;
  if(clazz==NULL){goto cleanup;}

  (*pdwNeeded)=JNU_CallStaticMethodByName(env,NULL,clazz,"getPortInfoSize","(I)I",dwLevel).i;
  if(env->ExceptionCheck()){goto cleanup;}                   

  if(((*pdwNeeded)<=cbBuf)&&(pPorts!=NULL)){                     // if spooler offers enough buffer space
    jbuf  = env->NewByteArray(cbBuf);                            // get java byte buffer
    if(jbuf==NULL){goto cleanup;}                                // OutOfMemoryError
                                                                 // call java enum ports; prepare Port_Info buffer  
                                                                 // tell spooler how many ports we have
    (*numberOfPorts)=JNU_CallStaticMethodByName(env,NULL,clazz,"enumPorts","(Ljava/lang/String;II[B)I",NULL,dwLevel,pPorts,jbuf).i;
    if(env->ExceptionCheck()){goto cleanup;}
    env->GetByteArrayRegion(jbuf,0,cbBuf,(jbyte*)pPorts);        // copy java array to c array
    if(env->ExceptionCheck()){goto cleanup;}                     // ArrayIndexOutOfBoundsException
    res=true;
  }else{
    SetLastError(ERROR_INSUFFICIENT_BUFFER);
  }
cleanup:
  if(env->ExceptionCheck()){env->ExceptionClear();}
  env->DeleteLocalRef(jbuf);
  env->DeleteLocalRef(clazz);
  return res;
}

BOOL WINAPI Monitor_XXXPort(char* procname,LPWSTR servername,HWND hWnd,LPWSTR name){
  if(name==NULL){return false;}

  JNIEnv* env=NULL;
  jvm->AttachCurrentThread((void**)&env,NULL);
  if(env->ExceptionCheck()){env->ExceptionClear();}
  jboolean   res         = false;
  jstring    jname       = NULL;
  jstring    jservername = NULL;
  jclass     clazz = env->FindClass(jprintmonitor);
  if(clazz==NULL){goto cleanup;}
  if(servername!=NULL){
    jservername=env->NewString((jchar*)servername,(jsize)wcslen(servername));
    if(jservername==NULL){goto cleanup;}
  }
  jname=env->NewString((jchar*)name,(jsize)wcslen(name));
  if(jname==NULL){goto cleanup;}
  res=JNU_CallStaticMethodByName(env,NULL,clazz,procname,"(Ljava/lang/String;ILjava/lang/String;)Z",jservername,hWnd,jname).z;
cleanup:
  if(env->ExceptionCheck()){env->ExceptionClear();}
  if(clazz!=NULL){env->DeleteLocalRef(clazz);}
  if((jservername!=NULL)&&(servername!=NULL)){env->ReleaseStringChars(jservername,(jchar*)servername);}
  if(jname!=NULL){env->ReleaseStringChars(jname,(jchar*)name);}
  return res;
}

BOOL WINAPI Monitor_AddPort(LPWSTR servername,HWND hWnd,LPWSTR monitorname){
  return Monitor_XXXPort("addPort",servername,hWnd,monitorname);
}

BOOL WINAPI Monitor_ConfigurePort(LPWSTR servername,HWND hWnd,LPWSTR portname){
  return Monitor_XXXPort("configurePort",servername,hWnd,portname);
}

BOOL WINAPI Monitor_DeletePort(LPWSTR servername,HWND hWnd,LPWSTR portname){
  return Monitor_XXXPort("deletePort",servername,hWnd,portname);
}

