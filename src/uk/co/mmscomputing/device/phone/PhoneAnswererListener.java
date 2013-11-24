package uk.co.mmscomputing.device.phone;

public interface PhoneAnswererListener{
  public void update(PhoneAnswererMetadata.Type type, PhoneAnswererMetadata metadata);
}