package uk.co.mmscomputing.device.capi.samples;

import java.io.*;
import javax.sound.sampled.AudioFormat;

import uk.co.mmscomputing.sound.*;
import uk.co.mmscomputing.device.capi.sound.*;

public class Raw2WaveConverter{

  static public final AudioFormat pcmformat = new AudioFormat(8000,16,1,true,false);
  static public final AudioFormat alawformat= new AudioFormat(AudioFormat.Encoding.ALAW,8000,8,1,1,8000,false);
  static public final AudioFormat ulawformat= new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);

  public static void main(String[] argv){
    try{
      WaveOutputFile out;InputStream in;

      in=new FileInputStream("uk/co/mmscomputing/device/capi/samples/capture.raw");
      in=new PCMInputStream(in,true);

      out=new WaveOutputFile("uk/co/mmscomputing/device/capi/samples/capturePCM.wav",pcmformat);

      int count=0;
      byte[] buffer=new byte[2048];
      while((count=in.read(buffer))>0){
        out.write(buffer,0,count);
      }
      out.close();


      in=new FileInputStream("uk/co/mmscomputing/device/capi/samples/capturePCM.wav");
      for(int i=0;i<58;i++){
        int b = in.read();
        System.err.println("["+i+"] "+b+" 0x"+Integer.toHexString(b)+" "+(char)b);
      }
      in.close();

    }catch(Exception e){
      System.err.println(e.getMessage());
    }
    try{
      WaveOutputFile out;InputStream in;

      in=new FileInputStream("uk/co/mmscomputing/device/capi/samples/capture.raw");
      in=new LawInputStream(in);

      out=new WaveOutputFile("uk/co/mmscomputing/device/capi/samples/captureALaw.wav",alawformat);

      int count=0;
      byte[] buffer=new byte[2048];
      while((count=in.read(buffer))>0){
        out.write(buffer,0,count);
      }
      out.close();
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
    try{
      WaveOutputFile out;InputStream in;

      in=new FileInputStream("uk/co/mmscomputing/device/capi/samples/capture.raw");
      in=new LawInputStream(in);
      in=new ConvertInputStream(in,true);

      out=new WaveOutputFile("uk/co/mmscomputing/device/capi/samples/captureuLaw.wav",ulawformat);

      int count=0;
      byte[] buffer=new byte[2048];
      while((count=in.read(buffer))>0){
        out.write(buffer,0,count);
      }
      out.close();
    }catch(Exception e){
      System.err.println(e.getMessage());
    }
  }
}