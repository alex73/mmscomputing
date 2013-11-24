package uk.co.mmscomputing.imageio.pdf;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Locale;
import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class PDFImageReaderSpi extends ImageReaderSpi {

  static final String vendorName="mm's computing";
  static final String version="0.0.1";
  static final String readerClassName="uk.co.mmscomputing.imageio.pdf.PDFImageReader";
  static final String[] names={"pdf","PDF"};
  static final String[] suffixes={"pdf","PDF"};
  static final String[] MIMETypes={"image/pdf"};
  static final String[] writerSpiNames={"uk.co.mmscomputing.imageio.pdf.PDFImageWriterSpi"};

  static final boolean supportsStandardStreamMetadataFormat = false;
  static final String nativeStreamMetadataFormatName = null;
  static final String nativeStreamMetadataFormatClassName = null;
  static final String[] extraStreamMetadataFormatNames = null;
  static final String[] extraStreamMetadataFormatClassNames = null;
  static final boolean supportsStandardImageMetadataFormat = false;
  static final String nativeImageMetadataFormatName =null;//"uk.co.mmscomputing.imageio.pdf.PDFFormatMetadata 0.0.1";
  static final String nativeImageMetadataFormatClassName =null;//"uk.co.mmscomputing.imageio.pdf.PDFFormatMetadata";
  static final String[] extraImageMetadataFormatNames = null;
  static final String[] extraImageMetadataFormatClassNames = null;

  public PDFImageReaderSpi(){
    super(	vendorName,		version,
		names,			suffixes,
		MIMETypes,		readerClassName,
		STANDARD_INPUT_TYPE,	writerSpiNames,
		supportsStandardStreamMetadataFormat,
		nativeStreamMetadataFormatName,
		nativeStreamMetadataFormatClassName,
		extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
		supportsStandardImageMetadataFormat,
		nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
		extraImageMetadataFormatClassNames
    );
  }

  public ImageReader createReaderInstance(Object extension)throws IOException{
    return new PDFImageReader(this);
  }

  public boolean canDecodeInput(Object source)throws IOException{
    if(!(source instanceof ImageInputStream)) { return false; }

    ImageInputStream stream = (ImageInputStream)source;
    String header = "%PDF-1.1";
    byte[] buffer = new byte[header.length()];

    try{
      stream.mark();
      stream.readFully(buffer);
      stream.reset();
    }catch(IOException e){
      return false;
    }

    return header.equals(new String(buffer));

/*
    ImageInputStream stream = (ImageInputStream)source;
    stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    byte[] type = new byte[3];
    byte[] version = new byte[3];
    try{
      stream.mark();
      stream.readFully(type);
      stream.readFully(version);
      stream.reset();
    }catch(IOException e){
      return false;
    }
    if((type[0]=='G')&&(type[1]=='I')&&(type[2]=='F')){
      if((version[0]=='8')&&(version[1]=='7')&&(version[2]=='a')){
        return true;
      }
      if((version[0]=='8')&&(version[1]=='9')&&(version[2]=='a')){
        return true;
      }
    }
    return false;
*/
  }

  public String getDescription(Locale locale){
    return "mmsc pdf decoder";
  }
}