package uk.co.mmscomputing.util.configuration;

import java.io.*;
import java.util.*;

import uk.co.mmscomputing.util.log.*;

public class ConfigurationWriter extends FilterWriter{

  public ConfigurationWriter(Writer out){
    super(new PrintWriter(new BufferedWriter(out)));
  }

  public ConfigurationWriter(String filename)throws IOException{
    this(new FileWriter(filename));
  }

  public void write(Map map)throws IOException{
    PrintWriter out = (PrintWriter)this.out;
    Set set=map.entrySet();
    Iterator iterator=set.iterator();
    while(iterator.hasNext()){
      Map.Entry entry=(Map.Entry)iterator.next();
      String key=(String)entry.getKey();
      Object value=entry.getValue();
      if(value instanceof String){
        String s=(String)value;
        s=s.replace(File.separatorChar,'/');  // change i.e. windows backslash to java '/'
        out.println(key+" = "+"\""+s+"\";");
      }
    }
  }

}