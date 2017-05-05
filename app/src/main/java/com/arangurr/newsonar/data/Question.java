package com.arangurr.newsonar.data;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Rodrigo on 30/03/2017.
 */

public abstract class Question {

  @SerializedName("id")
  private int mKey;
  @SerializedName("title")
  private String mTitle;
  @SerializedName("ops")
  private ArrayList<Option> mOptions;
  @SerializedName("qm")
  private int mQuestionMode;

  public Question(String title) {
    mTitle = title;
    mOptions = new ArrayList<>();
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

  public void setQuestionMode(int questionMode) {
    mQuestionMode = questionMode;
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
