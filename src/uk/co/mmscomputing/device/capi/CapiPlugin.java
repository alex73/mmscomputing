package uk.co.mmscomputing.device.capi;

import uk.co.mmscomputing.util.metadata.*;

public interface CapiPlugin extends CapiConstants,MetadataListener{
  public void serve(CapiChannel channel);
}