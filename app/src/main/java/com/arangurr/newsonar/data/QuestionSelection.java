package com.arangurr.newsonar.data;

import java.util.ArrayList;

/**
 * Created by Rodrigo on 21/04/2017.
 */

public class QuestionSelection {

  private int mQuestionId;
  private ArrayList<Integer> mSelections;

  public QuestionSelection(int questionId, ArrayList<Integer> options) {
    mQuestionId = questionId;
    mSelections = options;
  }

  public QuestionSelection(int questionId, Integer optionUUID) {
    mQuestionId = questionId;
    mSelections = new ArrayList<>();
    mSelections.add(optionUUID);
  }

  public ArrayList<Integer> getSelections() {
    return mSelections;
  }

  public int getQuestionId() {
    return mQuestionId;
  }

  public void swapSelection(ArrayList<Integer> options) {
    mSelections.clear();
    mSelections.addAll(options);
  }
}
