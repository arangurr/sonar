package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.BinaryQuestion;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.data.Poll;
import java.util.ArrayList;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

  ArrayList<Poll> mPolls;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    Button toCommsButton = (Button) findViewById(R.id.toCommsButton);
    Button saveButon = (Button) findViewById(R.id.saveButton);

    toCommsButton.setOnClickListener(this);
    saveButon.setOnClickListener(this);

    mPolls = new ArrayList<>();
    Map<String, ?> entries = getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE).getAll();
    if (!entries.isEmpty()) {
      for (Map.Entry<String, ?> entry : entries.entrySet()) {
        mPolls.add(GsonUtils.deserializeGson((String) entry.getValue(), Poll.class));
      }
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.toCommsButton:
        Poll p = mPolls.get(mPolls.size() - 1);

        Intent i = new Intent(getApplicationContext(), CommsActivity.class);
        i.putExtra(Constants.EXTRA_POLL_ID, p.getUuid());

        startActivity(i);
      case R.id.saveButton:
        SharedPreferences pollPrefs = getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pollPrefs.edit();
        SharedPreferences defaultPrefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        Poll poll = new Poll("Sample Poll Title");
        poll.setOwnerId(defaultPrefs.getString(Constants.KEY_UUID, null));
        poll.setOwnerName(defaultPrefs.getString(Constants.KEY_USERNAME, "username not defined"));
        poll.addQuestion(new BinaryQuestion("First Title", Constants.BINARY_MODE_TRUEFALSE));
        String toSaveString = GsonUtils.serialize(poll);
        prefEditor.putString(poll.getUuid().toString(), toSaveString);
        prefEditor.apply();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_dashboard, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.action_settings:
        // TODO: 20/03/2017 settings activity
        return true;
      case R.id.action_add:
        Intent i = (new Intent(this, EditorActivity.class));
        startActivity(i);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
