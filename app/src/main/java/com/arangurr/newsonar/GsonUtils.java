package com.arangurr.newsonar;


import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by Rodrigo on 07/04/2017.
 */

public class GsonUtils {

  static final Gson gson = new Gson();

  public static <T> T deserializeGson(String s, Class<T> type) {
    try {
      return gson.fromJson(s, type);
    } catch (JsonSyntaxException e) {
      Log.e(GsonUtils.class.getSimpleName(), "JsonSyntaxException parsing string " + s, e);
      return null;
    }
  }

  public static String serialize(Object object) {
    return gson.toJson(object);
  }
}
