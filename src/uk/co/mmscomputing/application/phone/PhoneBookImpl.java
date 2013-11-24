package uk.co.mmscomputing.application.phone;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.device.phone.*;
import uk.co.mmscomputing.device.phone.*;

public class PhoneBookImpl implements PhoneBook,PhoneConstants{

  Properties   properties;
  TreeMap[]    sets=new TreeMap[2];    // 0: key=numbers; 1: key=names;

  public PhoneBookImpl(Properties properties)throws IOException{
    this.properties=properties;

    String fn=properties.getProperty(phoneBookID);
    if(fn==null){
      fn= System.getProperty("user.home")
          +File.separator
          +"mmsc"
          +File.separator
          +"phonebook"
          +File.separator
          +"phonebook.csv";
      properties.setProperty(phoneBookID,new File(fn).getAbsolutePath());
    }

    for(int i=0;i<sets.length;i++){sets[i]=new TreeMap();}
  }

  public void write(){
    try{
      String fn=properties.getProperty(phoneBookID);
      File   f=new File(fn);
      f.getParentFile().mkdirs();
      PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(f)));
      Set set=sets[0].entrySet();
      Iterator iterator=set.iterator();
      while(iterator.hasNext()){
        Map.Entry entry=(Map.Entry)iterator.next();
        String key=(String)entry.getKey();
        Object value=entry.getValue();
        if(value instanceof String){
          String s=(String)value;
          out.println("\""+key+"\" , "+"\""+s+"\"");
        }
      }
      out.flush();
      out.close();
    }catch(Exception e){
      System.out.println("9\b"+e.getMessage());
    }
  }

  public void read(){
    File f=new File(properties.getProperty(phoneBookID));
    if(!f.exists()){return;}
    try{
      BufferedReader  in = new BufferedReader(new FileReader(f));
      StreamTokenizer st = new StreamTokenizer(in);
      st.wordChars('_','_');
      int t=0;String number="",name="";
      while((t=st.nextToken())=='"'){
        number=st.sval;                              
        if((t=st.nextToken())!=','){ 
          throw new IOException("Expect [=] have ["+t+" , "+((char)t)+"]");
        }
        t=st.nextToken();
        switch(t){
          case '"':
            name=st.sval;                          
          break;
          default: throw new IOException("Expect [Quoted String] have ["+t+"]");
        }
        sets[0].put(number,name);                     // key=number
        sets[1].put(name,number);                     // key=name
      }
    }catch(Exception e){
      System.out.println("9\b"+e.getMessage());
    }
  }

  public Set entrySet(int set){
    return sets[set].entrySet();
  }

  public Object put(Object number,Object name){
//    while(((String)number).startsWith("0")){number=((String)number).substring(1);}
    sets[1].put(name,number);  
    return sets[0].put(number,name);  
  }

  public Object get(Object number){
//    while(((String)number).startsWith("0")){number=((String)number).substring(1);}
    return sets[0].get(number);  
  }

  public Object remove(Object number){
//    while(((String)number).startsWith("0")){number=((String)number).substring(1);}
    sets[1].remove(sets[0].get(number));
    return sets[0].remove(number);
  }

  public String getName(String number){
//    while(number.startsWith("0")){number=number.substring(1);}
    String name=(String)sets[0].get(number);
    if(name!=null){return name;}
    return (String)number;
  }

  public String getNumber(String name){
    String number=(String)sets[1].get(name);
    if(number!=null){return number;}
    return (String)name;
  }

}