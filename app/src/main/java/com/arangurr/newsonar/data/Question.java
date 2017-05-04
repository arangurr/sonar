package com.arangurr.newsonar.data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Rodrigo on 30/03/2017.
 */

public class Question {

  private final int mQuestionMode;
  protected ArrayList<Option> mOptions;
  private UUID mUuid;
  private String mTitle;

  public Question(String title, int mode) {
    mUuid = UUID.randomUUID();
    mTitle = title;
    mQuestionMode = mode;
  }

  public ArrayList<Option> getAllOptions() {
    return mOptions;
  }

  public UUID getUuid() {
    return mUuid;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String title) {
    mTitle = title;
  }

  public void addOption(Option option) {
    mOptions.add(option);
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

  public void addSelection(QuestionSelection questionSelection, VoterIdPair voterIdPair) {
    for (Option option : mOptions) {
      for (UUID optionIdInQS : questionSelection.getSelections()) {
        if (optionIdInQS.equals(option.getOptionUUID())) {
          option.addVoter(voterIdPair);
        }
      }
    }
  }
}
