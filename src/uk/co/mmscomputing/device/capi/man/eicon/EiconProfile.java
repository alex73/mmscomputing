package uk.co.mmscomputing.device.capi.man.eicon;

import javax.swing.tree.*;

import uk.co.mmscomputing.device.capi.*;

public class EiconProfile extends CapiProfile{

  private int specOptions;

  static final public String[] specOptionStrings={
    "","","","","",
    "Extended DTMF tone detection and generation support",          // bit 5
    "Extended Fax paper formats and resolution support",            // bit 6
    "Extended modem configuration support",                         // bit 7
    "","","","","","","","","","","","","","","","","","","","","","","",""
  };

  public EiconProfile(byte[] profile){
    super(profile);
    specOptions = (profile[44]&0x00FF)|((profile[45]&0x00FF)<<8)|((profile[46]&0x00FF)<<16)|((profile[47]&0x00FF)<<24);
  }

//  public boolean isSupportingBroadband(){return checkBit(getOptions(),8);}         // [eicon/sdk/Doc/CxEcho.pdf]
//  public boolean isSupportingEchoCancellation(){return checkBit(getOptions(),9);}  // eicon/sdk/Doc/CxEcho.pdf

  public boolean isSupportingExtendedDTMF(){return checkBit(specOptions,5);}               // eicon/sdk/Doc/CxDtmf.pdf
  public boolean isSupportingExtendedFaxFormat(){return checkBit(specOptions,6);}          // eicon/sdk/Doc/CxFax.pdf
  public boolean isSupportingExtendedModemConfiguration(){return checkBit(specOptions,7);} // eicon/sdk/Doc/CxModem.pdf

  public String toString(){
    String s=super.toString();

    s+="\nEicon Spec Options = 0"+Integer.toBinaryString(specOptions)+"b\n";

    for(int i=0;i<specOptionStrings.length;i++){
      if(checkBit(specOptions,i)){ 
        s+="Bit["+i+"] - "+specOptionStrings[i]+"\n";
      }
    }
    return s;
  }

  public DefaultMutableTreeNode toTree(){
    DefaultMutableTreeNode category;
    DefaultMutableTreeNode top = super.toTree();

    category = new DefaultMutableTreeNode("Eicon Specific Options : 0"+Integer.toBinaryString(specOptions)+"b");
    top.add(category);

    for(int i=0;i<specOptionStrings.length;i++){
      if(checkBit(specOptions,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+specOptionStrings[i]));
      }
    }

    return top;
  }
}
