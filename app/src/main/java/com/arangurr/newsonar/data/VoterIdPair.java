package com.arangurr.newsonar.data;

import java.util.UUID;

/**
 * Created by Rodrigo on 01/04/2017.
 */

public class VoterIdPair {

  private final UUID mUuid;
  private String mUserName;

  public VoterIdPair(UUID id, String user) {
    this.mUuid = id;
    this.mUserName = user;
  }

  public UUID getUuid() {
    return mUuid;
  }

  public String getUserName() {
    return mUserName;
  }

  public void setUserName(String newName) {
    mUserName = newName;
  }
}
