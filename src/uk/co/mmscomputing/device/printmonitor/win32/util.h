#include <windows.h>

#ifndef _MMSC_UTIL_
#define _MMSC_UTIL_

void Util_showMessage(const LPWSTR format, ... );
void Util_showCMessage(const char* format, ... );

bool Util_loadLibrary(HINSTANCE* hLibDLL,WCHAR* libname);
void* Util_getLibraryFunction(HINSTANCE hLibDLL,char* fctname);

bool Util_getStringFromRegistry(HKEY key, LPCWSTR name, LPCWSTR buf, int bufsize);

#endif
