
// /usr/java/j2sdk1.4.2/include/jni.h -> /usr/include/jni.h
// /usr/java/j2sdk1.4.2/include/jni_md.h -> /usr/include/jni_md.h

#include <jni.h>                      

void JNU_ThrowByName(
    JNIEnv* env, 
    const char* name, 
    const char* msg
);

jvalue JNU_CallMethodByName(
    JNIEnv* env,
    jboolean* hasException,
    jobject obj,
    const char* name,
    const char* descriptor,
    ...
);

jobject JNU_NewObject(
    JNIEnv* env, 
    jboolean* hasException, 
    const char* classname, 
    const char* descriptor, 
    ...
);

jstring JNU_NewStringUTF(
    JNIEnv* env, 
    jboolean* hasException, 
    const char* str
);

jintArray JNU_NewIntArray(
    JNIEnv* env, 
    jboolean* hasException, 
    const jint size, 
    const jint* list
);

jobjectArray JNU_NewStringArray(
    JNIEnv* env, 
    jboolean* hasException, 
    const jint size, 
    const char** list
);

void JNU_SetIntField(
    JNIEnv* env, 
    jboolean* hasException, 
    jobject obj,
    const char* name,
    jint value
);
