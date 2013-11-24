package uk.co.mmscomputing.sms;

public interface SMSConstants{

  static public final int SMS_DLL_DATA               = 0x11;
  static public final int SMS_DLL_ERROR              = 0x12;
  static public final int SMS_DLL_EST                = 0x13;
  static public final int SMS_DLL_REL                = 0x14;
  static public final int SMS_DLL_ACK                = 0x15;
  static public final int SMS_DLL_NACK               = 0x16;

  // ETSI ES 201 912 V 1.2.1 5.3.2.2. p.16

  static public final int SMS_DLL_ERROR_CHECKSUM     = 0x01;  // checksum is not correct
  static public final int SMS_DLL_ERROR_LENGTH       = 0x02;  // msg length not correct
  static public final int SMS_DLL_ERROR_TYPE         = 0x03;  // unknown type
  static public final int SMS_DLL_ERROR_EXTMECHANISM = 0x04;  // extension mechanism not supported
  static public final int SMS_DLL_ERROR_UNSPECIFIED  = 0xFF;  // unspecified error cause
/*
  static public int[] phaseincs0={
        0,  256,  256,  768,  512, 1024, 1024, 1088,
     1248, 1264, 1248, 1264, 1248, 1088, 1024, 1024,
      512,  768,  256,  256,    0, -256, -256, -768,
     -512,-1024,-1024,-1088,-1248,-1264,-1248,-1264,
    -1248,-1088,-1024,-1024, -512, -768, -256, -256
  };

  static public int[] phaseincs1={
    -1088,-1024, -768, -768, -512, -256,    0,    0,
      256,  512,  768,  768, 1024, 1088, 1152, 1248,
     1240, 1256, 1248, 1152, 1088, 1024,  768,  768,
      512,  256,    0,    0, -256, -512, -768, -768,
    -1024,-1088,-1152,-1248,-1256,-1240,-1248,-1152
  };
*/

  static public final int smsDisconnected = 0;
  static public final int smsRinging      = 1;
  static public final int smsConnected    = 2;
}