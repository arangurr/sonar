package com.arangurr.newsonar.data;

import com.arangurr.newsonar.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public class Poll extends MessagePayload {

    private boolean mPasswordProtected = false;
    private String mPassword;
    private UUID mPollId;
    private UUID mOwnerId;
    private ArrayList<Question> mQuestionList;
    private int mPrivacySetting;

    public Poll() {
        mPollId = UUID.randomUUID();
        mQuestionList = new ArrayList<>();
        mPrivacySetting = Constants.PRIVACY_PRIVATE;
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

    public UUID getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(UUID ownerId) {
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
}
