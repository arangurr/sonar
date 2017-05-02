package com.arangurr.newsonar;


import android.util.Log;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Rodrigo on 07/04/2017.
 */

public class GsonUtils {

  private static final Gson fullGson = new Gson();
  private static final Gson excludeGson = new GsonBuilder()
      .setExclusionStrategies(new MyExclusionStrategy()).create();

  public static <T> T deserializeGson(String s, Class<T> type) {
    try {
      return fullGson.fromJson(s, type);
    } catch (JsonSyntaxException e) {
      Log.e(GsonUtils.class.getSimpleName(), "JsonSyntaxException parsing string " + s, e);
      return null;
    }
  }

  public static String serialize(Object object) {
    return fullGson.toJson(object);
  }

  public static String serializeExcluding(Object object) {
    return excludeGson.toJson(object);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Exclude {

  }

  private static class MyExclusionStrategy implements com.google.gson.ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
      return f.getAnnotation(Exclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
      return false;
    }
  }
}
