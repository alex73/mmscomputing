package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class V42InfoConf extends FacilityConf{

  private int mode, noOfCodeWords,maxStringSize;
  private int txTotal,txCompressed,rxTotal,rxDecompressed;

  public V42InfoConf(Rider r){
    super(r);
    if(info!=0){return;}
    try{
      info=r.readWord();                    // information available
      if(info==0){
        mode=r.readWord();                  // compression mode
        if(mode==1){                        // V.42 bis
          noOfCodeWords=r.readWord();       // number of code words
          maxStringSize=r.readWord();       // max String Size

          txTotal=r.readDWord();            // number of octets transmitted
          txCompressed=r.readDWord();       // number of octets transmitted after compression

          rxTotal=r.readDWord();            // number of octets received
          rxDecompressed=r.readDWord();     // number of octets received after decompression
        }
      }
    }catch(IndexOutOfBoundsException ioobe){
      ioobe.printStackTrace();
    }    
  }

  public int getInfo(){return info;}

  public String getInformation(){
    if(info!=0){
      return "No Information about V42 bis compression available.";
    }
    String s="";
    if(mode==0){
      s+="No compression\n";
    }else if(mode==1){
      s+="V.42 bis compression mode\n";
      s+="Number of code words                           :"+noOfCodeWords+"\n";
      s+="Max String Length                              :"+maxStringSize+"\n";
      s+="Number of octets transmitted                   :"+txTotal+"\n";
      s+="Number of octets transmitted after compression :"+txCompressed+"\n";
      s+="Number of octets received                      :"+rxTotal+"\n";
      s+="Number of octets received after decompression  :"+rxDecompressed+"\n";
    }
    return s;
  }

  public String toString(){
    String s=super.toString();
    s+="info : "+getInformation()+"\n\t";
    return s;
  }

}

