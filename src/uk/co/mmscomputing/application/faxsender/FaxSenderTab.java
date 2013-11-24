package uk.co.mmscomputing.application.faxsender;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.text.*;

import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.util.JarImageIcon;

import uk.co.mmscomputing.application.imageviewer.*;

/*
  Use ghostscript to convert *.ps or *.pdf file to *.sff file

  gs -q -dNOPAUSE -dBATCH -r200 -sOutputFile=test1.sff -sDEVICE=cfax test.ps

*/

public class FaxSenderTab extends ScannerTab implements FaxSenderListener{

  public FaxSenderTab(Properties properties){
    super(properties);
  }

  protected void setButtonPanel(JPanel gui){
    super.setButtonPanel(gui);

    JPanel buttonPanel=new JPanel();
    buttonPanel.setLayout(new GridLayout(0,1));
    buttonPanel.add(new JButton(getScaleAction()));
    gui.add(buttonPanel);

    FaxSender faxsender=FaxSender.getDevice();
    if(faxsender!=null){
      faxsender.addListener(this);
      gui.add(faxsender.getGUI(properties));
    }

  }

  public Action getScaleAction(){
    return new AbstractAction("<html><center>convert to <br><b>Fax Format</b></center></html>"){
      public void actionPerformed(ActionEvent ev){
        scale();
      }
    };
  }

  public void scale(){
    for(int i=0; i<images.getTabCount(); i++){
      JScrollPane sp=(JScrollPane)images.getComponentAt(i);
      ImagePanel ip=(ImagePanel)sp.getViewport().getView();
      BufferedImage original=ip.getImage();
/*
	Width

	G3 FAX	1728 * 2256	A4
	B4	2048
	A3	2432
*/
      if(1728<original.getWidth()){                    // if original is wider than A4 width: scale it down
        double sf=1728.0/original.getWidth();
        BufferedImage image=new BufferedImage(1728,(int)(original.getHeight()*sf),BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g=image.createGraphics();
        AffineTransform t=new AffineTransform();
        t.scale(sf,sf);
        g.drawRenderedImage(original,t);
        ip.setImage(image);
      }else if(original.getWidth()<1728){              // if smaller just copy
        BufferedImage image=new BufferedImage(1728,original.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g=image.createGraphics();
        AffineTransform t=new AffineTransform();
        g.setPaint(Color.white);
        g.fill(new Rectangle(0,0,image.getWidth(),image.getHeight()));
        g.drawRenderedImage(original,t);
        ip.setImage(image);
      }else if(original.getType()!=BufferedImage.TYPE_BYTE_BINARY){
        BufferedImage image=new BufferedImage(1728,original.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g=image.createGraphics();
        AffineTransform t=new AffineTransform();
        g.drawRenderedImage(original,t);
        ip.setImage(image);
      }
      ip.revalidate();ip.repaint();
    }
  }

  public void update(FaxSenderMetadata.Type type, final FaxSenderMetadata md){
    if(type.equals(FaxSenderMetadata.GETFILE)){           // application needs to set metadata
      md.setFile(getFile());                              // get name of file we want to fax
      md.setHeader(properties.getProperty(FaxConstants.faxHeaderID));
      md.setLocalNo(properties.getProperty(FaxConstants.faxLocalNoID));
      md.setRemoteNo(properties.getProperty(FaxConstants.faxRemoteNoID));
      int time=Integer.valueOf(properties.getProperty(FaxConstants.faxTimeOutID)).intValue();
      md.setTimeOut(time);
      md.progressmonitor=true;                            // want a progress monitor (opens only once data will be send)
    }else if(type.equals(FaxSenderMetadata.STATE)){       // in what state are we
      int state=md.getState();
      switch(state){
      case FaxConstants.faxRinging:
        System.out.println("Ringing ... "+md.getRemoteNo());
        break;
      case FaxConstants.faxConnected:
        System.out.println("Sending ... "+md.getRemoteNo());
        break;
      case FaxConstants.faxDisconnected:
        System.out.println("Disconnected ... "+md.getRemoteNo());
        break;
      }
    }else if(type.equals(FaxSenderMetadata.INFO)){
      System.out.println(md.getInfo());
    }else if(type.equals(FaxSenderMetadata.EXCEPTION)){
      System.out.println("9\b"+md.getException().getMessage());
    }
  }

  static final protected SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

  private String getFile(){
    try{
      String date     = sdf.format(Calendar.getInstance().getTime());
      String sendDir  = properties.getProperty(FaxConstants.faxSenderFileDirID);
      String remoteno = properties.getProperty(FaxConstants.faxRemoteNoID);
      String rno      = remoteno.replace('*','0').replace('#','0');  // PBX
      String srcfile  = sendDir+File.separator+date+rno+".sff";

      properties.setProperty(FaxConstants.faxFileID,srcfile);
      new File(sendDir).mkdirs();
      save(srcfile);
      return srcfile;
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".sendFax:\n\t"+e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
}
