#include <windows.h>
#include "jnu.h"

jobject BMP_transferImage(
    JNIEnv* env, 
    jclass jclazz,
    jboolean* hasException, 
    HGLOBAL dib
);
/*
void BMP_writeImage(
    JNIEnv* env, 
    jclass jclazz,
    jboolean* hasException, 
    HGLOBAL dib
);
*/