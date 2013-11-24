package uk.co.mmscomputing.imageio.pdf;

import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class PDFScanner implements PDFConstants{

  private static int[]       charTable;
  private static Dictionary  keywords; 

  private ImageInputStream in;
  private int         c;

  private int         sym;
  public  int         intval;
  public  double      realval;
  public  String      name="";
  public  String      str;

  public  int         symbol;

  private long        length; 
  private PDFBody     body;

  public PDFScanner(PDFBody body,ImageInputStream in)throws IOException{
    this.body = body;
    this.in   = in;
    c=' ';
    symbol=0;
    length=-1;
  }

  public PDFIndirectObject getIndirectObject(int on,int gn){
    return body.getIndirectObject(on,gn);  
  }

  public void seek(long pos)throws IOException{
    in.seek(pos);
    c=' ';
    symbol=0;
    rindex=0;
    scan();
  }

  public long getLength()throws IOException{
    if(length==-1){
      length = in.length();
      if(length==-1){                                 // we need to go to the end of the file
        byte[] buf=new byte[32*1024];
        length=0;
        int count;
        while((count=in.read(buf,0,buf.length))!=-1){
          length+=count;
        }
      }
    }
    return length;
  }

  public void find(int token)throws IOException{
    do{
      scan();
      if(symbol==T_EOF){
        throw new IOException(getClass().getName()+".find\n\t Cannot find token: "+token+". Unexpected EOF.");
      }
    }while(symbol!=token);
  }

  private void read()throws IOException{
    c=in.read();                                  // System.err.println(""+c+" "+Integer.toHexString(c)+" "+(char)c);
    sym=(c!=-1)?charTable[c]:-1;
  }

  public String readLine()throws IOException{
    int    b;
    String line = "";
    while((b=in.read())!=-1){                     // System.err.println(""+b+" "+Integer.toHexString(b)+" "+(char)b);
      if((b=='\n')||(b=='\r')){break;}
    }
    return line;
  }

  public void scanEOL()throws IOException{
    while((c!='\r')&&(c!='\n')&&(c!=-1)){
      read();
    }
    if(c=='\r'){           // msdos: \r\n 0D0A
      read();
      if(c=='\n'){
        read();
      }
    }else if(c=='\n'){     // unix: \n 0A
      read();
    }
  }

  public byte[] scanStream(byte[] buf)throws IOException{
    scanEOL();
    if(c==-1){
      throw new IOException(getClass().getName()+".readStream:\n\t Unexpected EOF.");
    }
    buf[0]=(byte)c;
    int count = in.read(buf,1,buf.length-1)+1;        // read stream of bytes
    if(count!=buf.length){
      throw new IOException(getClass().getName()+".readStream:\n\t Not enough bytes in inputstream! ["+count+" != "+buf.length+"]");
    }
    read();                                           // read next character; should be eol symbol or (e)ndstream
    return buf;
  }

  public byte[] scanStream(int len)throws IOException{
    return scanStream(new byte[len]);
  }

  public String scanComment()throws IOException{
    str="";
    read();
    while((c!='\n')&&(c!='\r')&&(c!=-1)){
      str+=(char)c;
      read();
    }
    if(c=='\r'){           // msdos: \r\n 0D0A
      read();
      if(c=='\n'){
        read();
      }
    }else if(c=='\n'){     // unix: \n 0A
      read();
    }
    return str;
  }

  private void scanName()throws IOException{
    str="";
    read();                                                // first character must be letter
    while((sym==T_CHAR)||(sym==T_INTEGER)){
      str+=(char)c;
      read();
    }
    symbol=T_NAME;                                         // System.err.println("Name = "+str);
  }

  private void scanString()throws IOException{
    str="";
    read();
    while(sym!=T_STRING_END){
      if((' '<=c)&&(c<=127)){
        if(c=='\\'){                                        // escape character
          read();
          if((c=='\n')||(c=='\r')){                         // todo: \r\n
            read(); continue;
          }else if(c=='t'){         c='\t';
          }else if(c=='b'){         c='\b';
          }else if(c=='f'){         c='\f';
          }else if(c=='\\'){        c='\\';
          }else if(c=='('){         c='(';
          }else if(c==')'){         c=')';
          }else if(('0'<=c)&&(c<='7')){                     // todo: octal   \d or \dd or \ddd i.e. \245
          }
        }
        str+=(char)c;
      }else{
        System.err.println(getClass().getName()+".scanLiteral:\n\tInvalid character[0x"+Integer.toHexString(c)+"] in literal.");
      }
      read();
    }
    read();
    symbol=T_STRING;
  }

  private void scanHexString()throws IOException{
    char b;
    int  h,l;

    str = "";

    while(c!='>'){
      h=0;l=0;
      if(c==-1){
        System.err.println(getClass().getName()+".scanHexString:\n\tMissing '>'");
        break;
      }
      if(('0'<=c)&&(c<='9')){          h=c-'0';
      }else if(('A'<=c)&&(c<='F')){    h=c-'A'+10;
      }else if(('a'<=c)&&(c<='f')){    h=c-'a'+10;
      }else{
        System.err.println(getClass().getName()+".scanHexString:\n\tWrong character '"+((char)c)+"'");
      }
      read();
      if(c==-1){
        System.err.println(getClass().getName()+".scanHexString:\n\tMissing '>'");
        break;
      }
      if(c=='>'){                      l=0;
      }else{
        if(('0'<=c)&&(c<='9')){        l=c-'0';
        }else if(('A'<=c)&&(c<='F')){  l=c-'A'+10;
        }else if(('a'<=c)&&(c<='f')){  l=c-'a'+10;
        }else{
          System.err.println(getClass().getName()+".scanHexString:\n\tWrong character '"+((char)c)+"'");
        }
        read();
      }
      b = ((char)((h<<4)|l));
      str+=b;
    }
    read();
    symbol=T_STRING;
  }

  private void scanIdentifier()throws IOException{
    str="";
    while((sym==T_CHAR)||(sym==T_INTEGER)){
      str+=(char)c;
      read();
    }
    name=str;
    Integer val=(Integer)keywords.get(name);
    if(val!=null){
      symbol=val.intValue();
    }else{
      symbol=T_NAME;
    }
  }

  private void scanReal()throws IOException{
    symbol=T_REAL;
    realval=intval;
    scanNumber();
  }

  private void scanNumber()throws IOException{
    int        i,j;
    int        d;
    int[]      dig=new int[11];
    boolean    hex;

    hex=false;
    dig[0]=c-0x30;
    i=0;
    while(true){
      if(i==(dig.length-1)){System.err.println(getClass().getName()+".scanNumber:\n\tToo many digits["+i+"] in number.");break;}
      read();
      i+=1;
      if(c=='.'){break;}
      if(c<'0'){break;}
      if(c<='9'){
        dig[i]=c-0x30;
      }else if(('a'<=c)&&(c<='f')){
        hex=true;dig[i]=c-'a'+10;
      }else if(('A'<=c)&&(c<='F')){
        hex=true;dig[i]=c-'A'+10;
      }else{
        break;
      }
    }
    if(c=='.'){
      read();
      scanReal();
      return;
    }
    if((c=='H')||(c=='h')){                      // hexadecimal
      read();
      j=0;
      while(!((i-j==8)||(i-j==4)||(i-j==2))&&(j<i-1)&&(dig[j]==0)){
        j+=1;
      }
      if(i>8){j=i-8;}
      intval=dig[j];
      if(intval>=8){intval-=0x10;}
      j+=1;
      while(j<i){
        intval=intval*0x10+dig[j];j+=1;
      }
      symbol=T_INTEGER;
    }else if(!hex){                              // decimal
        j=0;
        while((j<i-1)&&(dig[j]==0)){
            j+=1;
        }
        intval=0;
        do{
          intval=intval*10+dig[j];j+=1;
        }while(j<i);
        if(c=='X'){                              // char
          read();
          if((intval<0)||(255<intval)){
            System.err.println(getClass().getName()+".scanNumber:\n\tChar ordinal["+intval+"] out of range.");
          }
          symbol=T_CHAR;
        }else{
          symbol=T_INTEGER;
        }
    }else{
      System.err.println(getClass().getName()+".scanNumber:\n\tInvalid number.");
      intval=0;
      symbol=0;
    }
    intval=intval;
  }

  public void scanStartXRefNumber()throws IOException{
    symbol=0;
    while(sym==T_WHITE){read();}
    if(sym!=T_INTEGER){
      throw new IOException(getClass().getName()+".scanStartXRefNumber:\n\tMissing startXRef offset.");
    }
    scanNumber();
  }

  private void scan1()throws IOException{
    symbol=0;
    while(sym==T_WHITE){read();}
    switch(sym){
    case T_CHAR:         scanIdentifier(); break;
    case T_INTEGER:      scanNumberR();    break;
    case T_NAME:         scanName();       break;      // /Name
    case T_STRING_START: scanString();     break;      // ( string )
    case T_LSS:
      read();
      if(sym==T_LSS){                                  // <<
        symbol=T_DICTIONARY_START;
        read();
      }else{                                           // <string as hex>
        scanHexString();
      }
      break;
    case T_COMMENT:                                    // %...EOL
      scanComment();
      scan1();
      break;
    case T_GTR:
      read();
      if(sym==T_GTR){                                  // >>
        symbol=T_DICTIONARY_END;
        read();
      }
      break;
    default:
      symbol=sym;
      read();
      break;
    }
  }

  private int   rmax     = 3;
  private int   rindex   = 0;
  public  int[] rvalues  = new int[rmax];
  private int[] rsymbols = new int[rmax];

  public  int   objectNumber,generationNumber;

  private void scanNumberR1()throws IOException{
    if(rindex<rmax){
      while(sym==T_WHITE){read();}
      if(sym==T_INTEGER){
        scanNumberR();
      }else{
        scan1();
        rsymbols[rindex++]=symbol;
      }
    }
  }

  private void scanNumberR()throws IOException{
    scanNumber();
    rsymbols[rindex] = symbol;
    rvalues [rindex] = intval;
    rindex++;
    scanNumberR1();
  }

  private void remove(){
    intval = rvalues[0];
    symbol = rsymbols[0];
    rindex--;
    for(int i=0;i<rindex;i++){
      rvalues[i]=rvalues[i+1];
      rsymbols[i]=rsymbols[i+1];
    }
  }

  public void scan()throws IOException{
    if(rindex>0){
      if(rsymbols[rindex-1]==T_INTEGER){
        scanNumberR1();
        if(symbol==T_R){                               // System.err.println("a rindex = "+rindex);
          rindex-=2; generationNumber = rvalues[rindex];
          rindex-=1; objectNumber     = rvalues[rindex];
          rsymbols[rindex++] = symbol;
        }
      }
      remove();
    }else{
      scan1();
      if(symbol==T_R){                                 // System.err.println("b rindex = "+rindex);
        rindex-=2; generationNumber = rvalues[rindex];
        rindex-=1; objectNumber     = rvalues[rindex];
      }else if(rindex>0){
        remove();
      }
    }
  }

  static{
    int i;

    charTable = new int[256];
    for(i=0;i<256;i++){
      charTable[i]=T_WHITE;
    }
    for(i='!';i<127;i++){
      charTable[i]=T_CHAR;
    }
    for(i='0';i<='9';i++){
      charTable[i]=T_INTEGER;
    }

    charTable['%']=T_COMMENT;
    charTable['(']=T_STRING_START;
    charTable[')']=T_STRING_END;
    charTable['<']=T_LSS;
    charTable['>']=T_GTR;
    charTable['[']=T_ARRAY_START;
    charTable[']']=T_ARRAY_END;
    charTable['{']=T_LBRACE;
    charTable['}']=T_RBRACE;
    charTable['/']=T_NAME;

    keywords  = new Hashtable(); 

    keywords.put("null",      new Integer(T_NULL));
    keywords.put("false",     new Integer(T_FALSE));
    keywords.put("true",      new Integer(T_TRUE));

    keywords.put("stream",    new Integer(T_STREAM));
    keywords.put("endstream", new Integer(T_ENDSTREAM));

    keywords.put("obj",       new Integer(T_OBJ));
    keywords.put("endobj",    new Integer(T_ENDOBJ));

    keywords.put("R",         new Integer(T_R));
    keywords.put("xref",      new Integer(T_XREF));
    keywords.put("trailer",   new Integer(T_TRAILER));
    keywords.put("startxref", new Integer(T_STARTXREF));
    keywords.put("n",         new Integer(T_N));
    keywords.put("f",         new Integer(T_F));
  }

  public static void main(String[] argv){
    try{
      String           test = "335566\n%%EOF\n [1] [1 2 ] [1 2 3] [1 2 3 4] [1 2 3 4 5] [1 2 R] [0 1 2 R] [0 1 R 2 3 R] [0 1 2 3 4 R] [0 1 2 3 4  5 R] [0 1 0 R 2 0 R 3 0][0] [0 1] (Klasse wie geht's\\t?\\\n Hallo)  true \\ << 101 10.2 >>  [false  null]  /NA***;_ME<41>  %comment\n <4142303961>";
      InputStream      in   = new ByteArrayInputStream(test.getBytes());
      ImageInputStream iin  = ImageIO.createImageInputStream(in);
      PDFScanner       s    = new PDFScanner(null,iin);

      while(s.symbol!=T_EOF){
        s.scan();      
        switch(s.symbol){
        case T_STRING: System.err.println("string = "+s.str); break;
        case T_NAME:   System.err.println("name   = "+s.str); break;
        case T_TRUE:   System.err.println("true   = "+s.str); break;
        case T_FALSE:  System.err.println("false  = "+s.str); break;
        case T_NULL:   System.err.println("null   = "+s.str); break;
        case T_INTEGER:   System.err.println("int = "+s.intval); break;
        case T_REAL:   System.err.println("real = "+s.realval); break;
        case T_ARRAY_START:      System.err.println("array start"); break;
        case T_ARRAY_END:        System.err.println("array end");   break;
        case T_DICTIONARY_START: System.err.println("dict start");  break;
        case T_DICTIONARY_END:   System.err.println("dict end");    break;
        case T_R:      System.err.println("R "+s.objectNumber+" "+s.generationNumber); break;
        default:       System.err.println("symbol    = "+s.symbol); break;
        }
      }      
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}










