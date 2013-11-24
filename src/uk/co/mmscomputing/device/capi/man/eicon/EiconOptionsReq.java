package uk.co.mmscomputing.device.capi.man.eicon;

import uk.co.mmscomputing.device.capi.*;

public class EiconOptionsReq extends EiconReq{

  //  bit   |  option    |  comment
  //  5     |  dtmf      |  switch on proprietary DTMF Facility Req/Conf/Ind (eicon/sdk/Doc/CxDtmf.pdf)
  //  6     |  fax paper |  switch on proprietary Fax Paper Format and Resolution (eicon/sdk/Doc/CxFax.pdf)


  public EiconOptionsReq(int appid,int cntl,int optionsMask){   // to switch on options  use 1 in bitmask
    super(                                                      // to switch off options use 0 in bitmask
      appid,
      cntl,
      CAPI_EICON_OPTIONS,
      getCmdParameter(optionsMask)
    );
  }

  static private StructOut getCmdParameter(int optionsMask){
    StructOut s=new StructOut(4);
    s.writeDWord(optionsMask);
    return s;
  }
 
  static public class DTMF extends EiconOptionsReq{             
    public DTMF(int appid,int cntl){super(appid,cntl,(1<<5));}
  }

  static public class FaxFormatReq extends EiconOptionsReq{             
    public FaxFormatReq(int appid,int cntl){super(appid,cntl,(1<<6));}
  }
}