
// C:\j2sdk1.4.2\include\jni.h -> C:\Borland\BCC55\Include\jni.h
// C:\j2sdk1.4.2\include\jni_md.h -> C:\Borland\BCC55\Include\jni_md.h

#include "jni.h"

JNIEnv* JNU_GetEnv(
  JavaVM* jvm
);

JNIEXPORT void JNU_ThrowByName(
    JNIEnv* env, 
    const char* name, 
    const char* msg
);

JNIEXPORT jvalue JNU_CallMethodByName(
    JNIEnv* env,
    jboolean* hasException,
    jobject obj,
    const char* name,
    const char* descriptor,
    ...
);

JNIEXPORT jvalue JNU_CallStaticMethodByName(
    JNIEnv* env,
    jboolean* hasException,
    jclass clazz,
    const char* name,
    const char* descriptor,
    ...
);

JNIEXPORT jobject JNU_NewObject(
    JNIEnv* env, 
    jboolean* hasException, 
    const char* classname, 
    const char* descriptor, 
    ...
);

JNIEXPORT jstring JNU_NewStringUTF(
    JNIEnv* env, 
    jboolean* hasException, 
    const char* str
);

JNIEXPORT jbyteArray JNU_NewByteArray(
    JNIEnv* env, 
    jboolean* hasException, 
    const jint size, 
    const jbyte* list
);

JNIEXPORT jintArray JNU_NewIntArray(
    JNIEnv* env, 
    jboolean* hasException, 
    const jint size, 
    const jint* list
);

JNIEXPORT jobjectArray JNU_NewStringArray(
    JNIEnv* env, 
    jboolean* hasException, 
    const jint size, 
    const char** list
);

JNIEXPORT void JNU_SetIntField(
    JNIEnv* env, 
    jboolean* hasException, 
    jobject obj,
    const char* name,
    jint value
);

JNIEXPORT void JNU_SignalException(
    JNIEnv* env, 
    jclass jclazz, 
    char* cmsg
);
