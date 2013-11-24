package uk.co.mmscomputing.device.capi.plc;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.q931.*;

public class InfoInd extends CapiIndMsg{

  protected int    no=0;
  protected String info="";

  public InfoInd(Rider r){
    super(r);
    no=r.readWord();

    if((no&0x00008000)!=0){
       info=Q931Constants.MessageTypeStrs[no&0x00FF];
    }else if((no&0x00004000)!=0){
      info="[Suppl.Info]";
/*
    switch(no&0x000000FF){
    case 0x00:
      info+="Total charges in charge units = %li\n",
        CAPIMsg->info.info_ind.structs[1];
      break;
    case 0x01:
      printf("Total charges in national currency");
      printf("\tCharges          = %li\n",
        CAPIMsg->info.info_ind.structs[1]);
      printf("\tExtended Charges = %li\n",
        CAPIMsg->info.info_ind.structs[5]);
      printf("\tMultiplier       = %i",
        CAPIMsg->info.info_ind.structs[9]);
      //also decode currency sign, ref. ETS 300 182-1, table 2
      break;
    default:
      printf("INFO = %x:",CAPIMsg->info.info_ind.Number);
      for(i=0;i<CAPIMsg->info.info_ind.structs[0];i++)
        printf(" %02x",CAPIMsg->info.info_ind.structs[1+i]);
      printf("\n");
      break;
    }
*/
    }else if((no&0x0000c000)==0){                         //ETSI 300 102, table 4.3
      info=Q931Factory.create(no,r).toString();
    }else{
      info="ERROR: Unmatched 0x"+Integer.toHexString(no);
    }
//    System.err.println(toString());
  }

/*
  public String toString(){
    String s=super.toString();
    s+="info     = "+info+"\n";
    return s;
  }
*/
  public String toString(){return info;}
}

