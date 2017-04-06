package com.arangurr.newsonar.data;

import java.util.ArrayList;

/**
 * Created by Rodrigo on 01/04/2017.
 */

public class Option {

    private String mOptionName;
    private ArrayList<VoterIdPair> mVoterList;

    public Option(String title) {
        mOptionName = title;
        mVoterList = new ArrayList<>();
    }

    public int getNumberOfVotes(){
        return mVoterList.size();
    }
}
