package com.arangurr.sonar.data;

/**
 * Created by Rodrigo on 01/04/2017.
 */

public class VoterIdPair {

  private final String mUuid;
  private String mUserName;

  public VoterIdPair(String id, String user) {
    this.mUuid = id;
    this.mUserName = user;
  }

  public String getUuid() {
    return mUuid;
  }

  public String getUserName() {
    return mUserName;
  }

  public void setUserName(String newName) {
    mUserName = newName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof VoterIdPair) {
      return ((VoterIdPair) obj).getUuid().equals(mUuid) && ((VoterIdPair) obj).getUserName().equals(mUserName);
    } else {
      return false;
    }
  }
}
