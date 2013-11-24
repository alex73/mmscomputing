package uk.co.mmscomputing.device.capi.protocol;

import uk.co.mmscomputing.device.capi.StructOut;

public class FaxBProtocol extends BProtocol{

  public FaxBProtocol(StructOut b3conf){
    super(4,4,4,StructOut.empty,StructOut.empty,b3conf,StructOut.empty);
  }

  public FaxBProtocol(
    int res,			  //	fax resolution
    int format,			//	format i.e. sff=0, tiff=4
    String id,			//	station id
    String hl			  //	head line
  ){
    this(getB3Conf(res,format,id,hl));
  }


  static public StructOut getB3Conf(
    int res,			  //	fax resolution
    int format,			//	format i.e. sff=0, tiff=4
    String id,			//	station id
    String hl			  //	head line
  ){
    StructOut sid=new StructOut(id);
    StructOut shl=new StructOut(hl);
    StructOut s  =new StructOut(4+sid.getLength()+shl.getLength());
    s.writeWord(res);
    s.writeWord(format);
    s.writeStruct(sid);	//	'+',' ',digits
    s.writeStruct(shl);
    return s;    
  }

}
