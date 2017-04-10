package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.BinaryQuestion;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.ui.widget.OnItemClickListener;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

  private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

  private DashboardRecyclerAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mAdapter.add(PersistenceUtils.fetchPollWithId(getApplicationContext(), key));
      }
    };

    RecyclerView pollRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_dashboard_polls);

    mAdapter = new DashboardRecyclerAdapter(PersistenceUtils.fetchAllPolls(this));
    mAdapter.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        Snackbar.make(view, position + " clicked", Snackbar.LENGTH_SHORT).show();

      }
    });
    pollRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
  }

  @Override
  protected void onPause() {
    super.onPause();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.fab_dashboard_add:
        Intent i = new Intent(getApplicationContext(), EditorActivity.class);
        startActivity(i);
        break;
      case R.id.fab_dashboard_add_debug:
        SharedPreferences defaultPrefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        Poll poll = new Poll("Sample Poll Title");
        poll.setOwnerId(defaultPrefs.getString(Constants.KEY_UUID, null));
        poll.setOwnerName(defaultPrefs.getString(Constants.KEY_USERNAME, "username not defined"));
        poll.addQuestion(new BinaryQuestion("First Title", Constants.BINARY_MODE_TRUEFALSE));

        PersistenceUtils.storePollInPreferences(this, poll);
        break;

      /*case R.id.toCommsButton:
        Poll p = mPolls.get(mPolls.size() - 1);

        Intent i = new Intent(getApplicationContext(), CommsActivity.class);
        i.putExtra(Constants.EXTRA_POLL_ID, p.getUuid());

        startActivity(i);*/
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
