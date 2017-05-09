package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;
import java.util.ArrayList;

/**
 * Created by Rodrigo on 31/03/2017.
 */
public class BinaryQuestion extends Question {

  public BinaryQuestion(String title, int mode) {
    super(title, mode);
    mOptions = new ArrayList<>(2);
    switch (mode) {
      case Constants.BINARY_MODE_YESNO:
        addOption("Yes");
        addOption("No");
        break;
      case Constants.BINARY_MODE_TRUEFALSE:
        addOption("True");
        addOption("False");
        break;
      case Constants.BINARY_MODE_CUSTOM:
      default:
        break;
    }
  }
}
