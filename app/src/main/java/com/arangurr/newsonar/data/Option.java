package com.arangurr.newsonar.data;

import com.arangurr.newsonar.GsonUtils.Exclude;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by Rodrigo on 01/04/2017.
 */

public class Option {

  @SerializedName("id")
  private final int mKey;
  @SerializedName("title")
  private String mOptionName;
  @Exclude
  private ArrayList<VoterIdPair> mVoterList;

  public Option(int key, String title) {
    mKey = key;
    mOptionName = title;
    mVoterList = new ArrayList<>();
  }

  public ArrayList<VoterIdPair> getVoterList() {
    return mVoterList;
  }

  public int getNumberOfVotes() {
    return mVoterList.size();
  }

  public String getOptionName() {
    return mOptionName;
  }

  public int getKey() {
    return mKey;
  }

  public boolean isVotedBy(VoterIdPair voter) {
    for (VoterIdPair voterInOption : mVoterList) {
      if (voterInOption.getUuid().equals(voter.getUuid())) {
        return true;
      }
    }
    return false;
  }

  public void addVoter(VoterIdPair voter) {
    mVoterList.add(voter);
  }

  public void removeVoter(VoterIdPair voter) {
    if (!mVoterList.remove(voter)) {
      for (VoterIdPair user : mVoterList) {
        if (user.getUuid().equals(voter.getUuid())) {
          mVoterList.remove(user);
          return;
        }
      }
    }
  }
}
