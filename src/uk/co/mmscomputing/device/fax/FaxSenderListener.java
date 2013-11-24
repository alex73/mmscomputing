package uk.co.mmscomputing.device.fax;

public interface FaxSenderListener{
  public void update(FaxSenderMetadata.Type type, FaxSenderMetadata metadata);
}