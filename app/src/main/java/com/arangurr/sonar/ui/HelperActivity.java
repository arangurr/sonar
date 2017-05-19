package com.arangurr.sonar.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.arangurr.sonar.Constants;

/**
 * Created by Rodrigo on 18/03/2017.
 */

public class HelperActivity extends Activity {

  private boolean isFirstLaunch() {
    return (PreferenceManager.getDefaultSharedPreferences(this)
        .getString(Constants.KEY_UUID, null) == null);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (isFirstLaunch()) {
      startActivity(new Intent(HelperActivity.this, IntroActivity.class));
      finish();
    } else {
      startActivity(new Intent(HelperActivity.this, DashboardActivity.class));
      finish();
    }
  }
}
