package com.arangurr.newsonar.data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Rodrigo on 21/04/2017.
 */

public class QuestionSelection {

  private UUID mQuestionId;
  private ArrayList<UUID> mSelections;

  public QuestionSelection(UUID questionId, ArrayList<UUID> options) {
    mQuestionId = questionId;
    mSelections = options;
  }

  public QuestionSelection(UUID questionId, UUID optionUUID) {
    mQuestionId = questionId;
    mSelections = new ArrayList<>();
    mSelections.add(optionUUID);
  }

  public ArrayList<UUID> getSelections() {
    return mSelections;
  }

  public UUID getQuestionId() {
    return mQuestionId;
  }

  public void swapSelection(ArrayList<UUID> options) {
    mSelections.clear();
    mSelections.addAll(options);
  }
}
