package uk.co.mmscomputing.device.capi;

import javax.swing.tree.*;

public class CapiProfile{

  static final public String[] optionStrings={
    "Internal controller",
    "External equipment",
    "Handset",
    "DTMF",
    "Supplementary Services",
    "Channel allocation",
    "Parameter B channel operation",
    "Line interconnect",
    "Broadband extensions (Eicon only ?)",
    "Echo cancellation (Eicon only ?)",                               // bit 9
    "","","","","","","","","","","","","","","","","","","","","",""
  };

  static final public String[] b1protocolStrings={
    "64 kbits/s with HDLC framing",
    "64 kbits/s bit-transparent",
    "V.110 asynchronous",
    "V.110 synchronous",
    "T.30 modem for Group 3 fax",
    "64 kbits/s inverted with HDLC framing",
    "56 kbits/s bit-transparent",
    "modem with all negotiations",
    "modem asynchronous",
    "modem synchronous",                                              // bit 9
    "","","","","","","","","","","","","","","","","","",
    "DSL:ATM (AVM only ?)",                                           // bit 28
    "","",""
  };

  static final public String[] b2protocolStrings={
    "ISO 7776 (X.75 SLP)",
    "Transparent",
    "SDLC",
    "LAPD in accordance with Q.921 for D-channel X.25 (SAPI 16)",
    "T.30 for Group 3 fax",
    "Point to point (PPP)",
    "Transparent (ignoring framing errors of B1 protocol)",
    "Modem error correction and compression (V.42bis or MNP5)",
    "ISO 7776 (X.75 SLP) modified supporting V.42bis compression",
    "V.120 asynchronous mode",
    "V.120 asynchronous mode supporting V.42bis",
    "V.120 bit-transparent mode",
    "LAPD in accordance with Q.921 including free SAPI selection",    // bit 12
    "","","","","","","","","","","","","","","","","",
    "DSL:PPPOE (AVM only ?)",                                         // bit 30
    ""
  };

  static final public String[] b3protocolStrings={
    "Transparent",
    "T.90NL",
    "ISO 8208 (X.25 DTE-DTE)",
    "X.25 DCE",
    "T.30 for Group 3 fax",
    "T.30 for Group 3 fax with extensions",
    "",
    "Modem",                                                          // bit 7
    "","","","","","","","","","","","","","","","","","","","","","",
    "DSL:PPPOE (AVM only ?)",                                         // bit 30
    ""
  };

  private int noc=0;

  private int noBChannels=-1;

  private int options=0;
  private int b1protocols=0;
  private int b2protocols=0;
  private int b3protocols=0;

  public CapiProfile(byte[] profile){
    noc         = (profile[ 0]&0x00FF)|((profile[ 1]&0x00FF)<<8);
    noBChannels = (profile[ 2]&0x00FF)|((profile[ 3]&0x00FF)<<8);
    options     = (profile[ 4]&0x00FF)|((profile[ 5]&0x00FF)<<8)|((profile[ 6]&0x00FF)<<16)|((profile[ 7]&0x00FF)<<24);
    b1protocols = (profile[ 8]&0x00FF)|((profile[ 9]&0x00FF)<<8)|((profile[10]&0x00FF)<<16)|((profile[11]&0x00FF)<<24);
    b2protocols = (profile[12]&0x00FF)|((profile[13]&0x00FF)<<8)|((profile[14]&0x00FF)<<16)|((profile[15]&0x00FF)<<24);
    b3protocols = (profile[16]&0x00FF)|((profile[17]&0x00FF)<<8)|((profile[18]&0x00FF)<<16)|((profile[19]&0x00FF)<<24);
  }

  public int getNoOfBChannels(){ return noBChannels;}

  protected boolean checkBit(int bits, int bit){ return ((bits&(1<<bit))!=0); }

  public boolean isSupportingDTMF(){
    return checkBit(options,3);
  }

  public boolean isSupportingGroup3Fax(){
    return checkBit(b1protocols,4)&&checkBit(b2protocols,4)&&checkBit(b3protocols,4);
  }

  public boolean isSupportingSupplementaryServices(){
    return checkBit(options,4);
  }

  public int getOptions(){ return options;}
  public int getB1Protocols(){ return b1protocols;}
  public int getB2Protocols(){ return b2protocols;}
  public int getB3Protocols(){ return b3protocols;}

  public String toString(){
    String s="";

    s+="BChannels : "+noBChannels+"\n";
    s+="\n"+getGlobalOptions();
    s+="\n"+getB1ProtocolSupport();
    s+="\n"+getB2ProtocolSupport();
    s+="\n"+getB3ProtocolSupport();

    return s;
  }

  private String getGlobalOptions(){
    String s="Options : 0"+Integer.toBinaryString(options)+"b\n";
    for(int i=0;i<optionStrings.length;i++){
      if(checkBit(options,i)){ 
        s+="Bit["+i+"] - "+optionStrings[i]+"\n";
      }
    }
    return s;
  }

  public boolean isSupportingBroadband(){return checkBit(options,8);}         // [eicon/sdk/Doc/CxEcho.pdf]
  public boolean isSupportingEchoCancellation(){return checkBit(options,9);}  // eicon/sdk/Doc/CxEcho.pdf

  private String getB1ProtocolSupport(){
    String s="B1 protocol support : 0"+Integer.toBinaryString(b1protocols)+"b\n";
    for(int i=0;i<b1protocolStrings.length;i++){
      if(checkBit(b1protocols,i)){ 
        s+="Bit["+i+"] - "+b1protocolStrings[i]+"\n";
      }
    }
    return s;
  }

  private String getB2ProtocolSupport(){
    String s="B2 protocol support : 0"+Integer.toBinaryString(b2protocols)+"b\n";
    for(int i=0;i<b2protocolStrings.length;i++){
      if(checkBit(b2protocols,i)){ 
        s+="Bit["+i+"] - "+b2protocolStrings[i]+"\n";
      }
    }
    return s;
  }

  private String getB3ProtocolSupport(){
    String s="B3 protocol support : 0"+Integer.toBinaryString(b3protocols)+"b\n";
    for(int i=0;i<b3protocolStrings.length;i++){
      if(checkBit(b3protocols,i)){ 
        s+="Bit["+i+"] - "+b3protocolStrings[i]+"\n";
      }
    }
    return s;
  }

  public DefaultMutableTreeNode toTree(){
    DefaultMutableTreeNode category;
    DefaultMutableTreeNode top = new DefaultMutableTreeNode("capi profile");

    top.add(new DefaultMutableTreeNode("Number of B-Channels : "+noBChannels));

    category = new DefaultMutableTreeNode("options : 0"+Integer.toBinaryString(options)+"b");
    top.add(category);

    for(int i=0;i<optionStrings.length;i++){
      if(checkBit(options,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+optionStrings[i]));
      }
    }

    category = new DefaultMutableTreeNode("B1 protocol support : 0"+Integer.toBinaryString(b1protocols)+"b");
    top.add(category);

    for(int i=0;i<b1protocolStrings.length;i++){
      if(checkBit(b1protocols,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+b1protocolStrings[i]));
      }
    }

    category = new DefaultMutableTreeNode("B2 protocol support : 0"+Integer.toBinaryString(b2protocols)+"b");
    top.add(category);

    for(int i=0;i<b2protocolStrings.length;i++){
      if(checkBit(b2protocols,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+b2protocolStrings[i]));
      }
    }

    category = new DefaultMutableTreeNode("B3 protocol support : 0"+Integer.toBinaryString(b3protocols)+"b");
    top.add(category);

    for(int i=0;i<b3protocolStrings.length;i++){
      if(checkBit(b3protocols,i)){ 
        category.add(new DefaultMutableTreeNode("Bit["+i+"] - "+b3protocolStrings[i]));
      }
    }
    return top;
  }
}

