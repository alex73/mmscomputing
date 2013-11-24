package uk.co.mmscomputing.device.fax;

import java.util.*;

public interface FaxCallHandlerFactory{
  public FaxCallHandler getHandler(Properties properties);
}