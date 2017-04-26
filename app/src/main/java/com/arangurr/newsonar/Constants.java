package com.arangurr.newsonar;

/**
 * Created by Rodrigo on 18/03/2017.
 */

public class Constants {

  public static final String KEY_UUID = "user-uuid";
  public static final String KEY_USERNAME = "username";
  public static final int TTL_30SEC = 30;
  public static final int TTL_10MIN = 600;

  public static final String NAMESPACE = "sonar";

  public static final int PRIVACY_PUBLIC = 0;
  public static final int PRIVACY_PRIVATE = 1;
  public static final int PRIVACY_SECRET = 2;
  
  public static final int BINARY_MODE_YESNO = 10;
  public static final int BINARY_MODE_TRUEFALSE = 11;
  public static final int BINARY_MODE_UPDOWNVOTE = 12;

  public static final int BINARY_MODE_CUSTOM = 13;
  public static final String PREFS_POLLS = "preferences_polls";

  public static final String PREFS_VOTE = "preferences_vote";
  public static final String EXTRA_POLL_ID = "poll_id";
  public static final String EXTRA_SERIALIZED_POLL = "serialized_poll";

  public static final String EXTRA_SERIALIZED_VOTE = "serialized_vote";
  public static final int VOTE_REQUEST = 100;
}
