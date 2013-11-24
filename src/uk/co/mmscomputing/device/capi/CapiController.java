package uk.co.mmscomputing.device.capi;

import java.io.*;
import uk.co.mmscomputing.util.metadata.*;
import uk.co.mmscomputing.device.capi.exception.*;

import uk.co.mmscomputing.device.capi.man.*;

public class CapiController{

  private int         ctrlid=-1;

  private String      manufacturer="";
  private int         manuid=0;

  private CapiProfile profile;

  CapiController(int id)throws IOException{
    ctrlid=id;	                                                 // capi starts counting from 1

    manufacturer=jcapi.getManufacturer(ctrlid);
    if(manufacturer.toLowerCase().indexOf("avm")>=0){
      manuid=ManufacturerConstants.CAPI_MANUID_AVM;
      profile=new uk.co.mmscomputing.device.capi.man.avm.AVMProfile(jcapi.getProfile(ctrlid));
    }else if(manufacturer.toLowerCase().indexOf("eicon")>=0){
      manuid=ManufacturerConstants.CAPI_MANUID_EICON;
      profile=new uk.co.mmscomputing.device.capi.man.eicon.EiconProfile(jcapi.getProfile(ctrlid));
    }else{
      profile=new CapiProfile(jcapi.getProfile(ctrlid));
    }
  }

  public int getNoOfBChannels(){ return profile.getNoOfBChannels();}

  public void update(Metadata md){
    md.putInt("capi.controller.id",ctrlid);
    md.putBoolean("capi.controller.isSupportingDTMF",profile.isSupportingDTMF());
    md.putBoolean("capi.controller.isSupportingGroup3Fax",profile.isSupportingGroup3Fax());
  }

  public String getManufacturer(){return manufacturer;}
  public CapiProfile getProfile(){return profile;}

  public boolean isSupportingDTMF(){return profile.isSupportingDTMF();}
  public boolean isSupportingGroup3Fax(){return profile.isSupportingGroup3Fax();}
  public boolean isSupportingSupplementaryServices(){return profile.isSupportingSupplementaryServices();}

  public int getId(){ return ctrlid;}
  public int getOptions(){ return profile.getOptions();}
  public int getB1Protocols(){ return profile.getB1Protocols();}
  public int getB2Protocols(){ return profile.getB2Protocols();}
  public int getB3Protocols(){ return profile.getB3Protocols();}

  public String getName(){
    /*
    This works only with Linux-CAPI as expected.
    Windows XP does not differentiate between the controllers. It will give you always the CAPI2032.DLL details [ctrlid=0].
    */

    String s=""+ctrlid;
    try{
      int[] version=jcapi.getVersion(ctrlid);
      s+=" "+manufacturer;
      try{
        s+=" ["+jcapi.getSerialNumber(ctrlid)+"]";
      }catch(CapiIOException cioe){
        System.err.println(cioe);
      }    
      s+=" V: "+version[0]+"."+version[1]+" - "+version[2]+"."+version[3]+"\n";
    }catch(CapiIOException cioe){
      System.err.println(cioe);
    }    
    return s;
  }

  public String toString(){
    String s="Controller : "+ctrlid+"\n";

    try{
      int[] version=jcapi.getVersion(ctrlid);

      s+="Manufacturer "+manufacturer+" [0x"+Integer.toHexString(manuid)+"]\n";
      s+="Version "+version[0]+"."+version[1]+" - "+version[2]+"."+version[3]+"\n";
      s+="Serial No "+jcapi.getSerialNumber(ctrlid)+"\n";

    }catch(CapiIOException cioe){
      System.err.println(cioe);
    }
    s+="\n"+profile.toString();
    return s;
  }
}