package com.arangurr.sonar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.arangurr.sonar.data.Poll;
import com.arangurr.sonar.data.Vote;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Rodrigo on 07/04/2017.
 */

public class PersistenceUtils {

  public static void storePollInPreferences(Context context, Poll poll) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(Constants.PREFS_POLLS, Context.MODE_PRIVATE)
        .edit();

    editor.putString(poll.getUuid().toString(), GsonUtils.serialize(poll));
    editor.apply();

  }

  public static Poll fetchPollWithId(Context context, String id) {
    SharedPreferences prefs = context
        .getSharedPreferences(Constants.PREFS_POLLS, Context.MODE_PRIVATE);

    return GsonUtils.deserializeGson(prefs.getString(id, null), Poll.class);
  }

  public static Poll fetchPollWithId(Context context, UUID id) {
    return fetchPollWithId(context, id.toString());
  }

  public static ArrayList<Poll> fetchAllPolls(Context context) {
    ArrayList<Poll> array = new ArrayList<>();
    Map<String, ?> preferenceEntries = context
        .getSharedPreferences(Constants.PREFS_POLLS, Context.MODE_PRIVATE).getAll();
    if (!preferenceEntries.isEmpty()) {
      for (Map.Entry<String, ?> entry : preferenceEntries.entrySet()) {
        array.add(GsonUtils.deserializeGson((String) entry.getValue(), Poll.class));
      }
    }

    Collections.sort(array, new Comparator<Poll>() {
      @Override
      public int compare(Poll o1, Poll o2) {
        return (int) (o1.getStartDate() - o2.getStartDate());
      }
    });
    return array;
  }

  public static void deletePoll(Context context, String uuid) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(Constants.PREFS_POLLS, Context.MODE_PRIVATE)
        .edit();

    editor.remove(uuid);
    editor.apply();
  }

  public static void deletePoll(Context context, UUID uuid) {
    deletePoll(context, uuid.toString());
  }

  public static void deletePoll(Context context, Poll poll) {
    deletePoll(context, poll.getUuid());
  }

  public static void storeVoteInPreferences(Context context, Vote vote) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(Constants.PREFS_VOTE, Context.MODE_PRIVATE)
        .edit();

    editor.putString("vote", GsonUtils.serialize(vote));
    editor.apply();
  }

  @Nullable
  public static Vote fetchVote(Context context) {
    SharedPreferences prefs = context
        .getSharedPreferences(Constants.PREFS_VOTE, Context.MODE_PRIVATE);
    String gson = prefs.getString("vote", null);
    return gson == null ? null : GsonUtils.deserializeGson(gson, Vote.class);
  }

  public static void deleteVote(Context context) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(Constants.PREFS_VOTE, Context.MODE_PRIVATE)
        .edit();

    editor.remove("vote");
    editor.apply();
  }

  @Nullable
  public static String getUser(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(Constants.KEY_USERNAME, null);
  }
}
