package uk.co.mmscomputing.application.answerphone;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import uk.co.mmscomputing.device.phone.*;
import uk.co.mmscomputing.application.phone.*;

public class PhoneFaxTab extends uk.co.mmscomputing.application.phone.PhoneTab{

  public PhoneFaxTab(Properties properties,PhoneBook phonebook){
    super(properties,phonebook);
  }

  protected AnswerPanel getAnswerPanel(Properties properties,PhoneBook phonebook){
    return new AnswerFaxPanel(properties,phonebook);
  }

  protected class AnswerFaxPanel extends AnswerPanel{

    public AnswerFaxPanel(Properties properties,PhoneBook phonebook){
      super(properties,phonebook);
    }
  }
}