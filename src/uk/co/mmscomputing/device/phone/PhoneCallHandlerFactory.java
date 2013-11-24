package uk.co.mmscomputing.device.phone;

import java.util.*;

public interface PhoneCallHandlerFactory{
  public PhoneCallHandler getHandler(Properties properties);
}