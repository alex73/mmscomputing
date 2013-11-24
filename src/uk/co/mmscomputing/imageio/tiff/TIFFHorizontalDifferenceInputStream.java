package uk.co.mmscomputing.imageio.tiff;

import java.io.*;

// [1] Adobe TIFF6.pdf

class TIFFHorizontalDifferenceInputStream extends FilterInputStream{

  private int width, spp, index;
  private int pixel[];

  public TIFFHorizontalDifferenceInputStream(InputStream in, int width, int spp){
    super(in);
    this.width = width * spp;
    this.spp   = spp;
    pixel      = new int[spp];
    index      = -1;
  }

  public int read()throws IOException{               // [1] p.64
    int b = in.read();
    if(b==-1){ return -1;}                           // end of file
    index++;
    if(index == width){ index = 0;}                  // new row
    int sample = index%spp;                          // pixel sample (colour)
    if(index < spp){                                 // if first pixel
      pixel[sample]  = (byte)b;                      // save samples
    }else{                                           // else 
      pixel[sample] += (byte)b;                      // apply horizontal difference 'predictor'
    }
    return pixel[sample];
  }
}