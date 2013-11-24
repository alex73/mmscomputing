package uk.co.mmscomputing.dsp.phone;

import java.io.*;

public class FSKTestUnit{

  static private final int EOF   = -1;
  static private final int QUIET = 0;
  static private final int MARK  = 1;               // 1300Hz
  static private final int SPACE = 2;               // 2100Hz

  static private final int[] markAmps={
    -8, 6784, 7296,  624,-6528,-7552,-1248, 6272, 7808, 1888,-5760,-7808,-2496, 5248, 8064, 3136,-4736,-8064,-3648, 4224, 
  8064, 4224,-3648,-8064,-4736, 3136, 8064, 5248,-2496,-7808,-5760, 1888, 7808, 6272,-1248,-7552,-6528,  624, 7296, 6784, 
     8,-6784,-7296, -624, 6528, 7552, 1248,-6272,-7808,-1888, 5760, 7808, 2496,-5248,-8064,-3136, 4736, 8064, 3648,-4224, 
 -8064,-4224, 3648, 8064, 4736,-3136,-8064,-5248, 2496, 7808, 5760,-1888,-7808,-6272, 1248, 7552, 6528, -624,-7296,-6784, 
  };

  static private final int[] spaceAmps={
    -8, 8064,-1248,-7808, 2496, 7552,-3648,-6784, 4736, 6272,-5760,-5248, 6528, 4224,-7296,-3136, 7808, 1888,-8064, -624,
  8064, -624,-8064, 1888, 7808,-3136,-7296, 4224, 6528,-5248,-5760, 6272, 4736,-6784,-3648, 7552, 2496,-7808,-1248, 8064,
     8,-8064, 1248, 7808,-2496,-7552, 3648, 6784,-4736,-6272, 5760, 5248,-6528,-4224, 7296, 3136,-7808,-1888, 8064,  624,
 -8064,  624, 8064,-1888,-7808, 3136, 7296,-4224,-6528, 5248, 5760,-6272,-4736, 6784, 3648,-7552,-2496, 7808, 1248,-8064,
  };

  static private final int[] mark2space={
     0, 1,66,59,52,45,38,31,24,17,10,37,76,69,62,55,48,41,34,27,
    20,13, 6,22,72,65,58,51,44,37,30,23,24, 9, 2,45,68,61,54,47,
    40,41,26,19,12, 5,78,71,64,57,50,77,36,29,22,15, 8, 1,74,67,
    60,53,46,62,32,25,18,11, 4,77,70,63,64,49,42, 5,28,21,14, 7,
  };

  static private final int[] space2mark={
     0,57,34,11,68,45,22,79,56,33,10,67,44,21,78,55,32, 9,66,43,
    20,77,54,31, 8,65,42,19,76,53,30, 7,64,41,18,45,52,29, 6,63,
    40,17,74,51,28, 5,62,39,16,73,50,27, 4,61,38,15,72,49,26, 3,
    60,37,14,71,48,25, 2,59,36,13,70,47,24, 1,58, 5,12,69,46,23,
  };

  static protected final int        max   = 7;
  static private   final double[][] retab = new double[max][max];
  static private   final double[][] imtab = new double[max][max];

  static protected double[]     au=new double[max];                   // buffer audio
  static protected double[]     re=new double[max];                   // buffer: real part
  static protected double[]     im=new double[max];                   // buffer: imaginary part

  public FSKTestUnit(){
  }

  static private int analyse(){
    double maxmag=0,rev,imv;
    int    maxfrq=0;

    for(int i=0;i<max;i++){                         // evaluate amplitude
      rev=0;imv=0;
      for(int k=0;k<max;k++){
        rev+=retab[i][k]*au[k];
        imv-=imtab[i][k]*au[k];
      }
      re[i]=rev;
      im[i]=imv;
    }

    for(int i=0;i<4;i++){                    
      rev = (Math.abs(re[i])<0.00001)?0.0:re[i];
      imv = (Math.abs(im[i])<0.001)?0.0:im[i];

      double mag = rev*rev+imv*imv;

      if(mag>maxmag){
        maxmag=mag;
        maxfrq=i;
      }
    }
    if(maxmag<1e6){ return QUIET;}
    if(maxfrq==3){  return QUIET;}
    return maxfrq;                                  // QUIET(0Hz),MARK(1300Hz),SPACE(2100Hz)
  }

  static{
    for(int i=0;i<max;i++){
      for(int k=0;k<max;k++){
        double tmp=2.0*Math.PI*((double)i)*((double)k)/((double)max);
        retab[i][k]=(1.0/(double)max)*Math.cos(tmp);
        imtab[i][k]=(1.0/(double)max)*Math.sin(tmp);
      }
    }
  }

  static void testm2s(int m,int testsym){
    System.err.println("MARK -> SPACE "+m+" "+((testsym==MARK)?"MARK":"SPACE"));
    for(int i=0;i<markAmps.length;i++){                            
      for(int j=m;j<au.length;j++){
        au[j]=markAmps[(i-j+m-1+markAmps.length)%markAmps.length];   // System.err.println("au["+j+"]="+au[j]);
      }
      int k=mark2space[i];                                           // System.err.println("k="+k);

      for(int j=0;j<m;j++){
        au[m-1-j]=spaceAmps[(k+j)%spaceAmps.length];                 // System.err.println("au["+(m-1-j)+"]="+au[m-1-j]);
      }
// System.err.println();

      int res = analyse();
      if(res!=testsym){
        System.err.println("["+i+"] = "+res+" "+markAmps[i]+" k="+k);
      }
    }
  }

  static void tests2m(int m,int testsym){
    System.err.println("SPACE -> MARK "+m+" "+((testsym==MARK)?"MARK":"SPACE"));
    for(int i=0;i<spaceAmps.length;i++){                            
      for(int j=m;j<au.length;j++){
        au[j]=spaceAmps[(i-j+m-1+spaceAmps.length)%spaceAmps.length];// System.err.println("au["+j+"]="+au[j]);
      }
      int k=space2mark[i];                                           // System.err.println("k="+k);

      for(int j=0;j<m;j++){
        au[m-1-j]=markAmps[(k+j)%markAmps.length];                   // System.err.println("au["+(m-1-j)+"]="+au[m-1-j]);
      }
// System.err.println();

      int res = analyse();
      if(res!=testsym){
        System.err.println("["+i+"] = "+res+" "+markAmps[i]+" k="+k);
      }
    }
  }

  public static void main(String[] argv){
    try{

      for(int i=0;i<markAmps.length;i++){
        for(int j=0;j<au.length;j++){
          au[j]=markAmps[(i-j+markAmps.length)%markAmps.length];
        }
        int res = analyse();
        if(res!=MARK){
          System.err.println("["+i+"] = "+res+" "+markAmps[i]);
        }
      }

      for(int i=0;i<spaceAmps.length;i++){
        for(int j=0;j<au.length;j++){
          au[j]=spaceAmps[(i-j+spaceAmps.length)%spaceAmps.length];
        }
        int res = analyse();
        if(res!=SPACE){
          System.err.println("["+i+"] = "+res+" "+spaceAmps[i]);
        }
      }
      System.err.println("\n\nSPACE -> MARK");
      tests2m(2,SPACE);
      tests2m(3,SPACE);
      tests2m(4,MARK);
      tests2m(5,MARK);
      tests2m(6,MARK);

      System.err.println("\n\nMARK -> SPACE");
      testm2s(2,MARK);
      testm2s(3,MARK);
      testm2s(4,SPACE);
      testm2s(5,SPACE);
      testm2s(6,SPACE);


    }catch(Exception e){
      e.printStackTrace();
    }
  }
}


// ETSI ES 201 912 v 1.2.1 (2004-06)
// ETS 300 659-1/2         (1997-02)

