package uk.co.mmscomputing.device.fax;

import java.io.File;

public interface FaxConstants{

  // property keys graphical user interface
  static public final String faxHeaderID="uk.co.mmscomputing.device.fax.header";
  static public final String faxLocalNoID="uk.co.mmscomputing.device.fax.localno";
  static public final String faxLocalNosID="uk.co.mmscomputing.device.fax.localnos";

  static public final String faxRemoteNoID="uk.co.mmscomputing.device.fax.remoteno";
  static public final String faxSenderFileDirID="uk.co.mmscomputing.device.faxsender.dir";
  static public final String faxFileID="uk.co.mmscomputing.device.fax.file";
  static public final String faxTimeOutID="uk.co.mmscomputing.device.fax.timeout";

  static public final String faxReceiverFileDirID="uk.co.mmscomputing.device.faxreceiver.dir";
  static public final String faxTimeToWaitID="uk.co.mmscomputing.device.fax.timetowait";
  static public final String faxMaxIllegalLineCodingsID="uk.co.mmscomputing.device.fax.maxillegallinecodings";
  static public final String faxFileTypeID="uk.co.mmscomputing.device.fax.filetype";

  static public final String faxDefaultPath=System.getProperty("user.home")+File.separator+"mmsc"+File.separator+"fax";

  static public final int faxDisconnected = 0;
  static public final int faxRinging      = 1;
  static public final int faxConnected    = 2;

}