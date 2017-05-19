package com.arangurr.newsonar.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arangurr.newsonar.Constants;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public class Poll {

  public static final String TYPE = "poll";

  @SerializedName("date")
  private final long mStartDate;
  @SerializedName("id")
  private final UUID mPollId;
  @SerializedName("idown")
  private final String mOwnerId; // Not UUID, coming from Secure.ID
  @SerializedName("namown")
  private String mOwnerName;
  @SerializedName("title")
  private String mPollTitle;
  @SerializedName("pwp")
  private boolean mPasswordProtected = false;
  @SerializedName("pw")
  private String mPassword;
  @SerializedName("qs")
  private ArrayList<Question> mQuestionList;
  @SerializedName("ps")
  private int mPrivacySetting;

  public Poll(Context context) {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mStartDate = new Date().getTime();
    mPrivacySetting = Constants.PRIVACY_PUBLIC;
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    mOwnerId = prefs.getString(Constants.KEY_UUID, null);
    mOwnerName = prefs.getString(Constants.KEY_USERNAME, null);
  }

  public Poll(Context context, String title) {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mPollTitle = title;
    mStartDate = new Date().getTime();
    mPrivacySetting = Constants.PRIVACY_PUBLIC;
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
    newQuestion.setKey(mQuestionList.size());
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
    if (hasVoted(vote.getVoterIdPair())) {
      deleteUserResponse(vote.getVoterIdPair());
    }
    addUserResponse(vote);
  }

  private void addUserResponse(Vote vote) {
    for (QuestionSelection questionSelection : vote.getSelectionList()) {
      for (Question q : mQuestionList) {
        if (q.getKey() == (questionSelection.getQuestionId())) {
          q.addSelection(questionSelection, vote.getVoterIdPair());
        }
      }
    }
  }

  private void deleteUserResponse(VoterIdPair user) {
    for (Question question : mQuestionList) {
      Option o = question.getOptionVotedBy(user);
      if (o != null) {
        o.removeVoter(user);
      }
    }
    return;
  }

  private boolean hasVoted(VoterIdPair voter) {
    for (Question question : mQuestionList) {
      if (question.isVotedBy(voter)) {
        return true;
      }
    }
    return false;
  }

  public int getNumberOfVotes() {
    return mQuestionList.size() == 0 ? 0 : mQuestionList.get(0).getNumberOfVotes();
  }

  public ArrayList<VoterIdPair> getVoterList() {
    ArrayList<VoterIdPair> voters = new ArrayList<>();
    for (Question question : mQuestionList) {
      for (Option option : question.getAllOptions()) {
        for (VoterIdPair voter : option.getVoterList()) {
          if (!voters.contains(voter)) {
            voters.add(voter);
          }
        }
      }
    }
    return voters;
  }
}
