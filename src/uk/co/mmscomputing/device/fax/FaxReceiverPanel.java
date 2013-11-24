package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.beans.*;
import javax.swing.border.*;

import uk.co.mmscomputing.device.phone.PhoneBook;
import uk.co.mmscomputing.util.*;

public class FaxReceiverPanel 
  extends    JPanel 
  implements FaxConstants,FaxCallHandlerFactory,FaxReceiverListener
{

  protected Properties            properties;
  protected FaxCallHandlerFactory handlerfactory;
  protected PhoneBook             phonebook;

  private JTable                  table;
  private FaxModel		            model=new FaxModel();
  private FaxColumnModel		      columnmodel=new FaxColumnModel();

  public FaxReceiverPanel(
    Properties properties,
    PhoneBook phonebook
  ){
    this.properties=properties;
    this.phonebook=phonebook;

    this.handlerfactory=this;

    setLayout(new BorderLayout());

    table=new JTable(model,columnmodel);
    table.createDefaultColumnsFromModel();
    table.getTableHeader().setReorderingAllowed(false);
    add(new JScrollPane(table),BorderLayout.CENTER);

    properties.setProperty(faxReceiverFileDirID,properties.getProperty(faxReceiverFileDirID,faxDefaultPath));
    buildCallList(properties.getProperty(faxReceiverFileDirID));

    table.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.isMetaDown()){
          int selected=table.rowAtPoint(new Point(e.getX(),e.getY()));
          table.setRowSelectionInterval(selected, selected);
          if(e.getClickCount()==2){
            deleteFaxes();
          }
        }else if(e.isShiftDown()){
          int selected=table.rowAtPoint(new Point(e.getX(),e.getY()));
          table.setRowSelectionInterval(selected, selected);
          if(e.getClickCount()==2){
            updatePhoneBook(model.getRemote(selected));
          }
        }else if(e.getClickCount()==2){
          int selected=table.getSelectedRow();
          if(selected==-1){return;}
          table.setRowSelectionInterval(selected,selected);
          showFax(
            model.getTime(selected),
            model.getLocal(selected),
            model.getRemote(selected),
            model.getFile(selected)
          );
        }
      }
    });
  }

  private void addPropertyChangeListener(String name){
    Component parent=getParent();
    while(parent!=null){
      if(parent instanceof JTabbedPane){
        JTabbedPane tp   =(JTabbedPane)parent;
        int         index=tp.indexOfTab(name);
        if(index!=-1){
          Object tab=tp.getComponentAt(index);
          if(tab instanceof PropertyChangeListener){
            PropertyChangeListener pcl=(PropertyChangeListener)tab;
            addPropertyChangeListener(pcl);
            return;
          }
        }
      }
      parent=parent.getParent();
    }
  }

  public void setHandlerFactory(FaxCallHandlerFactory handlerfactory){
    this.handlerfactory=handlerfactory;
  }

  public FaxCallHandler getHandler(Properties properties){
    FaxCallSaver handler=new FaxCallSaver();
    handler.init(properties);
    return handler;
  }

  private static boolean addedFaxImageListener=false;

  protected void showFax(String time,String local,String remote,String fn){
    if(!addedFaxImageListener){
      addedFaxImageListener=true;
      addPropertyChangeListener("Fax Image");
    }
    firePropertyChange("open", null, fn);		      //	Call ImagePanel; (property name, old value , new value)      
  }

  private static boolean addedPhoneBookListener=false;

  protected void updatePhoneBook(String remote){  // handled by sub class
    if(!addedPhoneBookListener){
      addedPhoneBookListener=true;
      addPropertyChangeListener("Phone Book");
    }
    firePropertyChange("update", null, remote);		//	Call PhoneBookTab; (property name, old value , new value)      
  }

  protected void deleteFaxes(){
    int[] selected=table.getSelectedRows();
    for(int i=selected.length-1; i>=0; i--){
      ((FaxModel)table.getModel()).delEntry(selected[i]);
    }
    table.revalidate();
  }

  private int getPickUpTime(String localno){
    try{
      String[] localnos=properties.getProperty(faxLocalNosID,"").split(",");

      if((localnos.length==1)&&(localnos[0].equals(""))){
        try{
          return Integer.parseInt(properties.getProperty(faxTimeToWaitID));
        }catch(Exception e){
          System.out.println("3\b"+getClass().getName()+".update:\n\tCould not set pick up time.\n\t"+e);
          return 20;
        }
      }
      for(int i=0;i<localnos.length;i++){
        if(localno.trim().equals(localnos[i])){
          try{
            return Integer.parseInt(properties.getProperty(faxTimeToWaitID));
          }catch(Exception e){
            System.out.println("3\b"+getClass().getName()+".update:\n\tCould not set pick up time.\n\t"+e);
            return 20;
          }
        }
      }
    }catch(Exception e){
    }
    return -1;                               // ignore call
  }

  public void update(FaxReceiverMetadata.Type type, FaxReceiverMetadata md){ // called from faxreceiver
    try{
      if(type.equals(FaxReceiverMetadata.STATE)){
        if(md.isState(faxRinging)){
          int waitforsecs = getPickUpTime(md.getLocalNo());

          if(waitforsecs!=-1){                                 // if we accept that call.
            IndicationProgressMonitor pm;
            String rno=md.getRemoteNo();

            pm=new IndicationProgressMonitor(
                md.getController(),rno,phonebook.getName(rno),waitforsecs*1000
            );
            md.setPickUpTime(waitforsecs,pm);
            try{
              md.setLocalNo(properties.getProperty(faxLocalNoID));
            }catch(Exception e){
              System.out.println("3\b"+getClass().getName()+".update:\n\tCould not set local number.\n\t"+e);
              md.setLocalNo("012345");
            }
            try{
              md.setHeader(properties.getProperty(faxHeaderID));
            }catch(Exception e){
              System.out.println("3\b"+getClass().getName()+".update:\n\tCould not set fax header.\n\t"+e);
              md.setHeader("My Fax Header");
            }
          }
        }else if(md.isState(faxConnected)){
          FaxCallHandler fch=handlerfactory.getHandler(properties);
          md.setHandler(fch);
        }else if(md.isState(faxDisconnected)){
          FaxCallHandler fch=md.getHandler();

          if(fch instanceof FaxCallMonitor){
            String fn=((FaxCallMonitor)fch).getFile();
            if(fn!=null){
              model.addEntry(parseFileName(new File(fn)));
              table.revalidate();
            }
          }

          if(fch instanceof FaxCallSaver){
            String fn=((FaxCallMonitor)fch).getFile();
            if(fn!=null){
              model.addEntry(parseFileName(new File(fn)));
              table.revalidate();
            }
          }
        }
      }
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".update:\n\t"+e);
      e.printStackTrace();
    }
  }

  private FaxEntry parseFileName(File file){
    String fn=file.getName();
    fn=fn.substring(0,fn.indexOf('.'));
    String[] content=fn.split("_");
    String date  =(content.length>=1)?content[0]:"unknown";
    String time  =(content.length>=2)?content[1].replace('-',':'):"unknown";
    String local =(content.length>=3)?content[2]:"unknown";
    String remote=(content.length>=4)?content[3]:"withhold";
    String name=phonebook.getName(remote);
    return new FaxEntry(date+" "+time,local,remote,name,file.getAbsolutePath());
  }

  private void buildCallList(String path){
    String[] files=list(path,new fnf()); 
    for(int i=0; i<files.length; i++){
      File file=new File(path,files[i]);
      try{
        model.addEntry(parseFileName(file));
      }catch(Exception e){
        System.out.println(getClass().getName()+".parseFileName:\n\t"+e);
        e.printStackTrace();
      }
    }
  }

  public String[] list(String dir, FilenameFilter fnf){
    String dirs[]=new java.io.File(dir).list(fnf);
    if(dirs==null){ return new String[0];}
    java.util.Arrays.sort(dirs);
    return dirs;
  }

  class fnf implements FilenameFilter{
    public boolean accept(File dir, String s){
      File f=new File(dir,s);
      return f.isFile()&&(s.endsWith(".sff")||s.endsWith(".tif")||s.endsWith(".tiff"));
    }
  }
}

class FaxEntry{
  static final   int        MAXCOL=5;
  static private String[]   headers={"time","local no","remote no","remote name","file"};
         private Object[]		content=new Object[MAXCOL];

  public FaxEntry(String time, String localno, String remoteno, String name, String file){
    content[0]=time;
    content[1]=localno;
    content[2]=remoteno;
    content[3]=name;
    content[4]=file;
  }

  public Object getColContent(int i){ return content[i];}
  static public String[] getHeaders(){ return headers;}
}

class FaxColumnModel extends DefaultTableColumnModel{

  public FaxColumnModel(){super();}

  public void addColumn(TableColumn tc){
    int index=getColumnCount();
    switch(index){
    case 0: tc.setMinWidth(150);tc.setMaxWidth(175);break;    // time
    case 1: tc.setMinWidth(75);tc.setMaxWidth(100);break;     // local
    case 2: tc.setMinWidth(120);tc.setMaxWidth(200);break;    // remote
    case 3: tc.setMinWidth(200);tc.setMaxWidth(400);break;    // name
    case 4: tc.setMinWidth(60);/*tc.setMaxWidth(200);*/break; // file
    } 
    super.addColumn(tc);
  }
}

class FaxModel extends AbstractTableModel{

  private static final Class[] columnTypes=new Class[]{String.class,String.class,String.class,String.class,String.class};

  private Vector entries=new Vector();

  public Object getValueAt(int r,int c){
    if(r<entries.size()){
      FaxEntry e=(FaxEntry)entries.elementAt(r);
      return e.getColContent(c);
    }
    throw new IllegalArgumentException("Bad Cell ("+r+", "+c+")");
  }

  public String getTime(int r){return (String)getValueAt(r,0);}
  public String getLocal(int r){return (String)getValueAt(r,1);}
  public String getRemote(int r){return (String)getValueAt(r,2);}
  public String getRemoteName(int r){return (String)getValueAt(r,3);}
  public String getFile(int r){return (String)getValueAt(r,4);}

  public void addEntry(FaxEntry e){ entries.add(e);}

  public void delEntry(int index){
    String filename=getFile(index);
    File   file=new File(filename);

    Object[] options = { "YES", "NO" };
    int action=JOptionPane.showOptionDialog(
      null, 
      "<html><b>Do you really want to delete the following file ?</b><br>&nbsp;<br><i>"+file.getAbsolutePath()+"</i><br>&nbsp;</html>", 
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

  public int getColumnCount(){        return FaxEntry.MAXCOL;}
  public int getRowCount(){           return entries.size();}
  public String getColumnName(int c){ return FaxEntry.getHeaders()[c];}
  public Class getColumnClass(int c){ return columnTypes[c];}
}

class IndicationProgressMonitor extends uk.co.mmscomputing.concurrent.Timer
    implements uk.co.mmscomputing.concurrent.TimerListener{

  JFrame              dialog;
  JProgressBar        progressBar;

  public IndicationProgressMonitor(int cntl,String remote,String name,int timeout){
    super(timeout);
    setDelay(500);
    setListener(this);
    dialog=getDialog(cntl,remote,name);
  }

  public JFrame getDialog(int cntl,String remote,String name){
    JPanel panel=new JPanel();

    JPanel q=new JPanel();
    q.setLayout(new BorderLayout());
    Border border = q.getBorder();
    Border margin = new EmptyBorder(10,10,5,10);
    q.setBorder(new CompoundBorder(border, margin));
    q.add(new JLabel("Indication ("+cntl+") from:"),BorderLayout.NORTH);

    JLabel img=new JLabel(new JarImageIcon(getClass(),"32x32/fax.png"));
    img.setBorder(new EmptyBorder(10,10,10,10));
    q.add(img,BorderLayout.WEST);

    JLabel msg=new JLabel("<html><i>"+name+"</i><br>"+remote+"</html>");
    msg.setBorder(new EmptyBorder(10,10,10,10));
    q.add(msg,BorderLayout.CENTER);

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());

    progressBar = new JProgressBar(0,timeout);
    progressBar.setValue(0);
    progressBar.setIndeterminate(false);
    progressBar.setStringPainted(true);

    progressBar.setBorder(new EmptyBorder(5,0,5,0));
    p.add(progressBar,BorderLayout.NORTH);


    JPanel buttons=new JPanel();
    buttons.setLayout(new GridLayout(0,3));
    buttons.add(new JPanel());

    JButton button=new JButton(
      new AbstractAction("pick up"){
        public void actionPerformed(ActionEvent ev){ 
          release();
        }
      }
    );
    buttons.add(button);
    buttons.add(new JPanel());

    p.add(buttons,BorderLayout.CENTER);
    q.add(p,BorderLayout.SOUTH);

    panel.setLayout(new BorderLayout());
    panel.setOpaque(true);
    panel.add(q,BorderLayout.NORTH);

    JFrame dialog = new JFrame("Progress...");
    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    dialog.setContentPane(panel);
    dialog.pack();

    return dialog;
  }

  public void begin(int timeleft){
    dialog.setLocationRelativeTo(null);
    dialog.toFront();
    dialog.setVisible(true);
  }

  public void tick(int timeleft){
    progressBar.setValue(timeout-timeleft);
    progressBar.setString("Pick up in "+timeleft+" ms");
  }

  public void end(int timeleft){
    dialog.dispose();
  }
}
