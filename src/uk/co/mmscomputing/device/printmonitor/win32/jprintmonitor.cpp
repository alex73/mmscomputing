/*

  to restart print spooler:

  command line:
          net stop spooler
          net start spooler

*/
#include <windows.h>
#include "winsplp.h"         // windows ddk
#include "jnu.h"
#include "util.h"
#include "monitor.h"

typedef jint (JNICALL *pJNI_CreateJavaVM)(JavaVM **pvm, void **penv, void *args);

char    classpath[]  = "-Djava.class.path=uk.co.mmscomputing.device.printmonitor.jar";  // put jar into system32 folder!
JavaVM* jvm          = NULL;

bool createJVM(void){
	HKEY  key,subkey;    

  WCHAR jreHKey[]=L"Software\\JavaSoft\\Java Development Kit";
  WCHAR version[MAX_PATH+1]={0};   
  WCHAR jreHome[MAX_PATH+1]={0};
	
	if(RegOpenKeyExW(HKEY_LOCAL_MACHINE,jreHKey,0,KEY_READ,&key)!=0){                 // is java installed ?
    Util_showMessage(L"Registry, cannot open: key=%s",jreHKey);		
		return false;    
	}
  if(!Util_getStringFromRegistry(key,L"CurrentVersion",version,sizeof(version))){  // what version is used
    Util_showMessage(L"Registry, cannot read: key=%s\\CurrentVersion",jreHKey);		
		RegCloseKey(key);		
		return false;    
  }
  // todo: check for 1.4 or higher version
	if(RegOpenKeyExW(key,version,0,KEY_READ,&subkey)!= 0){
    Util_showMessage(L"Registry, cannot open: key=%s\\%s",jreHKey,version);		
		RegCloseKey(key);		
		return false;    
	}
	if(!Util_getStringFromRegistry(subkey,L"JavaHome",jreHome,sizeof(jreHome))){     // get directory where jvm.dll is
    Util_showMessage(L"Registry, cannot read: key=%s\\%s\\JavaHome",jreHKey,version);		
		RegCloseKey(key);		
		RegCloseKey(subkey);		
		return false;    
	}
	RegCloseKey(key);    
	RegCloseKey(subkey);    

  HINSTANCE  hLibDLL;

/*
  // if jvm 1.6 need to find msvcr71.dll ourselves
  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6509291

  WCHAR msvcr71[MAX_PATH+1]={0};
  wcscpy(msvcr71,jreHome);
  wcscat(msvcr71,L"\\jre\\bin\\msvcr71.dll");

  if(Util_loadLibrary(&hLibDLL,msvcr71)){
  }
*/

  WCHAR jvmpath[MAX_PATH+1]={0};
  wcscpy(jvmpath,jreHome);
  wcscat(jvmpath,L"\\jre\\bin\\client\\jvm.dll");

//  Util_showMessage(L"Load JVM: %s",jvmpath);		

  if(/* Util_loadLibrary(&hLibDLL,msvcr71) && */ Util_loadLibrary(&hLibDLL,jvmpath)){
    pJNI_CreateJavaVM JNI_CreateJavaVM=(pJNI_CreateJavaVM)Util_getLibraryFunction(hLibDLL,"JNI_CreateJavaVM");
    if(JNI_CreateJavaVM!=NULL){
      JNIEnv*        env;
      JavaVMInitArgs vm_args;
      JavaVMOption   options[1];

      options[0].optionString    = classpath;

      vm_args.version            = JNI_VERSION_1_4;
      vm_args.options            = options;
      vm_args.nOptions           = 1;
      vm_args.ignoreUnrecognized = JNI_TRUE;

      int res=JNI_CreateJavaVM(&jvm,(void**)&env,&vm_args);
      if(res < 0){   Util_showMessage(L"Cannot create Java VM \n\t %s",jvmpath);return false;}

      return true;

//      if(env->ExceptionOccurred()){env->ExceptionDescribe();}
//      jvm->DestroyJavaVM();
    }
  }
  return false;
}

MONITOREX MonitorEx = {
    sizeof(MONITOR),
    {
        Monitor_EnumPorts,
        Monitor_OpenPort,
        NULL,       // Monitor_OpenPortEx,
        Monitor_StartDocPort,
        Monitor_WritePort,
        Monitor_ReadPort,
        Monitor_EndDocPort,
        Monitor_ClosePort,
        Monitor_AddPort,
        NULL,       // Monitor_AddPortEx,
        Monitor_ConfigurePort,
        Monitor_DeletePort,
        NULL,       // Monitor_GetPrinterDataFromPort,
        NULL        // Monitor_SetPortTimeOuts
    }
};

// EXPORT doesn't seem to work with ms vc use def file instead
// otherwise you get decorated (even with extern "C") exports or linkage errors

#define EXPORT __declspec(dllexport)  

EXPORT LPMONITOREX WINAPI InitializePrintMonitor(LPWSTR monitorRegKey){
  if(createJVM()&&Monitor_Init(monitorRegKey)){
//    MessageBoxW(NULL,L"Initialized Print Monitor.",L"jprintmonitor",MB_OK);
    return &MonitorEx;
  }
  MessageBoxW(NULL,L"Initialize Print Monitor Failed",L"jprintmonitor",MB_OK);
  return NULL;
}

int WINAPI DllMain(HINSTANCE hinst, unsigned long reason, void*){
  switch(reason){
  case DLL_PROCESS_ATTACH:
    DisableThreadLibraryCalls(hinst);
    break;
  case DLL_PROCESS_DETACH:
    break;
  }
  return TRUE;
}