package uk.co.mmscomputing.application.install;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import java.io.*;

public class Log extends JPanel{
  SimpleAttributeSet black, blue, red;
  JTextPane	pane;
  Document 	doc;

  public Log(){
    black=new SimpleAttributeSet(); StyleConstants.setForeground(black,Color.black);
    blue=new SimpleAttributeSet(); StyleConstants.setForeground(blue,Color.blue);
    red=new SimpleAttributeSet(); StyleConstants.setForeground(red,Color.red);

    SimpleDateFormat defaultDate=new SimpleDateFormat("yyyy-MM-dd--hh-mm-ss");
    String date=defaultDate.format(new Date());
    pane=new JTextPane();
    pane.setFont(new Font("Courier",Font.PLAIN,12));
    pane.setText("Log started at : "+date+"\n\n");
    doc=pane.getDocument();

    setLayout(new BorderLayout());
    add(new JScrollPane(pane),BorderLayout.CENTER);
  }

  private void write(String s, SimpleAttributeSet a){
    try{
      doc.insertString(0,s+"\n",a);
//      doc.insertString(doc.getLength(),s+"\n",a);
    }catch(/*BadLocation*/Exception e){
      System.err.println(e.getMessage());
      System.err.println(s);
    }
  }

  public void msg(InputStream in){
    try{
      BufferedReader r=new BufferedReader(new InputStreamReader(in));
      String s="";
      while((s=r.readLine())!=null){
        write(s,black);
      }
      in.close();
    }catch(IOException ioe){
      msg(ioe.getMessage());
    }
  }

  public void msg(String s){
    write(s,black);
    repaint();
//    JOptionPane.showMessageDialog(null, s, "Message", JOptionPane.ERROR_MESSAGE); 
  }

//  public void msg(Object s){
//    msg(s.toString());
//  }

  public void err(String s){
    System.err.println(s);
    write(s,red);
    JOptionPane.showMessageDialog(null, s, "Exception", JOptionPane.ERROR_MESSAGE); 
  }

  public void err(Exception e){
    e.printStackTrace();
//  System.err.println("err : "+e.getMessage());
    write(e.getMessage(),blue);
    JOptionPane.showMessageDialog(null, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE); 
 }

  public void err(String s, Exception e){
    e.printStackTrace();
//  System.err.println("err : "+e.getMessage());
//  System.err.println(s);
    write(s+"\r\n"+e.getMessage(),blue);
    JOptionPane.showMessageDialog(null, s+"\r\n"+e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE); 
  }


}