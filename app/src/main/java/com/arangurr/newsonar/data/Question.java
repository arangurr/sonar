package com.arangurr.newsonar.data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Rodrigo on 30/03/2017.
 */

public class Question {

    private UUID mUuid;
    private String mTitle;
    private ArrayList<Option> mOptions;
    private int mQuestionMode;

    public Question() {
        mUuid = UUID.randomUUID();
        mOptions = new ArrayList<>();
    }

    public Question(String title) {
        mUuid = UUID.randomUUID();
        mOptions = new ArrayList<>();
        mTitle = title;
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

    public void addOption(Option option){
        mOptions.add(option);
    }

    public int getQuestionMode() {
        return mQuestionMode;
    }

    public void setQuestionMode(int questionMode) {
        mQuestionMode = questionMode;
    }
}
