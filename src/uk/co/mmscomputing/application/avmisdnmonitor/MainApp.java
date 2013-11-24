package uk.co.mmscomputing.application.avmisdnmonitor;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import uk.co.mmscomputing.concurrent.*;
import uk.co.mmscomputing.util.*;

import uk.co.mmscomputing.device.capi.*;
import uk.co.mmscomputing.device.capi.facility.*;
import uk.co.mmscomputing.device.capi.man.avm.*;

public class MainApp extends UtilMainApp implements Runnable{

  // inspired by: avm/kisdnwatch-01.00.08/kavmmon/ccapiinfo.h and ccapiinfo.cpp

  private int       appid    = -1;
  private int       cntlid   = 1;
  private int       noc      = 0;
  private boolean   running  = false;

  private JPanel    panel,profilePanel;

  private JLabel    registeredApplications;
  private JLabel    isdnLine;
  private JLabel    dChannel;
  private JLabel[]  bChannels;
  
  private int       ra=-1;
  private boolean   il=false,dc=false;
  private boolean[] bcs=null;

  public MainApp(){super();}

  public MainApp(String title, String[] argv){
    super(title,argv);
  }

  protected JPanel getCenterPanel(Properties properties)throws Exception{

    registeredApplications = new JLabel();
    isdnLine               = new JLabel();
    dChannel               = new JLabel();

    JPanel p=new JPanel(new GridLayout(0,2));

    p.add(new JLabel("Registered Applications:"));
    p.add(registeredApplications);

    p.add(new JLabel("ISDN-Line :"));
    p.add(isdnLine);

    p.add(new JLabel("D-Channel :"));
    p.add(dChannel);

    panel = p;

    JPanel q=new JPanel(new BorderLayout());
    q.setBorder(new EmptyBorder(10,10,10,10));
    q.add(p,BorderLayout.NORTH);

    p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(q),BorderLayout.CENTER);

    JTabbedPane tp=new JTabbedPane();
    tp.addTab("Monitor",p);

    profilePanel=new JPanel(new BorderLayout());
    tp.addTab("Profile",profilePanel);

    p = new JPanel(new BorderLayout());
    p.add(tp);    

    return p;
  }

  protected void setFrameSize(JFrame frame, Rectangle bounds){
    frame.setSize(450,250);
  }

  private void createProfileAndBChannelIndicators(int appid,int cntlid)throws Exception{
    CapiProfile profile=new AVMProfile(jcapi.getProfile(cntlid));
    DefaultMutableTreeNode rootNode=profile.toTree();

    if(profile.isSupportingSupplementaryServices()){
      jcapi.putMessage(appid,new SupServiceReq.GetSupportedServicesReq(appid,cntlid).getBytes());
      jcapi.waitForMessage(appid);
      MsgIn msg=MsgIn.create(jcapi.getMessage(appid,null));
      if( (msg instanceof SupServiceConf.GetSupportedServicesConf)
      &&  (((CapiConfMsg)msg).getInfo()==0)
      ){
        rootNode.add(((SupServiceConf.GetSupportedServicesConf)msg).toTree());
      }
    }
    
    JTree tree = new JTree(rootNode);
    JScrollPane treeView = new JScrollPane(tree);
    profilePanel.add(treeView);

    noc=profile.getNoOfBChannels();

    bChannels  = new JLabel[noc];
    bcs        = new boolean[noc];

    for(int i=0;i<noc;i++){
      panel.add(new JLabel("B-Channel ["+i+"]:"));
      bChannels[i]=new JLabel();
      panel.add(bChannels[i]);
    }
  }

  public void start(){
    super.start();
    new Thread(this).start();
  }

  public void stop(){
    running=false;
    super.stop();
  }

  protected void signalRegisteredApplicationChange(int state){
    System.out.println("Applications "+state);
  }

  protected void signalISDNLineStateChange(boolean state){
    System.out.println("ISDN-Line "+((state)?"UP":"down"));
  }

  protected void signalDChannelStateChange(boolean state){
    System.out.println("D-Channel "+((state)?"UP":"down"));
  }

  protected void signalBChannelStateChange(int bChannel, boolean state){
    System.out.println("B-Channel ["+bChannel+"] "+((state)?"UP":"down"));
  }

  protected void handleMessage(MsgIn msg){
    if(msg instanceof AVMGetBChannelInfoConf){
      AVMGetBChannelInfoConf avmmsg=(AVMGetBChannelInfoConf)msg;

      int nra=avmmsg.getRegisteredApplicationCount();
      registeredApplications.setText(""+nra);
      if(ra!=nra){ra=nra;signalRegisteredApplicationChange(ra);}

      boolean nil=avmmsg.isISDNLineActive();
      isdnLine.setText((nil)?"ACTIVE":"inactive");
      if(il!=nil){il=nil;signalISDNLineStateChange(il);}

      boolean ndc=avmmsg.isDChannelActive();
      dChannel.setText((ndc)?"ACTIVE":"inactive");
      if(dc!=ndc){dc=ndc;signalDChannelStateChange(dc);}

      for(int i=0;i<noc;i++){
        boolean nbc=avmmsg.isBChannelActive(i);
        bChannels[i].setText((nbc)?"ACTIVE":"inactive");
        if(bcs[i]!=nbc){bcs[i]=nbc;signalBChannelStateChange(i,nbc);}
      }
    }else{
      System.out.println(msg);
    }
  }

  public void run(){
    try{
      jcapi.checkInstalled();

      String name=jcapi.getManufacturer(cntlid).toLowerCase();
      if(name.indexOf("avm")>=0){
        appid=jcapi.register(cntlid,2,128);
        try{
          running=true;
          createProfileAndBChannelIndicators(appid,cntlid);
          Semaphore s=new Semaphore(0,true);

          byte[] buf=null;
          while(running){
            AVMGetBChannelInfoReq req=new AVMGetBChannelInfoReq(appid,cntlid);
            jcapi.putMessage(appid,req.getBytes());
            jcapi.waitForMessage(appid);
            if(!running){break;}
            buf=jcapi.getMessage(appid,buf);
            MsgIn msg=MsgIn.create(buf);
            handleMessage(msg);
            s.tryAcquire(500,TimeUnit.MILLISECONDS);          
          }
        }finally{
          jcapi.release(appid);
        }
      }else{
        System.out.println("9\bSorry, this program works only with AVM ISDN cards!");
      }
    }catch(InterruptedException e){
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".run:\n\t"+e);
      System.err.println(getClass().getName()+".run:\n\t"+e);
      e.printStackTrace();
    }
  }

  public static void main(String[] argv){
    try{
      new MainApp("ISDN Monitor for AVM ISDN Cards [2006-05-05]", argv);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}