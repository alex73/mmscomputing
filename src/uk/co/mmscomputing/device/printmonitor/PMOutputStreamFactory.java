package uk.co.mmscomputing.device.printmonitor;

import java.io.*;
import javax.swing.*;

public class PMOutputStreamFactory{

  static public final int SFF   = 0;
  static public final int T4MH  = 1;
  static public final int T4MR  = 2;
  static public final int T6MMR = 3;
  static public final int NONE  = 4;

  static private JFileChooser savefc=null;

  static public OutputStream getOutputStream(int mode,String documentspath,String document)throws IOException{
    File dir=new File(documentspath);dir.mkdirs();
    JFileChooser savefc=new JFileChooser(documentspath);
    savefc.setDialogTitle("mmsc-java print port monitor");
    try{
      switch(mode){
      case SFF:    savefc.setSelectedFile(new File(document+".sff"));break;
      case T4MH:    
      case T4MR:    
      case T6MMR:  savefc.setSelectedFile(new File(document+".tif"));break;
      default:     savefc.setSelectedFile(new File(document));break;
      }
    }catch(Exception e){
      savefc.setSelectedFile(new File("printout"));
    }

    int res=savefc.showSaveDialog(null);
    if(res==JFileChooser.APPROVE_OPTION){
      String filepath=savefc.getSelectedFile().getPath();

      switch(mode){
      case SFF:    return new PMSFFOutputStream(filepath);
      case T4MH:   return new PMT4MHOutputStream(filepath);
      case T4MR:   return new PMT4MROutputStream(filepath);
      case T6MMR:  return new PMT6MMROutputStream(filepath);
      default:     return new PMRawOutputStream(filepath);
      }
    }
    return null;
  }
}
