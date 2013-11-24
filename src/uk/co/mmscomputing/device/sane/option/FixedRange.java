package uk.co.mmscomputing.device.sane.option;

import uk.co.mmscomputing.device.sane.SaneIOException;

public class FixedRange extends FixedDesc{

  int min,max,quant;
 
  public FixedRange(
    int      handle,
    int      no,

    String   name,
    String   title,
    String   desc,
    int      type,
    int      unit,
    int      size,
    int      cap,
    int      min,
    int      max,
    int      quant,
    int[]    values
  ){
    super(handle,no,name,title,desc,type,unit,size,cap,values);
    this.min=min;this.max=max;this.quant=quant;
  }

  public int setWordValue(int i, int val)throws SaneIOException{
    if(quant!=0){
      val=Math.abs(val/quant)*quant;
    }
    if(val<min){
      val=min;
    }else if(max<val){
      val=max;
    }
    return super.setWordValue(i,val);
  }

  public double getMinValue(){ return unfix(min);}
  public double getMaxValue(){ return unfix(max);}
  public double getMinQuant(){ return unfix(quant);}

  public String toString(){
    String s=super.toString();
    s+="\n    min   "+Integer.toHexString(min)+"    "+getMinValue();
    s+="\n    max   "+Integer.toHexString(max)+"    "+getMaxValue();
    s+="\n    quant "+Integer.toHexString(quant)+"    "+getMinQuant();
    return s;
  }

  public DescriptorPanel getGUI(){
    gui=new FixedRangePanel(this);
    return gui;
  }

}

