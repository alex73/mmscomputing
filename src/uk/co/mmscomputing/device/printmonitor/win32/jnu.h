#include "jni.h"

extern JavaVM* jvm;

JNIEXPORT jstring JNU_NewStringUTF(JNIEnv* env, jboolean* hasException, const char* str);

JNIEXPORT jvalue JNU_CallStaticMethodByName(JNIEnv* env, jboolean* hasException, 
    jclass clazz,const char* name, const char* descriptor, ...);

JNIEXPORT jobject JNU_NewObject(JNIEnv* env, jboolean* hasException, 
    const char* classname, const char* descriptor, ...);

JNIEXPORT jvalue JNU_CallMethodByName(JNIEnv* env, jboolean* hasException, 
    jobject obj,const char* name, const char* descriptor, ...);