#include <stdio.h>
#include <stdarg.h>

#include "util.h"

void Util_showMessage(const LPWSTR format, ... ){
   va_list args;
   int     len;
   LPWSTR  buffer;

   va_start(args,format);
   len    = vwprintf(format,args)+sizeof(WCHAR);
   buffer = (LPWSTR)malloc(len*sizeof(WCHAR));
   vswprintf(buffer,format,args);
   MessageBoxW(NULL,buffer,L"jprintmonitor",MB_OK);
   free(buffer);
}

void Util_showCMessage(const char* format, ... ){
   va_list args;
   int     len;
   char*   buffer;

   va_start(args,format);
   len    = vprintf(format,args)+sizeof(char);
   buffer = (char*)malloc(len*sizeof(char));
   vsprintf(buffer,format,args);
   MessageBoxA(NULL,buffer,"jprintmonitor",MB_OK);
   free(buffer);
}

bool Util_loadLibrary(HINSTANCE* hLibDLL,WCHAR* libname){
  (*hLibDLL)=LoadLibraryW(libname);
  if((*hLibDLL)==NULL){
    Util_showMessage(L"Could not load library %s",libname);
    return false;
  }
  return true;
}

void* Util_getLibraryFunction(HINSTANCE hLibDLL,char* fctname){
  void* fctptr=GetProcAddress(hLibDLL,fctname);
  if(fctptr==NULL){
    Util_showCMessage("Could not find function %s",fctname);
  }
  return fctptr;
}

bool Util_getStringFromRegistry(HKEY key, LPCWSTR name, LPCWSTR buf, int bufsize){
	DWORD type, size;
	if (RegQueryValueExW(key, name, 0, &type, 0, &size) == 0 && type == REG_SZ && (size < (unsigned int)bufsize)){
		if (RegQueryValueExW(key, name, 0, 0, (LPBYTE)buf, &size) == 0){
			return true;
		}
	}
	return false;
}
 
 
