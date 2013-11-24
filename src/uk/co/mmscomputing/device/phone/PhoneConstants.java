package uk.co.mmscomputing.device.phone;

import java.io.File;
import javax.sound.sampled.*;

public interface PhoneConstants{

  // property keys graphical user interface

  static public final String phoneLawID="uk.co.mmscomputing.device.phone.isalaw";
  static public final String phoneLocalNoID="uk.co.mmscomputing.device.phone.localno";
  static public final String phoneLocalNosID="uk.co.mmscomputing.device.phone.localnos";
  static public final String phoneRemoteNoID="uk.co.mmscomputing.device.phone.remoteno";
  static public final String phoneFileDirID="uk.co.mmscomputing.device.phone.dir";
  static public final String phoneFileID="uk.co.mmscomputing.device.phone.file";
  static public final String phoneTimeOutID="uk.co.mmscomputing.device.phone.timeout";
  static public final String phoneTimeToWaitID="uk.co.mmscomputing.device.phone.timetowait";
  static public final String phoneTimeToRecordID="uk.co.mmscomputing.device.phone.timetorecord";
  static public final String phoneStartMsgID="uk.co.mmscomputing.device.phone.startmsg";
  static public final String phoneEndMsgID="uk.co.mmscomputing.device.phone.endmsg";

  static public final int phoneDisconnected = 0;
  static public final int phoneRinging      = 1;
  static public final int phoneConnected    = 2;

  static public final String phoneDefaultPath=System.getProperty("user.home")+File.separator+"mmsc"+File.separator+"phone";

  static public final AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);
  static public final String phoneBookID="uk.co.mmscomputing.device.phone.phonebook";

  static public final int DefaultPhonePCMBlockSize = 512; // PCM Sound Data block Size
}