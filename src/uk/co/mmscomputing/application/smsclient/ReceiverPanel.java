package uk.co.mmscomputing.application.smsclient;

import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.sms.*;
import uk.co.mmscomputing.device.capi.CapiSystem;
import uk.co.mmscomputing.device.capi.CapiSMSPlugin;

public class ReceiverPanel extends JPanel implements SMSConstants,SMSPluginReceiver{

  private File       parent;
  private Properties properties;

  private JTable            table;
  private SMSMsgModel		    model=new SMSMsgModel();
  private SMSMsgColumnModel	columnmodel=new SMSMsgColumnModel();

  public ReceiverPanel(Properties properties)throws IOException{
    this.properties=properties;

    parent = new File(System.getProperty("user.home"),"mmsc"+File.separator+"sms");
    parent.mkdirs();

    CapiSMSPlugin plugin=new CapiSMSPlugin(this);
    CapiSystem.getSystem().addPlugin(plugin);

    table=new JTable(model,columnmodel);
    table.createDefaultColumnsFromModel();
    table.getTableHeader().setReorderingAllowed(false);

    buildCallList();

    table.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.isMetaDown()){
          int selected=table.rowAtPoint(new Point(e.getX(),e.getY()));
          table.setRowSelectionInterval(selected, selected);
          if(e.getClickCount()==2){
            int[] selectedRows=table.getSelectedRows();
            for(int i=selectedRows.length-1; i>=0; i--){
             ((SMSMsgModel)table.getModel()).delEntry(selectedRows[i]);
            }
            table.revalidate();
          }
        }else if(e.getClickCount()==2){
          int selected=table.getSelectedRow();
          if(selected==-1){return;}
          table.setRowSelectionInterval(selected,selected);

          try{
            JTextPane text=new JTextPane();
            text.setPage(new java.net.URL("file:////"+model.getFile(selected)));
            JFrame f=new JFrame();
            f.getContentPane().add(text);
//            f.setLocationRelativeTo(null);
            f.setSize(new Dimension(400,200));
//            f.pack();
            f.setVisible(true);
          }catch(Exception ex){
            ex.printStackTrace();
          }
        }
      }
    });

    setLayout(new BorderLayout());
    add(new JScrollPane(table),BorderLayout.CENTER);
  }

  public void update(SMSMetadata.Type type,SMSMetadata md){    // SMSPluginReceiver
    try{
      if(type.equals(SMSMetadata.STATE)){
        if(md.isState(smsRinging)){
          String pn=getClass().getPackage().getName();
/*
          String reccentre  = properties.getProperty(pn+".receivingservicecentre");
          String subaddress = properties.getProperty(pn+".receivingservicecentre_subadddress");

          String centre=reccentre+subaddress;                  // service centre (SC) number + subaddress
*/
          String centre  = properties.getProperty(pn+".receivingservicecentre");
          String remoteno=md.getRemoteNo();

          if(remoteno.startsWith(centre)){                     // if caller is service centre
            if(remoteno.endsWith("0")                          // 0 or 1 at end accept call; BT-Text always "0"
            || remoteno.endsWith("1")                          // [1] 5.5.6 p.19 ; [2] p.6
            ){                     
              md.setAccept(true);                              // We'll accept this call and let a 'Handler' object deal with it.
            }
          }
        }else if(md.isState(smsConnected)){
          md.setHandler(new Handler());
        }else if(md.isState(smsDisconnected)){
          Handler sh=(Handler)md.getHandler();
          File  file=sh.file;

          System.out.println("New SMS File : "+file.getAbsolutePath());

          model.addEntry(parseFileName(file));
          table.revalidate();
        }
      }
    }catch(Exception e){
      System.out.println(getClass().getName()+".update:\n\t"+e);
      e.printStackTrace();
    }
  }

  static private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS_");

  private class Handler implements SMSReceiver,SMSPluginHandler{

    File   file;
    String number="",time="";

    public void received(SMSDataUnit msg)throws IOException{   // SMSReceiver called from SMSLandLineInputStream
      if(msg instanceof SMSDeliver){
        SMSAddress oa   = (SMSAddress)msg.get("TP-OA");
        number          = oa.getNumber();                      // original address of sender

        SMSTimeStamp ts = (SMSTimeStamp)msg.get("TP-SCTS");
        time=ts.toString();


      }else if(msg instanceof SMSStatusReport){                       
        System.out.println(msg);                       
      }
    }

    public void run(String local,String remote,InputStream pin,final OutputStream pout){  // SMSPluginHandler
      SMSLandLineChannel channel = null;
      PrintStream        out     = null;
      FileOutputStream   fout    = null;
      try{
        String rno=remote.replaceAll("\\*\\*","");             // PBX: internal dialling

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        out  = new PrintStream(baos);

        channel=new SMSLandLineChannel(pout,pin,this);
        channel.send(new SMSDLLEstablished());                 // service centre (SC) called us; SM-TE (us) acknowledge
        InputStream in=channel.getInputStream();               // This is a SMSLandLineInputStream

        int len;byte[] bytes=new byte[256];
        if((len=in.read(bytes))!=-1){                          // Text received via a SMSDataUnit i.e. normally a SMSDeliver object
          String origin="SMS from: "+number+" "+time;

          out.println("<html>");
          out.println("<head>");
          out.println("<title>"+origin+"</title>");
          out.println("</head>");
          out.println("<body><b>"+origin+"<b>");

          do{
            out.println("<p><pre><i>");
            out.write(bytes,0,len);
            out.println("</i></pre></p>");
          }while((len=in.read(bytes))!=-1);                    // Text received via a SMSDataUnit i.e. normally a SMSDeliver object

          out.println("</body>");
          out.println("</html>");
        }
        out.close();


        String fn=sdf.format(new Date())+local+"_"+rno+"_"+number+".html";
        file = new File(parent,fn);
        fout=new FileOutputStream(file);
        fout.write(baos.toByteArray());

      }catch(Exception e){
        System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
        e.printStackTrace();
      }finally{
        try{
          if(fout!=null){fout.close();}
          if(channel!=null){channel.close();}
        }catch(Exception e){
          System.out.println("3\b"+getClass().getName()+".run:\n\t"+e);
        }
      }
    }
  }


  private SMSMsgEntry parseFileName(File file){
    String fn=file.getName();
    fn=fn.substring(0,fn.indexOf('.'));
    String[] content=fn.split("_");
    String date  =(content.length>=1)?content[0]:"unknown";
    String time  =(content.length>=2)?content[1].replace('-',':'):"unknown";
    String local =(content.length>=3)?content[2]:"unknown";
    String remote=(content.length>=4)?content[3]:"withhold";
    String origin=(content.length>=5)?content[4]:"unknown";
    return new SMSMsgEntry(date+" "+time,local,remote,origin,file.getAbsolutePath());
  }

  protected void buildCallList(){

    String[] files=list(parent,new fnf()); 
    for(int i=0; i<files.length; i++){
      File file=new File(parent,files[i]);
      try{
        model.addEntry(parseFileName(file));
      }catch(Exception e){
        System.out.println(getClass().getName()+".parseFileName:\n\t"+e);
        e.printStackTrace();
      }
    }
  }

  private String[] list(File dir, FilenameFilter fnf){
    String dirs[]=dir.list(fnf);
    if(dirs==null){ return new String[0];}
    java.util.Arrays.sort(dirs);
    return dirs;
  }

  class fnf implements FilenameFilter{
    public boolean accept(File dir, String s){
      File f=new File(dir,s);
      return f.isFile()&&s.endsWith(".html");
    }
  }
}

class SMSMsgEntry{
  static final   int        MAXCOL=5;
  static private String[]   headers={"time","local no","remote no","remote origin","file"};
         private Object[]		content=new Object[MAXCOL];

  public SMSMsgEntry(String time, String localno, String remoteno, String origin, String file){
    content[0]=time;
    content[1]=localno;
    content[2]=remoteno;
    content[3]=origin;
    content[4]=file;
  }

  public Object getColContent(int i){ return content[i];}
  static public String[] getHeaders(){ return headers;}
}

class SMSMsgColumnModel extends DefaultTableColumnModel{

  public SMSMsgColumnModel(){super();}

  public void addColumn(TableColumn tc){
    int index=getColumnCount();
    switch(index){
    case 0: tc.setMinWidth(150);tc.setMaxWidth(175);break;    // time
    case 1: tc.setMinWidth(75);tc.setMaxWidth(100);break;     // local
    case 2: tc.setMinWidth(120);tc.setMaxWidth(200);break;    // remote
    case 3: tc.setMinWidth(200);tc.setMaxWidth(400);break;    // origin
    case 4: tc.setMinWidth(60);/*tc.setMaxWidth(200);*/break; // file
    } 
    super.addColumn(tc);
  }
}

class SMSMsgModel extends AbstractTableModel{

  private static final Class[] columnTypes=new Class[]{String.class,String.class,String.class,String.class,String.class};

  private Vector entries=new Vector();

  public Object getValueAt(int r,int c){
    if(r<entries.size()){
      SMSMsgEntry e=(SMSMsgEntry)entries.elementAt(r);
      return e.getColContent(c);
    }
    throw new IllegalArgumentException("Bad Cell ("+r+", "+c+")");
  }

  public String getTime(int r){return (String)getValueAt(r,0);}
  public String getLocal(int r){return (String)getValueAt(r,1);}
  public String getRemote(int r){return (String)getValueAt(r,2);}
  public String getRemoteName(int r){return (String)getValueAt(r,3);}
  public String getFile(int r){return (String)getValueAt(r,4);}

  public void addEntry(SMSMsgEntry e){ entries.add(e);}

  public void delEntry(int index){
    String filename=getFile(index);
    File   file=new File(filename);

    Object[] options = { "YES", "NO" };
    int action=JOptionPane.showOptionDialog(
      null, 
      "<html><b>Do you really want to delete the following file ?</b><br>&nbsp;<br><i>"
          +file.getAbsolutePath()+"</i><br>&nbsp;</html>", 
      "Warning", 
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      options,
      options[0]
    );
    if(action==JOptionPane.YES_OPTION){
      file.delete();
      entries.remove(index);
    }
  }

  public int getColumnCount(){        return SMSMsgEntry.MAXCOL;}
  public int getRowCount(){           return entries.size();}
  public String getColumnName(int c){ return SMSMsgEntry.getHeaders()[c];}
  public Class getColumnClass(int c){ return columnTypes[c];}
}

