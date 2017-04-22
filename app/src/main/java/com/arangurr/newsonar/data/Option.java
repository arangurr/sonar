package com.arangurr.newsonar.data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Rodrigo on 01/04/2017.
 */

public class Option {

  private UUID mOptionUUID;
  private String mOptionName;
  private ArrayList<VoterIdPair> mVoterList;

  public Option(String title) {
    mOptionUUID = UUID.randomUUID();
    mOptionName = title;
    mVoterList = new ArrayList<>();
  }

  public int getNumberOfVotes() {
    return mVoterList.size();
  }

  public String getOptionName() {
    return mOptionName;
  }

  public UUID getOptionUUID() {
    return mOptionUUID;
  }
}
