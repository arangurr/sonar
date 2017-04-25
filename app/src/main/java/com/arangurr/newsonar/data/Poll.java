package com.arangurr.newsonar.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arangurr.newsonar.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public class Poll {

  public static final String TYPE = "poll";

  private final long mStartDate;
  private final UUID mPollId;
  private final String mOwnerId; // Not UUID, coming from Secure.ID
  private String mOwnerName;
  private String mPollTitle;
  private boolean mPasswordProtected = false;
  private String mPassword;
  private ArrayList<Question> mQuestionList;
  private int mPrivacySetting = Constants.PRIVACY_PRIVATE;

  public Poll(Context context) {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mPrivacySetting = Constants.PRIVACY_PRIVATE;
    mStartDate = new Date().getTime();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    mOwnerId = prefs.getString(Constants.KEY_UUID, null);
    mOwnerName = prefs.getString(Constants.KEY_USERNAME, null);
  }

  public Poll(Context context, String title) {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mPrivacySetting = Constants.PRIVACY_PRIVATE;
    mPollTitle = title;
    mStartDate = new Date().getTime();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    mOwnerId = prefs.getString(Constants.KEY_UUID, null);
    mOwnerName = prefs.getString(Constants.KEY_USERNAME, null);
  }

  public boolean isPasswordProtected() {
    return mPasswordProtected;
  }

  public void setPasswordProtected(boolean passwordProtected) {
    mPasswordProtected = passwordProtected;
  }

  public String getPassword() {
    return mPassword;
  }

  public void setPassword(String password) {
    mPassword = password;
  }

  public UUID getUuid() {
    return mPollId;
  }

  public List<Question> getQuestionList() {
    return mQuestionList;
  }

  public void setQuestionList(ArrayList<Question> questionList) {
    mQuestionList = questionList;
  }

  public int getPrivacySetting() {
    return mPrivacySetting;
  }

  public void setPrivacySetting(int privacySetting) {
    mPrivacySetting = privacySetting;
  }

  public void addQuestion(Question newQuestion) {
    mQuestionList.add(newQuestion);
  }

  public String getPollTitle() {
    return mPollTitle;
  }

  public void setPollTitle(String pollTitle) {
    mPollTitle = pollTitle;
  }

  public String getOwnerName() {
    return mOwnerName;
  }

  public void setOwnerName(String ownerName) {
    mOwnerName = ownerName;
  }

  public long getStartDate() {
    return mStartDate;
  }

  public String getOwnerId() {
    return mOwnerId;
  }

  public void updateWithVote(Vote vote) {
    if (!hasVoted(vote.getVoterIdPair())){
      for(QuestionSelection questionSelection : vote.getSelectionList()){
        for (Question q : mQuestionList){
          if (q.getUuid().equals(questionSelection.getQuestionId())){
            q.addSelection(questionSelection, vote.getVoterIdPair());
          }
        }
      }
    }
  }

  private boolean hasVoted(VoterIdPair voter) {
    for (Question question : mQuestionList) {
      if (question.isVotedBy(voter)) {
        return true;
      }
    }
    return false;
  }

}
