package uk.co.mmscomputing.device.printmonitor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PMConfigurationPanel extends JPanel implements ActionListener{

  static private String[][] filters  =  {
    {"SFF" ,"Structured Fax File CAPI"},
    {"MH"  ,"Fax T.4 Modified Huffman"},
    {"MR"  ,"Fax T.4 Modified READ"},
    {"MMR" ,"Fax T.6 Modified Modified READ"},
    {"NONE","None"}
  };

  static private int[] modes = {
    PMOutputStreamFactory.SFF,
    PMOutputStreamFactory.T4MH,
    PMOutputStreamFactory.T4MR,
    PMOutputStreamFactory.T6MMR,
    PMOutputStreamFactory.NONE
  };
  
  private JFrame             dialog;
  private ButtonGroup        filterbuttons=new ButtonGroup();
  private JTextField         doctf=new JTextField(32);
  private JTextField         nametf=new JTextField(32);
  private JTextField         desctf=new JTextField(32);

  private int      outputmode    = PMOutputStreamFactory.T6MMR;
  private int      outputmodenew = PMOutputStreamFactory.T6MMR;
  private Port     port;
  private boolean  wantToAdd;
    
  PMConfigurationPanel(Port port,boolean wantToAdd){
    super(new BorderLayout());
    this.wantToAdd=wantToAdd;
    this.port=port;
    outputmode=port.getOutputMode();
    outputmodenew=outputmode;

    JTabbedPane tp=new JTabbedPane();

    tp.addTab("Description",getTextFieldsPanel(port));
    tp.addTab("Compression",getFilterPanel(port));
    add(tp,BorderLayout.CENTER);

    JPanel bp=new JPanel();
    bp.setLayout(new GridLayout(1,2));

    JButton button = new JButton("ok");
    button.setActionCommand("ok");
    button.addActionListener(this);
    bp.add(button);

    button = new JButton("cancel");
    button.setActionCommand("cancel");
    button.addActionListener(this);
    bp.add(button);

    add(bp,BorderLayout.SOUTH);
  }

  private JPanel getFilterPanel(Port port){
    JPanel      rp    = new JPanel(new GridLayout(0, 1));
    ButtonGroup group = filterbuttons;
    JRadioButton button;
    for(int i=0;i<modes.length;i++){
      button = new JRadioButton(filters[i][1]);
      button.setActionCommand(filters[i][0]);
      button.addActionListener(this);
      if(outputmode==modes[i]){button.setSelected(true);}
      group.add(button);
      rp.add(button);
    }
    rp.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());
    p.add(rp, BorderLayout.CENTER);

    return p;
  }

  private JPanel getTextFieldsPanel(Port port){
    JPanel p=new JPanel();
    p.setLayout(new GridLayout(0,1));
    p.add(getNamePanel(port));
    p.add(getDescriptionPanel(port));
    p.add(getDocumentPathPanel(port));
    return p;
  }

  private JPanel getNamePanel(Port port){
    JPanel q=new JPanel();
    q.setLayout(new GridLayout(0,1));
    Border border=BorderFactory.createLineBorder(Color.black);
    TitledBorder title = BorderFactory.createTitledBorder(border, "Port Name");
    title.setTitleJustification(TitledBorder.CENTER);
    q.setBorder(title);
    nametf.setText(port.getName());
    nametf.setEditable(wantToAdd);
    q.add(nametf);
    return q;
  }

  private JPanel getDescriptionPanel(Port port){
    JPanel q=new JPanel();
    q.setLayout(new GridLayout(0,1));
    Border border=BorderFactory.createLineBorder(Color.black);
    TitledBorder title = BorderFactory.createTitledBorder(border, "Description");
    title.setTitleJustification(TitledBorder.CENTER);
    q.setBorder(title);
    desctf.setText(port.getDescription());
    desctf.setEditable(wantToAdd);
    q.add(desctf);
    return q;
  }

  private JPanel getDocumentPathPanel(Port port){
    JPanel q=new JPanel();
    q.setLayout(new GridLayout(0,1));
    Border border=BorderFactory.createLineBorder(Color.black);
    TitledBorder title = BorderFactory.createTitledBorder(border, "Documents Path");
    title.setTitleJustification(TitledBorder.CENTER);
    q.setBorder(title);
    doctf.setText(port.getDocumentsPath());
    q.add(doctf);
    return q;
  }

  public void actionPerformed(ActionEvent ev) {
    String action=ev.getActionCommand();

    if(action.equals("ok")){
      outputmode=outputmodenew;
      port.setName(nametf.getText());
      port.setDescription(desctf.getText());
      port.setOutputMode(outputmode);
      port.setDocumentsPath(doctf.getText());
      if(dialog!=null){dialog.dispose();}
    }else if(action.equals("cancel")){
      if(dialog!=null){dialog.dispose();}
    }else{
      for(int i=0;i<modes.length;i++){
        if(action.equals(filters[i][0])){
          outputmodenew=modes[i];break;
        }
      }
    }
  }

  public void display(){
    try{
      dialog=new JFrame("Java Print Monitor Configuration Panel");
      dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      dialog.setContentPane(this);
      GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
      dialog.pack();
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);
      dialog.toFront();
    }catch(Exception e){
      System.out.println(getClass().getName()+".display:\n\t"+e);
      e.printStackTrace();
    }
  }
}