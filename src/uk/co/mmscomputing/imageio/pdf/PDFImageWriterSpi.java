package uk.co.mmscomputing.imageio.pdf;

import java.io.IOException;
import java.util.Locale;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

public class PDFImageWriterSpi extends ImageWriterSpi{

  static final String vendorName="mm's computing";
  static final String version="0.0.1";
  static final String writerClassName="uk.co.mmscomputing.imageio.pdf.PDFImageWriter";
  static final String[] names={"pdf","PDF"};
  static final String[] suffixes={"pdf","PDF"};
  static final String[] MIMETypes={"image/pdf"};
  static final String[] readerSpiNames=null;//{"uk.co.mmscomputing.imageio.pdf.PDFImageReaderSpi"};

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

  public PDFImageWriterSpi(){
    super(      vendorName,   version,
                names,        suffixes,
                MIMETypes,    writerClassName,
                STANDARD_OUTPUT_TYPE,	readerSpiNames,
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

  public ImageWriter createWriterInstance(Object extension)throws IOException{
    return new PDFImageWriter(this);
  }

  public boolean canEncodeImage(ImageTypeSpecifier type){
// todo
//    int t=type.getBufferedImageType();
//    return (t==BufferedImage.TYPE_INT_RGB)||(t==BufferedImage.TYPE_BYTE_GRAY);
    return true;
  }

  public String getDescription(Locale locale){
    return "mmsc pdf encoder";
  }

}