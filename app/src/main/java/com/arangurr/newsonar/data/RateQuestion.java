package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;
import java.util.ArrayList;

/**
 * Created by Rodrigo on 08/05/2017.
 */

public class RateQuestion extends Question {

  public RateQuestion(String title, int mode) {
    super(title, mode);
    switch (mode) {
      case Constants.RATE_MODE_STARS:
        mOptions = new ArrayList<>(4); // 1 to 5 stars
        for (int i = 1; i <= 5; i++) {
          addOption(String.valueOf(i));
        }
        break;
      case Constants.RATE_MODE_LIKEDISLIKE:
        mOptions = new ArrayList<>(2);
        addOption(String.valueOf(0));
        addOption(String.valueOf(1));
        break;
      case Constants.RATE_MODE_SCORE:
        mOptions = new ArrayList<>(11); // 0 to 10
        for (int i = 1; i <= 10; i++) {
          addOption(String.valueOf(i));
        }
        break;
    }
  }

  public RateQuestion(String title, int low, int high) {
    super(title, Constants.RATE_MODE_CUSTOM);
    mOptions = new ArrayList<>(high - low);
    for (int i = low; i <= high; i++) {
      addOption(String.valueOf(i));
    }
  }
}
