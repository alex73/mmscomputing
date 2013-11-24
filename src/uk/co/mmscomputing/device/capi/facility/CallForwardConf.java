package uk.co.mmscomputing.device.capi.facility;

import uk.co.mmscomputing.device.capi.*;

public class CallForwardConf{

  static public class ActivateConf extends SupServiceConf{

    public ActivateConf(Rider r){
      super(r);
      info=r.readWord();                                // Supplementary Service Info
    }
  }

  static public class DeactivateConf extends SupServiceConf{

    public DeactivateConf(Rider r){
      super(r);
      info=r.readWord();                                // Supplementary Service Info
    }
  }

  static public class InterrogateParametersConf extends SupServiceConf{

    public InterrogateParametersConf(Rider r){
      super(r);
      info=r.readWord();                                // Supplementary Service Info
    }
  }

  static public class InterrogateNumbersConf extends SupServiceConf{

    public InterrogateNumbersConf(Rider r){
      super(r);
      info=r.readWord();                                // Supplementary Service Info
    }
  }
}


