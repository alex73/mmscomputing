package uk.co.mmscomputing.application.faxreceiver;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.device.fax.*;
import uk.co.mmscomputing.device.phone.*;

public class FaxReceiverTab extends FaxReceiverPanel{

  public FaxReceiverTab(Properties properties,PhoneBook phonebook){
    super(properties,phonebook);
  }

  public FaxCallHandler getHandler(Properties properties){
//    FaxCallHandler handler=new FaxCallSaver();
    FaxCallSaver    sffhandler=new FaxCallSffSaver();
    FaxCallMonitor  handler=new FaxCallMonitor(sffhandler);
    handler.init(properties);
    return handler;
  }
}