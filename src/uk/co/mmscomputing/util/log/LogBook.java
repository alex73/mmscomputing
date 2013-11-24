package uk.co.mmscomputing.util.log;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import uk.co.mmscomputing.concurrent.*;

public class LogBook extends JPanel{

  static SimpleAttributeSet black, blue, green, red;

  JTextPane	         pane;
  Document 	         doc;
  Semaphore          blocker=new Semaphore(1,true);

  public LogBook(){this(false);}

  public LogBook(boolean redirectSystemErr){
    pane=new JTextPane();
    pane.setFont(new Font("Courier",Font.PLAIN,12));
    doc=pane.getDocument();

    setLayout(new BorderLayout());
    add(new JScrollPane(pane),BorderLayout.CENTER);

    LogBookStream lbs=new LogBookStream(this);
    lbs.redirectSystemOut();
    if(redirectSystemErr){lbs.redirectSystemErr();}
    System.out.println("Log started.");
  }

  private void write(String s, SimpleAttributeSet a){
    try{
      blocker.acquire();
      doc.insertString(doc.getLength(),s,a);
      blocker.release();
    }catch(Exception e){
//      System.err.println(e.getMessage());
//      System.err.println(s);
    }
  }

  public void write(final int level,final String s){
    new Thread(){
      public void run(){
        switch(level){
        case 0:
          write(s,black);
          break;
        case 1:case 2:case 3:
          write(s,blue);
          break;
        case 4:case 5:case 6:
          write(s,green);
          new Thread(){
            public void run(){
              try{
                blocker.acquire();
                JOptionPane.showMessageDialog(null, s.substring(s.indexOf('\t')), "Message", JOptionPane.ERROR_MESSAGE); 
                blocker.release();
              }catch(Exception e){}
            }
          }.start();
          break;
        case 7:case 8:case 9:
          write(s,red);
          new Thread(){
            public void run(){
              try{
                blocker.acquire();
                JOptionPane.showMessageDialog(null, s.substring(s.indexOf('\t')), "Message", JOptionPane.ERROR_MESSAGE); 
                blocker.release();
              }catch(Exception e){}
            }
          }.start();
          break;
        }
        repaint();
      }
    }.start();
  }

  static{
    black=new SimpleAttributeSet(); StyleConstants.setForeground(black,Color.black);
    blue =new SimpleAttributeSet(); StyleConstants.setForeground(blue,Color.blue);
    green=new SimpleAttributeSet(); StyleConstants.setForeground(green,Color.green);
    red  =new SimpleAttributeSet(); StyleConstants.setForeground(red,Color.red);
  }
}