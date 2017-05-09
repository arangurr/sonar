package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Rodrigo on 30/03/2017.
 */

public class Question {

  @SerializedName("qm")
  private final int mQuestionMode;
  @SerializedName("ops")
  private ArrayList<Option> mOptions;
  @SerializedName("id")
  private int mKey;
  @SerializedName("title")
  private String mTitle;

  public Question(String title, int mode) {
    mTitle = title;
    mQuestionMode = mode;
    mOptions = new ArrayList<>();
    switch (mode) {
      case Constants.BINARY_MODE_YESNO:
        addOption("Yes");
        addOption("No");
        break;
      case Constants.BINARY_MODE_TRUEFALSE:
        addOption("True");
        addOption("False");
        break;
      case Constants.RATE_MODE_STARS:
        for (int i = 1; i <= 5; i++) {
          addOption(String.valueOf(i));
        }
        break;
      case Constants.RATE_MODE_LIKEDISLIKE:
        addOption(String.valueOf(0));
        addOption(String.valueOf(1));
        break;
      case Constants.RATE_MODE_SCORE:
        for (int i = 1; i <= 10; i++) {
          addOption(String.valueOf(i));
        }
        break;
      case Constants.BINARY_MODE_CUSTOM:
      case Constants.RATE_MODE_CUSTOM:
        break;
      default:
        break;
    }
  }

  public void setRateCustomLowHigh(int low, int high) {
    for (int i = low; i <= high; i++) {
      addOption(String.valueOf(i));
    }
  }

  public ArrayList<Option> getAllOptions() {
    return mOptions;
  }

  public int getKey() {
    return mKey;
  }

  public void setKey(int key) {
    mKey = key;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String title) {
    mTitle = title;
  }

  public void addOption(String optionTitle) {
    Option o = new Option(mOptions.size(), optionTitle);
    mOptions.add(o);
  }

  public int getQuestionMode() {
    return mQuestionMode;
  }

  public Option getOption(int position) {
    return mOptions.get(position);
  }

  public boolean isVotedBy(VoterIdPair voter) {
    for (Option option : mOptions) {
      if (option.isVotedBy(voter)) {
        return true;
      }
    }
    return false;
  }

  public Option getOptionVotedBy(VoterIdPair voter) {
    for (Option option : mOptions) {
      if (option.isVotedBy(voter)) {
        return option;
      }
    }
    return null;
  }

  public void addSelection(QuestionSelection questionSelection, VoterIdPair voterIdPair) {
    for (Option option : mOptions) {
      for (Integer optionIdInQS : questionSelection.getSelections()) {
        if (optionIdInQS.equals(option.getKey())) {
          option.addVoter(voterIdPair);
        }
      }
    }
  }
}
