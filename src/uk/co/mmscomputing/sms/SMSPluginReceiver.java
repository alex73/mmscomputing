package uk.co.mmscomputing.sms;

public interface SMSPluginReceiver{
  public void update(SMSMetadata.Type type,SMSMetadata md);
}
