package uk.co.mmscomputing.device.capi.samples;

import uk.co.mmscomputing.device.capi.*;

public class GetProfile{

  public static void main(String[] argv){
    try{
      CapiEnumerator e=new CapiEnumerator();
      System.err.println(e.toString());
    }catch(Exception e){
      System.err.println(e);
      e.printStackTrace();
    }
  }
}
