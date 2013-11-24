package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.device.capi.exception.*;

public class CapiSystem extends Thread{

  private CapiMetadata          md      = null;
  private CapiServerApplication server  = null;

  private CapiSystem()throws CapiException{
    md=new CapiMetadata();

    md.useMaxLogicalConnections(CapiEnumerator.getNoOfBChannels());
    md.useALaw();                                        // set some defaults
    md.use64kBit();
    md.acceptAllCalls();
    md.setController(1);

    server=new CapiServerApplication(md);                // registers capi application, otherwise throws exception
    server.start();                                      // start listening to incoming capi calls
  }

  public int   getController(){                     return md.getController();}
  public void  setController(int no){               md.setController(no);}

  public CapiCallApplication getCaller(){           return server;}
  public CapiServerApplication getServer(){         return server;}

  public CapiPanel getGUI()throws CapiInformation{  return new CapiPanel(md);}

  public void run(){
    try{
      while(server.isRunning()){
        CapiChannel channel=server.accept();             // accept incoming calls here
        if(!server.isRunning()){break;}
        channel.getPlugin().serve(channel);              // call plugin
      }
    }catch(InterruptedException ie){
    }finally{
      server=null;
    }
//    System.err.println(getClass().getName()+".run: CLOSED");
  }

  public void close(){if(server!=null){server.close();}}

  public void addPlugin(CapiPlugin plugin){              
    md.addListener(0,plugin);
  }

  public void addPlugin(int index,CapiPlugin plugin){              
    md.addListener(index,plugin);

    printPlugins();
  }

  public void removePlugin(CapiPlugin plugin){
    md.removeListener(plugin);
  }

  public void printPlugins(){
    System.err.println(md);
  }

  static private CapiSystem system = null;

  synchronized static public CapiSystem getSystem()throws CapiException{
    if(system!=null){return system;}                     // if a capi system is already running return
    system=new CapiSystem();
    system.start();
    system.addPlugin(new CapiInfoPlugin());              // first plugin: will make capi application ignore calls by default
    return system;
  }
}



