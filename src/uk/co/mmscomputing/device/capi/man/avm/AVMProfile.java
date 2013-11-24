package uk.co.mmscomputing.device.capi.man.avm;

import javax.swing.tree.*;

import uk.co.mmscomputing.device.capi.*;

public class AVMProfile extends CapiProfile implements AVMConstants{

  private int dProtocol=0;
  private int line=0;

  public AVMProfile(byte[] profile){
    super(profile);

    if(profile[44]==1){
      dProtocol = (profile[44+3]&0x00FF)|((profile[44+4]&0x00FF)<<8);
      line      = (profile[44+5]&0x00FF);
    }
  }

  public String toString(){
    String s=super.toString();

    s+="\nAVM specific data:\n"; 
    s+="D channel protocol : 0"+Integer.toBinaryString(dProtocol)+"b\n";
    for(int i=0;i<dProtocolStrings.length;i++){
      if(checkBit(dProtocol,i)){
        s+="Bit["+i+"] - "+dProtocolStrings[i]+"\n";
      }
    }

    s+="Type of line : 0"+Integer.toBinaryString(line)+"b\n";
    for(int i=0;i<lineStrings.length;i++){
      if(checkBit(line,i)){
        s+="Bit["+i+"] - "+lineStrings[i]+"\n";
      }
    }

    return s;
  }

  public DefaultMutableTreeNode toTree(){
    DefaultMutableTreeNode category;
    DefaultMutableTreeNode top = super.toTree();

    category = new DefaultMutableTreeNode("D channel protocol : 0"+Integer.toBinaryString(dProtocol)+"b");
    top.add(category);

    for(int i=0;i<dProtocolStrings.length;i++){
      if(checkBit(dProtocol,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+dProtocolStrings[i]));
      }
    }

    category = new DefaultMutableTreeNode("Type of line : 0"+Integer.toBinaryString(line)+"b");
    top.add(category);

    for(int i=0;i<lineStrings.length;i++){
      if(checkBit(line,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+lineStrings[i]));
      }
    }


    return top;
  }
}
