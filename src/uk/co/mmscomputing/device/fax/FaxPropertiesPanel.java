package uk.co.mmscomputing.device.fax;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.util.*;

public class FaxPropertiesPanel extends JPanel implements FaxConstants{

  public FaxPropertiesPanel(Properties properties){
    this(true,true,properties);
  }

  public FaxPropertiesPanel(boolean receiving, boolean sending,Properties properties){
    Border border=BorderFactory.createLineBorder(Color.black);

    setLayout(new BorderLayout());
    JPanel q=new JPanel();
    add(q,BorderLayout.NORTH);
    q.setLayout(new BoxLayout(q,BoxLayout.PAGE_AXIS));

    TitledBorder title;

    if(sending){
      JPanel q1=new JPanel();
      q1.setLayout(new GridLayout(0,2));
      title = BorderFactory.createTitledBorder(border, "Sending");
      title.setTitleJustification(TitledBorder.CENTER);
      q1.setBorder(title);
      new UtilTextField(q1,properties,faxRemoteNoID,"Number to Call","Remote Number",32);
      new UtilTextField(q1,properties,faxHeaderID,"My Fax Header Line","Fax Header",64);
      new UtilTextField(q1,properties,faxLocalNoID,"++44 1234 5678","Local Number",32);
      new UtilTextField(q1,properties,faxTimeOutID,"60","Connection Time-Out [secs]",32);
      properties.setProperty(faxSenderFileDirID,properties.getProperty(faxSenderFileDirID,faxDefaultPath));
      new UtilTextField(q1,properties,faxSenderFileDirID,properties.getProperty(faxSenderFileDirID),"Sender File Path",256);
      q.add(q1);
    }
    if(receiving){
      JPanel q2=new JPanel();
      q2.setLayout(new GridLayout(0,2));
      title = BorderFactory.createTitledBorder(border, "Receiving");
      title.setTitleJustification(TitledBorder.CENTER);
      q2.setBorder(title);
      new UtilTextField(q2,properties,faxLocalNosID,"","Numbers To Pick-Up [comma separated]",64);
      new UtilTextField(q2,properties,faxTimeToWaitID,"0","Pick-up time [secs]",32);
      new UtilTextField(q2,properties,faxMaxIllegalLineCodingsID,"-1","Maximal illegal line codings [ignore=-1]",32);
      new UtilTextField(q2,properties,faxFileTypeID,"sff","File format type [sff|tif]",32);
      properties.setProperty(faxReceiverFileDirID,properties.getProperty(faxReceiverFileDirID,faxDefaultPath));
      new UtilTextField(q2,properties,faxReceiverFileDirID,properties.getProperty(faxReceiverFileDirID),"Receiver File Path",256);
      q.add(q2);
    }
  }
}