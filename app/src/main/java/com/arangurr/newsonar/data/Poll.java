package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public class Poll {

  private final long mStartDate;
  private UUID mPollId;
  private String mOwnerId;
  private String mOwnerName;
  private String mPollTitle;
  private boolean mPasswordProtected = false;
  private String mPassword;
  private ArrayList<Question> mQuestionList;
  private int mPrivacySetting = Constants.PRIVACY_PRIVATE;

  public Poll() {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mPrivacySetting = Constants.PRIVACY_PRIVATE;
    mStartDate = new Date().getTime();
  }

  public Poll(String title) {
    mPollId = UUID.randomUUID();
    mQuestionList = new ArrayList<>();
    mPrivacySetting = Constants.PRIVACY_PRIVATE;
    mPollTitle = title;
    mStartDate = new Date().getTime();
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

  public void setUuid(UUID id) {
    mPollId = id;
  }

  public void setOwnerId(String ownerId) {
    mOwnerId = ownerId;
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
}
