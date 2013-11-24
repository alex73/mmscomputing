package uk.co.mmscomputing.device.phone;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.util.*;

public class PhonePropertiesPanel extends JPanel implements PhoneConstants{

  public PhonePropertiesPanel(Properties properties){
    this(true,true,properties);
  }

  public PhonePropertiesPanel(boolean receiving, boolean calling,Properties properties){
    Border border=BorderFactory.createLineBorder(Color.black);

    setLayout(new BorderLayout());
    JPanel q=new JPanel();
    add(q,BorderLayout.NORTH);
    q.setLayout(new BoxLayout(q,BoxLayout.PAGE_AXIS));

    TitledBorder title;

    if(calling){
      JPanel q1=new JPanel();
      q1.setLayout(new GridLayout(0,2));
      title = BorderFactory.createTitledBorder(border, "Calling");
      title.setTitleJustification(TitledBorder.CENTER);
      q1.setBorder(title);
      new UtilTextField(q1,properties,phoneRemoteNoID,"Number to Call","Remote Number",32);
      new UtilTextField(q1,properties,phoneLocalNoID,"++44 1234 5678","Local Number",32);
      new UtilTextField(q1,properties,phoneTimeOutID,"60","Connection Time-Out [secs]",32);
      q.add(q1);
    }
    if(receiving){
      JPanel q1=new JPanel();
      q1.setLayout(new GridLayout(0,2));
      title = BorderFactory.createTitledBorder(border, "Receiving");
      title.setTitleJustification(TitledBorder.CENTER);
      q1.setBorder(title);

      new UtilTextField(q1,properties,phoneLocalNosID,"","Numbers To Pick-Up [comma separated]",64);
      new UtilTextField(q1,properties,phoneLawID,"true","A-Law",32);
      new UtilTextField(q1,properties,phoneLocalNoID,"My Local No","Local Number",32);
      new UtilTextField(q1,properties,phoneTimeToWaitID,"20","Time to wait in secs",32);
      new UtilTextField(q1,properties,phoneTimeToRecordID,"60","Time to record in secs",32);

      properties.setProperty(phoneFileDirID,properties.getProperty(phoneFileDirID,phoneDefaultPath));
      new UtilTextField(q1,properties,phoneFileDirID,properties.getProperty(phoneFileDirID),"File Path",256);

      new UtilTextField(q1,properties,phoneStartMsgID,"uk/co/mmscomputing/sounds/startmsg.wav","Start Message",256);
      new UtilTextField(q1,properties,phoneEndMsgID,"uk/co/mmscomputing/sounds/endmsg.wav","End of Call Message",256);

      q.add(q1);
    }
  }
}
