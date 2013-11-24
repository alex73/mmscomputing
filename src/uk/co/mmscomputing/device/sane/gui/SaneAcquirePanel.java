package uk.co.mmscomputing.device.sane.gui;

import java.util.HashMap;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.beans.EventHandler;

import uk.co.mmscomputing.util.JarImageIcon;
import uk.co.mmscomputing.concurrent.Semaphore;
import uk.co.mmscomputing.device.sane.*;
import uk.co.mmscomputing.device.sane.option.*;

public class SaneAcquirePanel extends JPanel implements WindowListener{

  static private String previewtabstr        = jsane.getResource("gui.SaneAcquirePanel.previewtabstr");
  static private String previewbutstr        = jsane.getResource("gui.SaneAcquirePanel.previewbutstr");
  static private String scanstr              = jsane.getResource("gui.SaneAcquirePanel.scanstr");
  static private String cancelstr            = jsane.getResource("gui.SaneAcquirePanel.cancelstr");
  static private String progressbartitlestr  = jsane.getResource("gui.SaneAcquirePanel.progressbartitlestr");
  static private String acquireframetitlestr = jsane.getResource("gui.SaneAcquirePanel.acquireframetitlestr");

  private JFrame             dialog;

  private SaneDevice         scanner;
  private SanePreviewPanel   preview;
  private SaneSpecialPanel   special;
  private JTabbedPane        tp;
  private HashMap            options;
  private Semaphore          semaphore;

  public SaneAcquirePanel(SaneDevice device,Semaphore s)throws SaneIOException{
    this.dialog=null;
    this.scanner=device;
    this.semaphore=s;

    options=new HashMap();

    setLayout(new BorderLayout());
 
    tp=new JTabbedPane();
    buildTab();
    add(tp,BorderLayout.CENTER);

    JPanel p=new JPanel();
    p.setLayout(new BorderLayout());

    JProgressBar pbar=new JProgressBar();
    pbar.setBorder(new TitledBorder(progressbartitlestr));
    pbar.setStringPainted(true);
    pbar.setMinimum(0);
    pbar.setString(device.getName());
    p.add(pbar,BorderLayout.CENTER);
    device.setProgressBar(pbar);

    JPanel bp=new JPanel();
    bp.setLayout(new GridLayout(1,3));

    JButton button;

    button=new JButton(previewbutstr,new JarImageIcon(getClass(),"16x16/scanner.png"));
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "preview"));
    bp.add(button);

    button=new JButton(scanstr);
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "scan"));
    bp.add(button);

    button=new JButton(cancelstr);
    button.addActionListener((ActionListener)EventHandler.create(ActionListener.class, this, "cancelScan"));
    bp.add(button);

    p.add(bp,BorderLayout.SOUTH);
    add(p,BorderLayout.SOUTH);
  }

  private void    close(){      semaphore.release();}
  public  void    scan(){       dialog.dispose();}
  public  void    cancelScan(){ scanner.setCancel(true); dialog.dispose();}
  public  void    preview(){    new Thread(preview).start();}      // start reading image data into preview panel

  private int build(JTabbedPane parent, OptionDescriptor[] list, int no, boolean group){
    while(no<list.length){
      Descriptor od=(Descriptor)list[no];
      if(od!=null){
        if(od.getType()==SaneConstants.SANE_TYPE_GROUP){
          if(group){
            return no-1;  
          }else{
            JTabbedPane tp=new JTabbedPane();
            no=build(tp,list,no+1,true);
            if(tp.getTabCount()>0){
              parent.addTab(od.getTitle(),tp);
            }
          }
        }else{
          try{
            JComponent gui=od.getGUI();
            options.put(od.getName(),od);
            parent.addTab(od.getTitle(),gui);
          }catch(Exception e){                  // silently ignore broken options
//            System.err.println("Cannot create option : "+od.getName()+"\n"+e.getMessage());
            e.printStackTrace();
          }
        }
      }
      no++;
    }
    return no;
  }

  public void buildTab(){
    tp.removeAll();
    try{
      build(tp,scanner.getOptionDescriptors(),1,false);
    }catch(Exception e){
//      System.out.println("9\b"+getClass().getName()+".build:\n\t"+e);
      e.printStackTrace();
    }
    special=new SaneSpecialPanel(scanner,options);
    tp.insertTab("jsane-Special",null,special,null,0);
    preview=new SanePreviewPanel(scanner,options);
    tp.insertTab(previewtabstr,null,preview,null,0);
    tp.setSelectedIndex(0);
  }

  // start window listener methods

  public void windowOpened(WindowEvent e){}
  public void windowClosing(WindowEvent e){scanner.setCancel(true);}
  public void windowClosed(WindowEvent e){close();}
  public void windowIconified(WindowEvent e){}
  public void windowDeiconified(WindowEvent e){}
  public void windowActivated(WindowEvent e){}
  public void windowDeactivated(WindowEvent e){}

  // end window listener methods

  public void display(){
    dialog=new JFrame(acquireframetitlestr);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.getContentPane().add(this);
    dialog.addWindowListener(this);
    dialog.setSize(400,500);
//    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }
}
