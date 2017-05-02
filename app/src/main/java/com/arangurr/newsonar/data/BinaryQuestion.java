package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;

/**
 * Created by Rodrigo on 31/03/2017.
 */
public class BinaryQuestion extends Question {

  public BinaryQuestion(String title, int mode) {
    super(title);
    switch (mode) {
      case Constants.BINARY_MODE_YESNO:
        addOption("Yes");
        addOption("No");
        break;
      case Constants.BINARY_MODE_TRUEFALSE:
        addOption("True");
        addOption("False");
        break;
      case Constants.BINARY_MODE_UPDOWNVOTE: // TODO: 02/05/2017 move to "rate"
        addOption("Upvote");
        addOption("Downvote");
        break;
      default:
        setQuestionMode(Constants.BINARY_MODE_CUSTOM);
        break;
    }
  }
}
