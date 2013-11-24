package uk.co.mmscomputing.application.phone;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.beans.*;

import uk.co.mmscomputing.util.*;
import uk.co.mmscomputing.util.configuration.*;

import uk.co.mmscomputing.device.phone.*;

public class PhoneBookTab extends JPanel implements PropertyChangeListener,PhoneCallerListener{

  InputPanel                      input;

  private Properties              properties;

  private PhoneBookImpl           phonebook=null;
  private PhoneBookModel		      model=null;
  private PhoneBookColumnModel		columnmodel=null;

  JTable                  				table;
  private int                     curcol=0;

  public PhoneBookTab(Properties properties,PhoneBookImpl phonebook)throws IOException{
    this.properties=properties;
    this.phonebook=phonebook;

    columnmodel=new PhoneBookColumnModel();
    model=new PhoneBookModel();

    setLayout(new BorderLayout());

/*
    PhoneCaller caller=PhoneCaller.getDevice();
    if(caller!=null){
      caller.addListener(this);
    }
*/
    input=new InputPanel(/*caller*/);
    add(input,BorderLayout.NORTH);

    table=new JTable(model,columnmodel);
    table.createDefaultColumnsFromModel();

    ListSelectionModel csm=table.getSelectionModel();
    csm.addListSelectionListener(new SelectionListener());

    JTableHeader th=table.getTableHeader();
    th.setReorderingAllowed(false);
    th.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.getClickCount()==1){
          buildTable(table.getTableHeader().columnAtPoint(e.getPoint()));
          table.revalidate();
          getParent().repaint();
        }else{
        }
      }
    });
    table.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.isMetaDown()){
          int selected=table.rowAtPoint(e.getPoint());
          table.setRowSelectionInterval(selected, selected);
          if(e.getClickCount()==2){
              delete(selected);
          }
/*
        }else if(e.isShiftDown()){
          int selected=table.rowAtPoint(e.getPoint());
          table.setRowSelectionInterval(selected, selected);
          if(e.getClickCount()==2){
            phone(selected);
          }
*/
        }else if(e.getClickCount()==1){
          int selected=table.getSelectedRow();
          if(selected==-1){return;}
          table.setRowSelectionInterval(selected,selected);
          String name=(String)table.getValueAt(selected,0);
          String number=(String)table.getValueAt(selected,1);
          input.setText(name,number);       
        }
      }
    });
    add(new JScrollPane(table),BorderLayout.CENTER);
    buildTable(curcol);
  }

  private void buildTable(int column){
    model.clear();
    column=(column+1)&1;
    Set set=phonebook.entrySet(column);
    Iterator iterator=set.iterator();
    while(iterator.hasNext()){
      Map.Entry entry=(Map.Entry)iterator.next();
      PhoneBookEntry pbe;
      if(column==0){
        pbe=new PhoneBookEntry((String)entry.getValue(),(String)entry.getKey());
      }else{
        pbe=new PhoneBookEntry((String)entry.getKey(),(String)entry.getValue());
      }
      model.addEntry(pbe);
    }
  }

  public void propertyChange(final PropertyChangeEvent evt){
    String prop=evt.getPropertyName();
    if(prop.equals("update")){
      JTabbedPane tp=(JTabbedPane)getParent();
      tp.setSelectedIndex(tp.indexOfTab("Phone Book"));
      String number=(String)evt.getNewValue();
      input.setText(phonebook.getName(number),number);       
    }
  }

  private void save(String name, String number){
//    while(number.startsWith("0")){number=number.substring(1);}
    int row=model.getRow(0,name);
    if(row!=-1){
      phonebook.remove((String)model.getValueAt(row,1));
      model.removeRow(row);
    }else{
      row=model.getRow(1,number);
      if(row!=-1){
        phonebook.remove(number);
        model.removeRow(row);
      }
    }
    PhoneBookEntry pbe=new PhoneBookEntry(name,number);
    model.addEntry(pbe);
    phonebook.put(number,name);
    phonebook.write();
    buildTable(curcol);
    table.revalidate();
    getParent().repaint();
  }

/*
  private void phone(int row){
    Object[] options = { "YES", "NO" };
    String name=(String)table.getValueAt(row,0);
    String number=(String)table.getValueAt(row,1);
    int action=JOptionPane.showOptionDialog(
        null, 
        "<html><b>Do you really want to call "+name+" ["+number+"] ?</b><br>&nbsp;</html>", 
        "Warning", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );
    if(action==JOptionPane.YES_OPTION){
      System.out.println("9\bPhone "+number);
    }
  }
*/
  private void delete(int row){
    Object[] options = { "YES", "NO" };
    String name=(String)table.getValueAt(row,0);
    String number=(String)table.getValueAt(row,1);
    int action=JOptionPane.showOptionDialog(
        null, 
        "<html><b>Do you really want to delete entry "+name+" ["+number+"] ?</b><br>&nbsp;</html>", 
        "Warning", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );
    if(action==JOptionPane.YES_OPTION){
      phonebook.remove(number);
      model.removeRow(row);
      phonebook.write();
      buildTable(curcol);
      table.revalidate();
    }
  }

  public void addEntry(String name, String number){
    input.setText(number,name);     
    JTabbedPane tp=(JTabbedPane)getParent();
    tp.setSelectedIndex(tp.indexOfTab("Phone Book"));
  }

  class SelectionListener implements ListSelectionListener{
    public void valueChanged(ListSelectionEvent lse){
      if(lse.getValueIsAdjusting()){return;}
      int selected=table.getSelectedRow();
      table.setRowSelectionInterval(selected,selected);
      String name=(String)table.getValueAt(selected,0);
      String number=(String)table.getValueAt(selected,1);
      input.setText(name,number);       
    }
  }

  private InputStream getInputStream(){
    return null;
  }

  private OutputStream getOutputStream(){
//    SourceDataLine out=SoundMixerEnumerator.getOutputLine(pcmformat,bufsize);  // get default sound output i.e. speaker
    return null;
  }
///*
  public void update(PhoneCallerMetadata.Type type, final PhoneCallerMetadata md){
    if(type.equals(PhoneCallerMetadata.STATE)){           // in what state are we
      int state=md.getState();
      switch(state){
      case PhoneConstants.phoneRinging:
        md.setLocalNo(properties.getProperty(PhoneConstants.phoneLocalNoID));
        md.setRemoteNo(properties.getProperty(PhoneConstants.phoneRemoteNoID));
        System.out.println("Ringing ... "+md.getRemoteNo());
        break;
      case PhoneConstants.phoneConnected:
        md.setInputStream(getInputStream());
        md.setOutputStream(getOutputStream());
        System.out.println("Connected to ... "+md.getRemoteNo());
        break;
      case PhoneConstants.phoneDisconnected:
        System.out.println("Disconnected ... "+md.getRemoteNo());
        break;
      }
    }else if(type.equals(PhoneCallerMetadata.INFO)){
      System.out.println(md.getInfo());
    }else if(type.equals(PhoneCallerMetadata.EXCEPTION)){
      System.out.println("9\b"+md.getException().getMessage());
    }
  }
//*/
  class InputPanel extends JPanel{

    JTextField name=new JTextField(40);
    JTextField number=new JTextField(60);

    InputPanel(/*PhoneCaller caller*/){
      setLayout(new BorderLayout());

      JPanel p=new JPanel();p.setLayout(new GridLayout(0,2));
      JLabel l=new JLabel("Name");l.setBorder(new EtchedBorder());p.add(l);p.add(name);
      l=new JLabel("Phone Number");l.setBorder(new EtchedBorder());p.add(l);p.add(number); 

      add(p,BorderLayout.CENTER);

      p=new JPanel();
      p.setLayout(new GridLayout(1,0));
      p.add(new JButton(getSaveAction()));
//      if(caller!=null){p.add(caller.getGUI(properties));}
      add(p,BorderLayout.EAST);
    }

    public void setText(String name, String number){
      this.name.setText(name);
      this.number.setText(number);
      properties.setProperty(PhoneConstants.phoneRemoteNoID,number);
    }

    public Action getSaveAction(){
      return new AbstractAction("",new JarImageIcon(getClass(),"32x32/save.png")){
        public void actionPerformed(ActionEvent ev){
          save(name.getText(),number.getText());
        }
      };
    }
///*
    public Action getCallAction(){
      return new AbstractAction("Call",new JarImageIcon(getClass(),"32x32/save.png")){
        public void actionPerformed(ActionEvent ev){
          save(name.getText(),number.getText());
        }
      };
    }
//*/    
  }
}

class PhoneBookModel extends AbstractTableModel{

  private static final Class[] columnTypes=new Class[]{String.class,String.class};

  private Vector entries=null;

  public PhoneBookModel(){
    entries=new Vector();
  }

  public Object getValueAt(int r,int c){
    if(r<entries.size()){
      PhoneBookEntry e=(PhoneBookEntry)entries.elementAt(r);
      return e.getColContent(c);
    }
    throw new IllegalArgumentException("Bad Cell ("+r+", "+c+")");
  }
  
  public void clear(){entries.clear();}
  public void addEntry(PhoneBookEntry e){ entries.add(e);}

  public void delEntry(int index){
    String file=(String)getValueAt(index,4);
    new File(file).delete();
    entries.remove(index);
  }

  public int getColumnCount(){        return PhoneBookEntry.MAXCOL;}
  public int getRowCount(){           return entries.size();}
  public String getColumnName(int c){ return PhoneBookEntry.getHeaders()[c];}
  public Class getColumnClass(int c){ return columnTypes[c];}

  public void removeRow(int row){
    entries.remove(row);
  }

  public int getRow(int col, String value){
    String val;
    int    rowcount=getRowCount();
    for(int i=0;i<rowcount;i++){
      val=(String)getValueAt(i,col);
      if(val.equals(value)){
        return i;
      }
    }
    return -1;
  }

  public int getRow(String name, String number){
    String nam,num;
    int    rowcount=getRowCount();
    for(int i=0;i<rowcount;i++){
      nam=(String)getValueAt(i,0);
      num=(String)getValueAt(i,1);
      if(nam.equals(name)&&num.equals(number)){
        return i;
      }
    }
    return -1;
  }

}

class PhoneBookColumnModel extends DefaultTableColumnModel{

  public PhoneBookColumnModel(){
    super();
  }

  public void addColumn(TableColumn tc){
    int index=getColumnCount();
    switch(index){
    case 0: tc.setMinWidth(100);/*tc.setMaxWidth(200);*/break;
    case 1: tc.setMinWidth(300);tc.setMaxWidth(600);break;
    } 
    super.addColumn(tc);
  }
}

class PhoneBookEntry{

  static final   int        MAXCOL=2;
  static private String[]   headers={"Name","Phone Number"};
         private Object[]		content=new Object[MAXCOL];

  public PhoneBookEntry(String name, String number){
    content[0]=name;
    content[1]=number;
  }

  public Object getColContent(int i){ return content[i];}
  static public String[] getHeaders(){ return headers;}

}
