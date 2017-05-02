package com.arangurr.newsonar.data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Rodrigo on 21/04/2017.
 */

public class Vote {

  public static final String TYPE = "vote";

  private UUID mPollId;
  private VoterIdPair mVoterIdPair;
  private ArrayList<QuestionSelection> mSelectionList;

  public Vote(UUID pollId, String voterId, String voterName) {
    mVoterIdPair = new VoterIdPair(voterId, voterName);
    mPollId = pollId;

    mSelectionList = new ArrayList<>();
  }

  public ArrayList<QuestionSelection> getSelectionList() {
    return mSelectionList;
  }

  public void attachResponse(Question question, Option... options) {
    ArrayList<Integer> ids = new ArrayList<>(options.length);
    for (Option o : options) {
      ids.add(o.getKey());
    }

    for (QuestionSelection qs : mSelectionList) {
      if (qs.getQuestionId() == (question.getKey())) {
        qs.swapSelection(ids);
        return;
      }
    }
    mSelectionList.add(new QuestionSelection(question.getKey(), ids));
  }

  public UUID getPollId() {
    return mPollId;
  }

  public VoterIdPair getVoterIdPair() {
    return mVoterIdPair;
  }
}
