#include "bmp.h"
#include "twain.h"
#include "..\uk_co_mmscomputing_device_twain_jtwain.h"

//-- Twain 2.0 memory declarations start

// Defined in jtwain.cpp but also used in bmp.cpp

TW_MEMREF FAR PASCAL DSM_Lock  (TW_HANDLE memory);
void      FAR PASCAL DSM_Unlock(TW_MEMREF memory);
void      FAR PASCAL DSM_Free  (TW_HANDLE ptr);

//-- Twain 2.0 memory declarations end

#define TYPE_CUSTOM             0x00
#define TYPE_INT_RGB            0x01
#define TYPE_INT_ARGB           0x02
#define TYPE_INT_ARGB_PRE       0x03
#define TYPE_INT_BGR            0x04
#define TYPE_3BYTE_BGR          0x05
#define TYPE_4BYTE_ABGR         0x06
#define TYPE_4BYTE_ABGR_PRE     0x07
#define TYPE_USHORT_565_RGB     0x08
#define TYPE_USHORT_555_RGB     0x09
#define TYPE_BYTE_GRAY          0x0A
#define TYPE_USHORT_GRAY        0x0B
#define TYPE_BYTE_BINARY        0x0C
#define TYPE_BYTE_INDEXED       0x0D

static char TwainBufferedImage[] ="uk/co/mmscomputing/device/twain/TwainBufferedImage";

jint getColorsInPalette(const BITMAPINFOHEADER* bih){
  if(bih->biClrUsed!=0){           // as in info header
    return bih->biClrUsed;
  }else if(bih->biBitCount<16){    // 2, 16, 256 colours
    return 1<<bih->biBitCount;
  }else{                           // 0 for 16,24,32 bits per pixel
    return 0;
  }
}

jbyte* getImageData(const BITMAPINFOHEADER* bih){
  jbyte* jbuf=(jbyte*)&(((BITMAPINFO*)bih)->bmiColors[getColorsInPalette(bih)]);
  if(bih->biCompression==BI_BITFIELDS){
    jbuf+=3*sizeof(DWORD);
  }
  return jbuf;
}

void BMP_setImage(JNIEnv* env,jclass jclazz,jobject jimage, jboolean* hasException,const BITMAPINFOHEADER* bih,jbyteArray jbuf, jint jBytesPerLine){
  jint   cBytesPerLine=((bih->biWidth*bih->biBitCount+31)>>5)<<2;   // include DWORD padding
  jint   joffset=0;
  jbyte* coffset=getImageData(bih);coffset+=(bih->biHeight-1)*cBytesPerLine;
  for(int i=bih->biHeight-1;i>=0;i--){
    env->SetByteArrayRegion(jbuf,joffset,jBytesPerLine,coffset);
    joffset+=jBytesPerLine;
    coffset-=cBytesPerLine;
  }
  env->DeleteLocalRef(jbuf);
}

jobject get01BitIndexColorModel(JNIEnv* env, jboolean* hasException,const BITMAPINFOHEADER* bih){
  const  int bitcount=1;
  jbyte  r[1<<bitcount]={0},g[1<<bitcount]={0},b[1<<bitcount]={0};
  const  int maxcol=1<<bitcount;

  for(int i=0;i<maxcol;i++){
    r[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbRed;
    g[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbGreen;
    b[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbBlue;
  }
  jobject jcoltab=JNU_NewObject(env,hasException,"java/awt/image/IndexColorModel","(II[B[B[B)V",
    bitcount,maxcol,
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&r),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&g),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&b)
  );
  return jcoltab;
}

jobject BMP_transfer01BitImage(JNIEnv* env, jclass jclazz,jboolean* hasException, const BITMAPINFOHEADER* bih){
  jobject jimage=(jobject)JNU_NewObject(
      env,hasException,
      TwainBufferedImage,
      "(IIILjava/awt/image/IndexColorModel;)V",
      bih->biWidth,
      bih->biHeight,
      TYPE_BYTE_BINARY,
      get01BitIndexColorModel(env,hasException,bih)
  );
  if(jimage!=NULL){
    jbyteArray jbuf=(jbyteArray)(JNU_CallMethodByName(
        env,hasException,
        jimage,
        "getBuffer",
        "()[B"
    ).l);
    if(jbuf!=NULL){
      BMP_setImage(env,jclazz,jimage,hasException,bih,jbuf,(bih->biWidth+7)>>3);
    }
  }
  return jimage;
}

jobject get04BitIndexColorModel(JNIEnv* env, jboolean* hasException,const BITMAPINFOHEADER* bih){
  const  int bitcount=4;
  jbyte  r[1<<bitcount]={0},g[1<<bitcount]={0},b[1<<bitcount]={0};
  int    maxcol=getColorsInPalette(bih);

  for(int i=0;i<maxcol;i++){
    r[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbRed;
    g[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbGreen;
    b[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbBlue;
  }
  jobject jcoltab=JNU_NewObject(env,hasException,"java/awt/image/IndexColorModel","(II[B[B[B)V",
    bitcount,maxcol,
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&r),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&g),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&b)
  );
  return jcoltab;
}

jobject BMP_transfer04BitImage(JNIEnv* env,jclass jclazz,jboolean* hasException, const BITMAPINFOHEADER* bih){
  jobject jimage=(jobject)JNU_NewObject(
      env,hasException,
      TwainBufferedImage,
      "(IIILjava/awt/image/IndexColorModel;)V",
      bih->biWidth,
      bih->biHeight,
      TYPE_BYTE_BINARY,
      get04BitIndexColorModel(env,hasException,bih)
  );
  if(jimage!=NULL){
    jbyteArray jbuf=(jbyteArray)(JNU_CallMethodByName(
        env,hasException,
        jimage,
        "getBuffer",
        "()[B"
    ).l);
    if(jbuf!=NULL){
      BMP_setImage(env,jclazz,jimage,hasException,bih,jbuf,(bih->biWidth+1)>>1);
    }
  }
  return jimage;
}

jobject get08BitIndexColorModel(JNIEnv* env, jboolean* hasException,const BITMAPINFOHEADER* bih){
  const  int bitcount=8;
  jbyte  r[1<<bitcount]={0},g[1<<bitcount]={0},b[1<<bitcount]={0};
  int    maxcol=getColorsInPalette(bih);

  for(int i=0;i<maxcol;i++){
    r[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbRed;
    g[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbGreen;
    b[i]=((BITMAPINFO*)bih)->bmiColors[i].rgbBlue;
  }
  jobject jcoltab=JNU_NewObject(env,hasException,"java/awt/image/IndexColorModel","(II[B[B[B)V",
    bitcount,maxcol,
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&r),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&g),
    JNU_NewByteArray(env,hasException,maxcol,(const jbyte*)&b)
  );
  return jcoltab;
}

jboolean checkGreyScale(JNIEnv* env, jboolean* hasException,const BITMAPINFOHEADER* bih){
  if(getColorsInPalette(bih)!=256){ return FALSE;}

  RGBQUAD color;
  for(int i=0;i<256;i++){
    color = ((BITMAPINFO*)bih)->bmiColors[i];
    if(color.rgbRed  !=i){ return FALSE;}
    if(color.rgbGreen!=i){ return FALSE;}
    if(color.rgbBlue !=i){ return FALSE;}
  }
  return TRUE;
}

jobject BMP_transfer08BitImage(JNIEnv* env, jclass jclazz,jboolean* hasException, const BITMAPINFOHEADER* bih){
  jobject jimage;
  if(checkGreyScale(env,hasException,bih)){
    jimage=(jobject)JNU_NewObject(
        env,hasException,
        TwainBufferedImage,
        "(III)V",
        bih->biWidth,
        bih->biHeight,
        TYPE_BYTE_GRAY
    );
  }else{
    jimage=(jobject)JNU_NewObject(
        env,hasException,
        TwainBufferedImage,
        "(IIILjava/awt/image/IndexColorModel;)V",
        bih->biWidth,
        bih->biHeight,
        TYPE_BYTE_INDEXED,
        get08BitIndexColorModel(env,hasException,bih)
    );
  }
  if(jimage!=NULL){
    jbyteArray jbuf=(jbyteArray)(JNU_CallMethodByName(
        env,hasException,
        jimage,
        "getBuffer",
        "()[B"
    ).l);
    if(jbuf!=NULL){
      BMP_setImage(env,jclazz,jimage,hasException,bih,jbuf,bih->biWidth);
    }
  }
  return jimage;
}

jobject BMP_transfer24BitImage(JNIEnv* env, jclass jclazz,jboolean* hasException, const BITMAPINFOHEADER* bih){
  jobject jimage=(jobject)JNU_NewObject(
      env,hasException,
      TwainBufferedImage,
      "(III)V",
      bih->biWidth,
      bih->biHeight,
      TYPE_3BYTE_BGR
  );
  if(jimage!=NULL){
    jbyteArray jbuf=(jbyteArray)(JNU_CallMethodByName(
        env,hasException,
        jimage,
        "getBuffer",
        "()[B"
    ).l);
    if(jbuf!=NULL){
      BMP_setImage(env,jclazz,jimage,hasException,bih,jbuf,bih->biWidth*3);
    }
  }
  return jimage;
}

jobject BMP_transferImage(JNIEnv* env, jclass jclazz,jboolean* hasException, HGLOBAL dib){
  jobject jimage=NULL;

  if(dib!=NULL){
    BITMAPINFOHEADER* bih=(BITMAPINFOHEADER*)DSM_Lock(dib);           // get a pointer to the beginning of the DIB
    if(bih!=NULL){ 
      int type=(int)bih->biCompression;

//    fprintf(stderr,"bitCount=%d\ntype=%d\n",bih->biBitCount,bih->biCompression);

      if((type==BI_RGB)||(type==BI_BITFIELDS)){
        switch(bih->biBitCount){
        case  1: jimage=BMP_transfer01BitImage(env,jclazz,hasException,bih);break;
        case  4: jimage=BMP_transfer04BitImage(env,jclazz,hasException,bih);break;
        case  8: jimage=BMP_transfer08BitImage(env,jclazz,hasException,bih);break;
        case 24: jimage=BMP_transfer24BitImage(env,jclazz,hasException,bih);break;
        default:
          JNU_SignalException(env,jclazz,"Unsupported biBitCount in DIB.");
          break;
        }
        JNU_CallMethodByName(
          env,hasException,
          jimage,
          "setPixelsPerMeter",
          "(II)V",
          bih->biXPelsPerMeter,
          bih->biYPelsPerMeter
        );
      }else{
        JNU_SignalException(env,jclazz,"Cannot deal with DIB type [RLE4 or RLE8].");
      }
      DSM_Unlock(dib);
    }else{
      JNU_SignalException(env,jclazz,"Cannot get DIB pointer.");
    }
    DSM_Free(dib);
  }else{
    JNU_SignalException(env,jclazz,"DIB is a NULL pointer.");
  }
  return jimage;
}
/*
void BMP_writeImage(JNIEnv* env, jclass jclazz,jboolean* hasException, HGLOBAL dib){
  BITMAPFILEHEADER  bfh={0};
  BITMAPINFOHEADER* bih;
  jint              len;
  if(dib!=NULL){
    len=DSM_Size(dib);                              
    bih=(BITMAPINFOHEADER*)DSM_Lock(dib);           // get a pointer to the beginning of the DIB
    if(bih!=NULL){ 
      jbyteArray jbuf=env->NewByteArray(sizeof(BITMAPFILEHEADER)+len);
      if(jbuf!=NULL){
        bfh.bfType=0x4D42;                            // "BM"
        bfh.bfSize=sizeof(BITMAPFILEHEADER)+len;
        bfh.bfOffBits=sizeof(BITMAPFILEHEADER)
                     +sizeof(BITMAPINFOHEADER)
                     +sizeof(RGBQUAD)*getColorsInPalette(bih)
                     +((bih->biCompression==BI_BITFIELDS)?6:0);  // 16 & 32bit
        env->SetByteArrayRegion(jbuf,0,sizeof(BITMAPFILEHEADER),(jbyte*)&bfh);
        env->SetByteArrayRegion(jbuf,sizeof(BITMAPFILEHEADER),len,(jbyte*)bih);
        JNU_CallStaticMethodByName(env,hasException,jclazz,"writeImage","([B)V",jbuf);
        env->DeleteLocalRef(jbuf);
      }else{
        JNU_SignalException(env,jclazz,"Cannot allocate image buffer.");
      }
      DSM_Unlock(dib);
    }else{
      JNU_SignalException(env,jclazz,"Cannot get DIB pointer.");
    }
    DSM_Free(dib);
  }else{
    JNU_SignalException(env,jclazz,"DIB is a NULL pointer.");
  }
}
*/
/*
[1]
    TWAIN Specification Version 1.9
    2000-01-20
    http://www.twain.org
*/