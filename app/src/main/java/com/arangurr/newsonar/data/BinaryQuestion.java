package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;

/**
 * Created by Rodrigo on 31/03/2017.
 */
public class BinaryQuestion extends Question {

  public BinaryQuestion() {
    super();
  }

  public BinaryQuestion(String title) {
    super(title);
  }

  public BinaryQuestion(String title, int mode) {
    super(title);
    switch (mode) {
      case Constants.BINARY_MODE_YESNO:
        addOption(new Option("Yes"));
        addOption(new Option("No"));
        break;
      case Constants.BINARY_MODE_TRUEFALSE:
        addOption(new Option("True"));
        addOption(new Option("False"));
        break;
      case Constants.BINARY_MODE_UPDOWNVOTE:
        addOption(new Option("Upvote"));
        addOption(new Option("Downvote"));
        break;
      default:
        setQuestionMode(Constants.BINARY_MODE_CUSTOM);
        break;
    }
  }


}
