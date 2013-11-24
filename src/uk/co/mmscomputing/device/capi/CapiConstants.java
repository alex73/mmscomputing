package uk.co.mmscomputing.device.capi;

public interface CapiConstants{

  static public final int MaxNumB3DataBlocks = 7;    	 // max. number of unconfirmed B3-datablocks
                                                       // 7 is the maximal number supported by CAPI
  static public final int MaxB3DataBlockSize = 2048;   // max. B3-Datablocksize
                                                       // 2048 is the maximum supported by CAPI (where from ?)
  static public final int DefaultB3DataBlockSize = 256;// B3 data block size needs to be small for real time phone applications
  static public final int DefaultPCMBlockSize = DefaultB3DataBlockSize<<1; // PCM Sound Data block Size

//----- CAPI commands -----

  static public final int CAPI_ALERT                 = 0x01;
  static public final int CAPI_CONNECT               = 0x02;
  static public final int CAPI_CONNECT_ACTIVE        = 0x03;
  static public final int CAPI_CONNECT_B3_ACTIVE     = 0x83;
  static public final int CAPI_CONNECT_B3            = 0x82;
  static public final int CAPI_CONNECT_B3_T90_ACTIVE = 0x88;
  static public final int CAPI_DATA_B3               = 0x86;
  static public final int CAPI_DISCONNECT_B3         = 0x84;
  static public final int CAPI_DISCONNECT            = 0x04;
  static public final int CAPI_FACILITY              = 0x80;
  static public final int CAPI_INFO                  = 0x08;
  static public final int CAPI_LISTEN                = 0x05;
  static public final int CAPI_MANUFACTURER          = 0xff;
  static public final int CAPI_RESET_B3              = 0x87;
  static public final int CAPI_SELECT_B_PROTOCOL     = 0x41;

//----- CAPI subcommands -----

  static public final int CAPI_REQ   = 0x80;
  static public final int CAPI_CONF  = 0x81;
  static public final int CAPI_IND   = 0x82;
  static public final int CAPI_RESP  = 0x83;

//----- CIP Value
  static public final int CIP_ACCEPTALL    = 0x1FFF03FF;	//	0x1FFF03FF = connect to everything cip mask
  static public final int CIP_SPEECH       = 1;

  static public final int ACCEPT = 0;			//	accept call
  static public final int IGNORE = 1;			//	ignore call
  static public final int REJECT = 2;			//	reject call

  public static final int KBIT64 = 1;
  public static final int KBIT56 = 6;

//----- Q.931
  static public final int LAYER1USERINFO_uLAW = 0x02;			//	u-Law
  static public final int LAYER1USERINFO_ALAW = 0x03;			//	A-Law

  static final public int CAPI_FACILITY_HANDSET=0;
  static final public int CAPI_FACILITY_DTMF=1;
  static final public int CAPI_FACILITY_V42bis=2;
  static final public int CAPI_FACILITY_SUPSERVICE=3;
  static final public int CAPI_FACILITY_PWRMGMT=4;
  static final public int CAPI_FACILITY_INTERCONNECT=5;

  static final public int CAPI_FACILITY_BROADBAND=6;        // ? Next three Eicon only ?
  static final public int CAPI_FACILITY_CONTROLLEREVENTS=7; 
  static final public int CAPI_FACILITY_ECHOCANCELLATION=8; // eicon/sdk/Doc/CxEcho.pdf



  static final public int CAPI_PROTOCOL_TRANSPARENT = 0;   // NCPI
  static final public int CAPI_PROTOCOL_T90NL       = 1;
  static final public int CAPI_PROTOCOL_ISO8208     = 2;
  static final public int CAPI_PROTOCOL_X25         = 3;
  static final public int CAPI_PROTOCOL_T30_FAX     = 4;
  static final public int CAPI_PROTOCOL_T30_FAX_EXT = 5;
  static final public int CAPI_PROTOCOL_MODEM       = 7;

  static final public int CAPI_SERVICES_GETSUPPORTEDSERVICES =0;
  static final public int CAPI_SERVICES_LISTEN               =1;
  static final public int CAPI_SERVICES_HOLD                 =2;
  static final public int CAPI_SERVICES_RETRIEVE             =3;
  static final public int CAPI_SERVICES_SUSPEND              =4;
  static final public int CAPI_SERVICES_RESUME               =5;
  static final public int CAPI_SERVICES_ECT                  =6;
  static final public int CAPI_SERVICES_PTY3BEGIN            =7;
  static final public int CAPI_SERVICES_PTY3END              =8;

  static final public int CAPI_SERVICES_CF_ACTIVATE              =0x0009;   // call forward
  static final public int CAPI_SERVICES_CF_DEACTIVATE            =0x000A;   // call forward
  static final public int CAPI_SERVICES_CF_INTERROGATEPARAMETERS =0x000B;   // call forward
  static final public int CAPI_SERVICES_CF_INTERROGATENUMBERS    =0x000C;   // call forward

  static final public int CAPI_SERVICES_CD                       =0x0D;     // call deflection

  static public final String capiSpeechCodingID="capi.speechcoding";
  static public final String capiControllerID="capi.controller.id";
  static public final String capiMaxLogicalConnectionsID="capi.maxlogicalcon";

  static final public int CAPI_B1PROTOCOL_ATM                =(1<<28);
  static final public int CAPI_B2PROTOCOL_PPPOE              =(1<<30);
  static final public int CAPI_B3PROTOCOL_PPPOE              =(1<<30);
}