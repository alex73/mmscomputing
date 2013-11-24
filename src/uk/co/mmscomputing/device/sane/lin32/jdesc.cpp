/*

  File: uk/co/mmscomputing/device/sane/linux/jdesc.cpp

[1] SANE Standard Version 1.03
    (Scanner Access Now Easy)
    2002-10-10
    http://www.sane-project.org

[2] The Java Native Interface
    Sheng Liang
    1999-12
    Addison-Wesley
*/

//#include <sane/sane.h>                                // /usr/include/sane/sane.h
//#include <sane/saneopts.h>                            // /usr/include/sane/saneopts.h
#include "sane.h"                                       // use my own version here, due to dynamic loading

#include "jnu.h"                                        // some JNI utilitiy functions
#include "jsane.h"                                    
#include "../uk_co_mmscomputing_device_sane_jsane.h"    // uk/co/mmscomputing/device/sane/uk_...h

/* ----- getOptionDescriptor ------ */

jobject newDescriptor(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc
){                                                                    // SANE_CONSTRAINT_NONE=0
  return JNU_NewObject(  // return new OptionDescriptor(desc.name,desc.title,..,desc.cap);
      env,
      hasException,
      "uk/co/mmscomputing/device/sane/option/Descriptor",
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap
  );
}

jobject newBoolDesc(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){
  const char* classname="uk/co/mmscomputing/device/sane/option/BoolDesc";
  return JNU_NewObject(  // return new BoolDesc(desc.name,desc.title,..,desc.cap,values);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newIntDesc(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){
  const char* classname="uk/co/mmscomputing/device/sane/option/IntDesc";
  return JNU_NewObject(  // return new IntDesc(desc.name,desc.title,..,desc.cap,values);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newIntRange(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){  // 1
  const char* classname="uk/co/mmscomputing/device/sane/option/IntRange";
  return JNU_NewObject(  // return new IntRange(desc.name,desc.title,..,desc.cap,desc.min,..,desc.quant,values);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIIIII[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,

      desc->constraint.range->min,
      desc->constraint.range->max,
      desc->constraint.range->quant,

      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newIntList(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){     // 2
  const char* classname="uk/co/mmscomputing/device/sane/option/IntList";
  return JNU_NewObject(  // return new IntListOptionDescriptor(desc.name,desc.title,..,desc.cap,desc.word_list,value);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[I[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewIntArray(env,hasException,desc->constraint.word_list[0],&desc->constraint.word_list[1]),
      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newFixedDesc(JNIEnv* env,jboolean* hasException,
    jint handle,jint option,const SANE_Option_Descriptor* desc, jint* values
){
  const char* classname="uk/co/mmscomputing/device/sane/option/FixedDesc";
  return JNU_NewObject(  // return new FixedDesc(desc.name,desc.title,..,desc.cap,values);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newFixedRange(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){
  const char* classname="uk/co/mmscomputing/device/sane/option/FixedRange";
  return JNU_NewObject(  // return new FixedRange(desc.name,desc.title,..,desc.cap,desc.min,..,desc.quant,value);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIIIII[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,

      desc->constraint.range->min,
      desc->constraint.range->max,
      desc->constraint.range->quant,

      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newFixedList(JNIEnv* env,jboolean* hasException,
    jint handle,jint option, const SANE_Option_Descriptor* desc, jint* values
){
  const char* classname="uk/co/mmscomputing/device/sane/option/FixedList";
  return JNU_NewObject(  // return new FixedList(desc.name,desc.title,..,desc.cap,desc.word_list,values);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[I[I)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewIntArray(env,hasException,desc->constraint.word_list[0],&desc->constraint.word_list[1]),
      JNU_NewIntArray(env,hasException,(desc->size>>2),values)
  );
}

jobject newStringDesc(JNIEnv* env,jboolean* hasException,
    jint handle,jint option,const SANE_Option_Descriptor* desc, char* value
){
  return JNU_NewObject(  // return new StringDesc(desc.name,desc.title,..,desc.cap,value);
      env,
      hasException,
      "uk/co/mmscomputing/device/sane/option/StringDesc",
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewStringUTF(env,hasException,value)
  );
}

jobject newStringList(JNIEnv* env,jboolean* hasException,
    jint handle,jint option,const SANE_Option_Descriptor* desc, char* value
){
  jint size;
  SANE_String_Const* list=(SANE_String_Const*)desc->constraint.string_list;
  for(size=0;list[size]!=NULL;size++){}             // list is a NULL terminated list
  return JNU_NewObject(  // return new StringList(desc.name,desc.title,..,desc.cap,desc.string_list,value);
      env,
      hasException,
      "uk/co/mmscomputing/device/sane/option/StringList",
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII[Ljava/lang/String;Ljava/lang/String;)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap,
      JNU_NewStringArray(env,hasException,size,list),
      JNU_NewStringUTF(env,hasException,value)
  );
}

jobject newButtonDesc(JNIEnv* env,jboolean* hasException,
    jint handle,jint option,const SANE_Option_Descriptor* desc
){
  const char* classname="uk/co/mmscomputing/device/sane/option/ButtonDesc";
  return JNU_NewObject(  // return new BoolDesc(desc.name,desc.title,..,desc.cap,value);
      env,
      hasException,
      classname,
      "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII)V",
      handle,option,
      JNU_NewStringUTF(env,hasException,desc->name),
      JNU_NewStringUTF(env,hasException,desc->title),
      JNU_NewStringUTF(env,hasException,desc->desc),
      desc->type,desc->unit,desc->size,desc->cap
  );
}

// Option Descriptor Functions

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getWordControlOption(JNIEnv* env,jclass cls,
    jint handle,jint option
){
  if(env->ExceptionCheck()==JNI_FALSE){

    jint        value;
    SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,&value,NULL);

    jboolean hasException=JNI_FALSE;
    if(checkStatus(env,&hasException,status)){
      return value;
    }
  }
  return 0;
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setWordControlOption(JNIEnv* env,jclass cls,
    jint handle,jint option,jint value
){
  if(env->ExceptionCheck()==JNI_FALSE){

    SANE_Int    info=0;
    SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_SET_VALUE,&value,&info);

    jboolean hasException=JNI_FALSE;
    if(checkStatus(env,&hasException,status)){
      return info;
    }
  }
  return 0;
}

JNIEXPORT void JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getWordArrayControlOption(
    JNIEnv* env,jclass cls,jint handle,jint option,jintArray jbuf
){
  if(env->ExceptionCheck()==JNI_FALSE){

    jint* cbuf=env->GetIntArrayElements(jbuf,NULL);
    if(cbuf!=NULL){

      SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,cbuf,NULL);

      env->ReleaseIntArrayElements(jbuf,cbuf,0);

      jboolean hasException=JNI_FALSE;
      checkStatus(env,&hasException,status);
    }
  }
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setWordArrayControlOption(
    JNIEnv* env,jclass cls,jint handle,jint option,jintArray jbuf
){
  if(env->ExceptionCheck()==JNI_FALSE){

    jint*    cbuf=env->GetIntArrayElements(jbuf,NULL);
    if(cbuf!=NULL){

      SANE_Int info=0;
      SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_SET_VALUE,cbuf,&info);

      env->ReleaseIntArrayElements(jbuf,cbuf,0);

      jboolean hasException=JNI_FALSE;
      if(checkStatus(env,&hasException,status)){
        return info;
      }
    }
  }
  return 0;
}

JNIEXPORT jstring JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getStringControlOption(
    JNIEnv* env,jclass cls,jint handle,jint option,jint csize
){
  if(env->ExceptionCheck()==JNI_FALSE){

    char cstr[csize];
    SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,cstr,NULL);

    jboolean hasException=JNI_FALSE;
    if(checkStatus(env,&hasException,status)){
      return JNU_NewStringUTF(env,&hasException,cstr);
    }
  }
  return NULL;
}

JNIEXPORT jint JNICALL Java_uk_co_mmscomputing_device_sane_jsane_setStringControlOption(
    JNIEnv* env,jclass cls,jint handle,jint option,jint csize,jstring jstr
){
  if(env->ExceptionCheck()==JNI_FALSE){

    if(jstr==NULL){ 
      JNU_ThrowByName(env,"java/lang/NullPointerException","jdesc.c:setStringControlOption: null pointer exception. jbuf = null");
      return 0;
    }
    jint jsize=env->GetStringUTFLength(jstr);

// 2007-09-20
//    if(csize<=jsize){
//      JNU_ThrowByName(env,"uk/co/mmscomputing/device/sane/SaneIOException","jdesc.c:setStringControlOption: jstr too big.");
//      return 0;
//    }

    char cstr[jsize+1];

    env->GetStringUTFRegion(jstr,0,jsize,cstr);cstr[jsize]=0;
    if(env->ExceptionCheck()==JNI_FALSE){
      jint info=0;
      SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_SET_VALUE,&cstr,&info);

      jboolean hasException=JNI_FALSE;
      if(checkStatus(env,&hasException,status)){
        return info;
      }
    }
  }
  return 0;
}

JNIEXPORT jobject JNICALL Java_uk_co_mmscomputing_device_sane_jsane_getOptionDescriptor(JNIEnv* env,jclass cls,
  jint handle,jint option
){
  if(env->ExceptionCheck()==JNI_FALSE){

    const SANE_Option_Descriptor* desc =sane_get_option_descriptor((SANE_Handle)handle,option);

//  fprintf(stderr,"getOptionDescriptor handle=0x%X    option=%d    desc=0x%X\n",(int)handle,option,(int)desc);

    if(desc!=NULL){
      jboolean hasException=JNI_FALSE;
      jobject  jdesc       =NULL;

      switch(desc->type){
      case SANE_TYPE_BOOL:{
        int values[desc->size>>2];

//      getWordArray(env,jsaneobj,option,&hasException,desc,values);

        if((desc->cap&SANE_CAP_SOFT_SELECT)==SANE_CAP_SOFT_SELECT){
          SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,values,NULL);
          checkStatus(env,&hasException,status);
        }
        jdesc = newBoolDesc(env,&hasException,handle,option,desc,values);
        break;
      }
      case SANE_TYPE_INT:{
        int values[desc->size>>2];

//      getWordArray(env,jsaneobj,option,&hasException,desc,values);

        if((desc->cap&SANE_CAP_SOFT_SELECT)==SANE_CAP_SOFT_SELECT){
          SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,values,NULL);
          checkStatus(env,&hasException,status);
        }

        switch(desc->constraint_type){
        case SANE_CONSTRAINT_NONE:      jdesc = newIntDesc(env,&hasException,handle,option,desc,values);  break;
        case SANE_CONSTRAINT_RANGE:     jdesc = newIntRange(env,&hasException,handle,option,desc,values); break;
        case SANE_CONSTRAINT_WORD_LIST: jdesc = newIntList(env,&hasException,handle,option,desc,values);  break;
        default: break;
        }
        break;
      }
      case SANE_TYPE_FIXED:{
        int values[desc->size>>2];

//      getWordArray(env,jsaneobj,option,&hasException,handle,option,desc,values);

        if((desc->cap&SANE_CAP_SOFT_SELECT)==SANE_CAP_SOFT_SELECT){
          SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,values,NULL);
          checkStatus(env,&hasException,status);
        }

        switch(desc->constraint_type){
        case SANE_CONSTRAINT_NONE:      jdesc = newFixedDesc(env,&hasException,handle,option,desc,values);  break;
        case SANE_CONSTRAINT_RANGE:     jdesc = newFixedRange(env,&hasException,handle,option,desc,values); break;
        case SANE_CONSTRAINT_WORD_LIST: jdesc = newFixedList(env,&hasException,handle,option,desc,values);  break;
        default: break;
        }
        break;
      }
      case SANE_TYPE_STRING:{

        char cstr[desc->size];
        SANE_Status status=sane_control_option((SANE_Handle)handle,option,SANE_ACTION_GET_VALUE,cstr,NULL);

        if(checkStatus(env,&hasException,status)){
          switch(desc->constraint_type){
          case SANE_CONSTRAINT_NONE:        jdesc = newStringDesc(env,&hasException,handle,option,desc,cstr); break;
          case SANE_CONSTRAINT_STRING_LIST: jdesc = newStringList(env,&hasException,handle,option,desc,cstr); break;
          default: break;
          }
        }
        break;
      }
      case SANE_TYPE_BUTTON:
        jdesc = newButtonDesc(env,&hasException,handle,option,desc);
        break;
      default:                          
        jdesc = newDescriptor(env,&hasException,handle,option,desc);
        break;
      }
      return (hasException)?NULL:jdesc;
    }
  }
  return NULL;
}

