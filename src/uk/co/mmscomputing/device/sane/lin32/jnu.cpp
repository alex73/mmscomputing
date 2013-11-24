
/* ----- JNI utility functions ----- */
/*
[1] The Java Native Interface
    Sheng Liang
    1999-12
    Addison-Wesley
*/

/*

[1] p.78 6.2.2

  Why the 'hasException' flag ?

  Only a small number of JNI functions can be called safely, when an exception is pending !
  List of these functions p.162 11.8.2

*/

#include "jnu.h"

/* ----- [1] p.75 6.1.2 ----- */

JNIEXPORT void JNU_ThrowByName(JNIEnv* env, const char* name, const char* msg){
  jclass cls=env->FindClass(name);
  if(cls!=NULL){ 
    env->ThrowNew(cls,msg);
  }/*else{
    exception already occurred
  }
  */
  env->DeleteLocalRef(cls);  
}

/* ----- [1] p.79 6.2.2 ----- */

JNIEXPORT jvalue JNU_CallMethodByName(JNIEnv* env, jboolean* hasException, 
    jobject obj,const char* name, const char* descriptor, ...){

  va_list args; jclass clazz; jmethodID mid; jvalue result;
  if(env->EnsureLocalCapacity(2)==JNI_OK){
    clazz=env->GetObjectClass(obj);
    mid=env->GetMethodID(clazz,name,descriptor);
    if(mid){
      const char* p=descriptor;
      while(*p!=')'){ p++;} p++;
      va_start(args,descriptor);
      switch(*p){
      case 'V':           env->CallVoidMethodV(obj,mid,args);               break;
      case '[': case 'L': result.l=env->CallObjectMethodV(obj,mid,args);    break;
      case 'Z':           result.z=env->CallBooleanMethodV(obj,mid,args);   break;
      case 'B':           result.b=env->CallByteMethodV(obj,mid,args);      break;
      case 'C':           result.c=env->CallCharMethodV(obj,mid,args);      break;
      case 'S':           result.s=env->CallShortMethodV(obj,mid,args);     break;
      case 'I':           result.i=env->CallIntMethodV(obj,mid,args);       break;
      case 'J':           result.j=env->CallLongMethodV(obj,mid,args);      break;
      case 'F':           result.f=env->CallFloatMethodV(obj,mid,args);     break;
      case 'D':           result.d=env->CallDoubleMethodV(obj,mid,args);    break;
      default: env->FatalError("jnu.c - JNU_CallMethodByName : Illegal descriptor.");
      }
      va_end(args);
    }
    env->DeleteLocalRef(clazz);
  }
  if(hasException){
    *hasException=env->ExceptionCheck();
  }
  return result;
}

JNIEXPORT jobject JNU_NewObject(JNIEnv* env, jboolean* hasException, 
    const char* classname, const char* descriptor, ...){

  if((hasException==NULL)||((*hasException)==JNI_FALSE)){
    va_list args; jclass clazz; jmethodID mid; jobject result;

    result=NULL;
    if(env->EnsureLocalCapacity(2)==JNI_OK){
      clazz=env->FindClass(classname);
      if(clazz==NULL){return NULL;}
      mid=env->GetMethodID(clazz,"<init>",descriptor);
      if(mid){
        va_start(args,descriptor);
        result=env->NewObjectV(clazz,mid,args);
        va_end(args);
      }
      env->DeleteLocalRef(clazz);
    }
    if(hasException){
      (*hasException)=env->ExceptionCheck();
    }
    return result;
  }
  return NULL;
}

JNIEXPORT jstring JNU_NewStringUTF(JNIEnv* env, jboolean* hasException, const char* str){
  if((hasException==NULL)||((*hasException)==JNI_FALSE)){
    jstring jstr=env->NewStringUTF(str);
    if(hasException){
      (*hasException)=env->ExceptionCheck();
    } 
    return jstr;
  }
  return NULL;
}

JNIEXPORT jintArray JNU_NewIntArray(JNIEnv* env, jboolean* hasException, const jint size, const jint* list){
  if((hasException==NULL)||((*hasException)==JNI_FALSE)){
    jintArray array=env->NewIntArray(size);
    if((array!=NULL)&&(list!=NULL)){
      env->SetIntArrayRegion(array,0,size,(jint*)list);
    }
    if(hasException){
      (*hasException)=env->ExceptionCheck();
    } 
    return array;
  }
  return NULL;
}

JNIEXPORT jobjectArray JNU_NewStringArray(JNIEnv* env, jboolean* hasException, const jint size, const char** list){
  jobjectArray array=NULL;
  if((hasException==NULL)||((*hasException)==JNI_FALSE)){
    jclass clazz=env->FindClass("java/lang/String");
    if(clazz!=NULL){
      array=(jobjectArray)env->NewObjectArray(size,clazz,NULL);
      if((array!=NULL)&&(list!=NULL)){
        jstring jstr;
        jint i;
        for(i=0;i<size;i++){
          jstr=env->NewStringUTF(list[i]);
          if(jstr==NULL){ break;}                                      
          env->SetObjectArrayElement(array,i,jstr);      
        }
      }
      env->DeleteLocalRef(clazz);
    }
    if(hasException){
      (*hasException)=env->ExceptionCheck();
    } 
  }
  return array;
}

JNIEXPORT void JNU_SetIntField(JNIEnv* env, jboolean* hasException, jobject obj,const char* name,jint value){
  if((hasException==NULL)||((*hasException)==JNI_FALSE)){
    jclass   clazz=env->GetObjectClass(obj);                   
    jfieldID fid=env->GetFieldID(clazz,name,"I");
    if(fid!=NULL){                                               
      env->SetIntField(obj,fid,value);
    }
    env->DeleteLocalRef(clazz);
    if(hasException){
      (*hasException)=env->ExceptionCheck();
    } 
  }
}


