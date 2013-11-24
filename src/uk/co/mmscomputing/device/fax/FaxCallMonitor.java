package uk.co.mmscomputing.device.fax;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.co.mmscomputing.util.*;

public class FaxCallMonitor implements FaxCallHandler{

  protected FaxCallSaver handler;

  public FaxCallMonitor(FaxCallSaver handler){
    this.handler=handler;
  }

  public String getFile(){return handler.getFile();}
  public void init(java.util.Properties properties){handler.init(properties);}

  public void run(String local,String remote,InputStream pin,OutputStream pout){
    try{
      pin=new FaxCallMonitorInputStream("Save Fax from "+remote,pin);
      handler.run(local,remote,pin,pout);
    }catch(Exception e){
      System.out.println("9\b"+getClass().getName()+".run\n\t"+e);
      e.printStackTrace();
    }
  }

  class FaxCallMonitorInputStream extends FilterInputStream{

    FaxCallProgressMonitor pm=null;
    int count=0;

    public FaxCallMonitorInputStream(String message,InputStream in){
      super(in);
      pm=new FaxCallProgressMonitor(message);
      new Thread(pm).start();
    }

    public int read()throws IOException{
      int b=in.read();if(b!=-1){count++;}return b;
    }

    public int read(byte[] b, int off, int len)throws IOException{
      len=in.read(b,off,len);
      if(len==-1){return -1;}
      count+=len;
      return len;
    }

    public void close(){
      if(pm!=null){pm.close();}
    }

    private class FaxCallProgressMonitor extends JPanel implements Runnable,ActionListener{

      JFrame       dialog=null;
      JProgressBar progressBar;
      JLabel       note,msg;

      Timer timer=null;
      int   time=0;

      public FaxCallProgressMonitor(String message){
        JPanel q=new JPanel();
        q.setLayout(new BorderLayout());
        Border border = q.getBorder();
        Border margin = new EmptyBorder(10,10,10,10);
        q.setBorder(new CompoundBorder(border, margin));

        JPanel r=new JPanel();
        r.setLayout(new BoxLayout(r,BoxLayout.PAGE_AXIS));
        r.add(new JLabel(new JarImageIcon(getClass(),"32x32/fax.png")));
        q.add(r,BorderLayout.WEST);

        JPanel p=new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));

        msg=new JLabel(message);
        p.add(msg);
        note=new JLabel(" ");
        p.add(note);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        p.add(progressBar);

        q.add(p,BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(q,BorderLayout.NORTH);
      }

      public void close(){
        timer.stop();
        if(dialog!=null){dialog.dispose();}
      }

      public void run(){
        timer=new Timer(500,this);
        timer.start();
      }

      public void setNote(String txt){
        note.setText(txt);
      }

      public void actionPerformed(ActionEvent e){
        time+=timer.getDelay();
        if(time>500){
          String s="File size: "+count+" in "+(time/1000)+" s ["+(count*1000/time)+" bps]";
          setNote(s);
          if(dialog==null){
            msg.setText("<html>"+msg.getText()+"<br><i>"+getFile()+"</i></html>");
            display();
          }
        }
      }

      void display() {
        dialog = new JFrame("Progress...");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setOpaque(true);
        dialog.setContentPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.toFront();
      }
    }
  }
}
