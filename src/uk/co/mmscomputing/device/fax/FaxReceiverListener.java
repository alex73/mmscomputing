package uk.co.mmscomputing.device.fax;

public interface FaxReceiverListener{
  public void update(FaxReceiverMetadata.Type type, FaxReceiverMetadata metadata);
}