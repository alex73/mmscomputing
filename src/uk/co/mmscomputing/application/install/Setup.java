package uk.co.mmscomputing.application.install;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Setup extends JPanel{

  private Log        log;

//  private String     java;
  private Vector     javacs;

  private JComboBox  ljavac;
  private String     osname;

  private JCheckBox  extractit;
  private JCheckBox  compileit;
  private JCheckBox  startAnswerphone;
  private JCheckBox  startImageViewer;

  public Setup(Log log){
    this.log=log;

    osname=System.getProperty("os.name");
    if(osname.startsWith("Linux")){
      javacs=search("/javac");

      try{
        String jre=System.getProperty("java.home");
        if(jre!=null){
          String jc=jre.replaceAll("/jre","/bin/javac");
          File f=new File(jc);
          if(f.exists() && f.isFile()){
            javacs.add(f.getCanonicalPath());
          }
        }
      }catch(Exception e){
        log.msg(e.getMessage());
      }

    }else if(osname.startsWith("Windows")){
      log.msg("java.home "+System.getProperty("java.home"));
      javacs=search("\\javac.exe");
    }else{
      javacs.add("javac");
    }

    JPanel p=new JPanel();
    p.setLayout(new GridLayout(0,1));

    JPanel q=new JPanel();
    q.setLayout(new GridLayout(0,2));

    q.add(new JLabel(" Installation directory"));
    q.add(new JLabel(getUserdir()));

    q.add(new JLabel(" Java compiler"));
    ljavac=new JComboBox(javacs);
    ljavac.setEditable(true);
    q.add(ljavac);

    p.add(q);

    q=new JPanel();
    q.setLayout(new GridLayout(0,2));

    extractit=new JCheckBox(" Extract package",true);
    q.add(extractit);

    compileit=new JCheckBox(" Compile java files",true);
    q.add(compileit);

    startAnswerphone=new JCheckBox(" Start answerphone application",true);
    q.add(startAnswerphone);

    startImageViewer=new JCheckBox(" Start imageviewer application",true);
    q.add(startImageViewer);

    p.add(q);

    setLayout(new BorderLayout());
    add(p,BorderLayout.NORTH);
  }

  public boolean getExtractIt(){
    return extractit.isSelected();
  }

  public boolean getCompileIt(){
    return compileit.isSelected();
  }

  public boolean getStartAnswerphone(){
    return startAnswerphone.isSelected();
  }

  public boolean getStartImageViewer(){
    return startImageViewer.isSelected();
  }

  public String getJavac(){
    String javac="javac";
    if(javacs.size()>0){
      javac=(String)ljavac.getSelectedItem();
    }else{
      javac = request(javac);
//    ljavac.setText(javac);
    }
    return javac;
  }

  public String getUserdir(){
    return System.getProperty("user.dir");
  }

  public String request(String pn){
    File f=new File(pn);
    while(!f.exists() /*|| !fn.endsWith(pn)*/){
      String msg="Cannot find program '"+pn+"'.\n";
      msg+="Please select a valid path with the upcoming file chooser.";
      try{
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE); 
        
        JFileChooser fc=new JFileChooser(System.getProperty("user.home"));
        int res=fc.showOpenDialog(null);
        if(res==JFileChooser.APPROVE_OPTION){
          pn=fc.getSelectedFile().getPath();
          f=new File(pn);
        }else{
          return "";
        }
      }catch(Exception e){
        log.msg(e.getMessage());
      }    
    }
    return pn;
  }

  String[] list={
    "/usr/java/jdk1.5",
    "/usr/java/j2sdk1.4",
    "/usr/lib/java",
    "/usr/lib/jvm",
    "/usr/lib/SunJava",
//    "/etc/alternatives/java",
    "c:\\j2sdk1.4",
    "c:\\Program Files\\java\\jdk1.5",
    "c:\\Program Files\\java\\jdk1.6",
//    "c:\\jdk1.3"
  };

  public Vector search(String javac){
    Vector   v = new Vector();
    try{
      for(int i=0;i<list.length;i++){
        File dir    = new File(list[i]);
        File parent = dir.getParentFile();
        if(parent.exists()&&parent.isDirectory()){
          String   name = dir.getName();
          String[] fns  = parent.list();
          for(int j=0;j<fns.length;j++){
            if(fns[j].startsWith(name)){
              String fn=fns[j]+File.separator+"bin"+javac;
System.err.println(parent+"/"+fn);
              File file=new File(parent,fn);
              if(file.exists()&&file.isFile()){
                v.add(file.getCanonicalPath());
              }
            }
          }
        }
      }
    }catch(Exception e){}
    return v;
  }  
}