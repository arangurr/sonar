package com.arangurr.newsonar;

import android.content.Context;
import android.content.SharedPreferences;
import com.arangurr.newsonar.data.Poll;
import java.util.ArrayList;
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
    return array;
  }

}