#include "jnu.h"

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

JNIEXPORT jvalue JNU_CallMethodByName(JNIEnv* env, jboolean* hasException, 
    jobject obj,const char* name, const char* descriptor, ...){

  va_list args; jclass clazz; jmethodID mid; jvalue result={0};
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
      default: env->FatalError("jnu.cpp - JNU_CallMethodByName : Illegal descriptor.");
      }
      va_end(args);
    }else{
      fprintf(stdout,"COULD NOT FIND %s\n",name);
    }
    env->DeleteLocalRef(clazz);
  }
  if(hasException){
    *hasException=env->ExceptionCheck();
  }
  return result;
}

JNIEXPORT jvalue JNU_CallStaticMethodByName(JNIEnv* env, jboolean* hasException, 
    jclass clazz,const char* name, const char* descriptor, ...){

  va_list args; jmethodID mid; jvalue result={0};
  if(env->EnsureLocalCapacity(2)==JNI_OK){
    mid=env->GetStaticMethodID(clazz,name,descriptor);
    if(mid){
      const char* p=descriptor;
      while(*p!=')'){ p++;} p++;
      va_start(args,descriptor);
      switch(*p){
      case 'V':           env->CallStaticVoidMethodV(clazz,mid,args);               break;
      case '[': case 'L': result.l=env->CallStaticObjectMethodV(clazz,mid,args);    break;
      case 'Z':           result.z=env->CallStaticBooleanMethodV(clazz,mid,args);   break;
      case 'B':           result.b=env->CallStaticByteMethodV(clazz,mid,args);      break;
      case 'C':           result.c=env->CallStaticCharMethodV(clazz,mid,args);      break;
      case 'S':           result.s=env->CallStaticShortMethodV(clazz,mid,args);     break;
      case 'I':           result.i=env->CallStaticIntMethodV(clazz,mid,args);       break;
      case 'J':           result.j=env->CallStaticLongMethodV(clazz,mid,args);      break;
      case 'F':           result.f=env->CallStaticFloatMethodV(clazz,mid,args);     break;
      case 'D':           result.d=env->CallStaticDoubleMethodV(clazz,mid,args);    break;
      default: env->FatalError("jnu.cpp - JNU_CallMethodByName : Illegal descriptor.");
      }
      va_end(args);
    }else{
      fprintf(stdout,"COULD NOT FIND %s\n",name);
    }
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
