package com.arangurr.sonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.arangurr.sonar.BuildConfig;
import com.arangurr.sonar.Constants;
import com.arangurr.sonar.PersistenceUtils;
import com.arangurr.sonar.R;
import com.arangurr.sonar.data.Poll;
import com.arangurr.sonar.data.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
        Poll p = PersistenceUtils.fetchPollWithId(getApplicationContext(), key);
        if (p != null) {
          // Has been added or changed
          if (mAdapter.containsWithId(p.getUuid())) {
            // Has changed
            // TODO: 27/04/2017 update polls
            //mAdapter.updatePoll(p);
          }
          // It's a new one.
          mAdapter.add(p);
        } else {
          // Has been removed
          mAdapter.removePoll(UUID.fromString(key));
        }
      }
    };

    RecyclerView pollRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_dashboard_polls);

    if (BuildConfig.DEBUG) {
      findViewById(R.id.fab_dashboard_add_debug).setVisibility(View.VISIBLE);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.N_MR1) {
      setupShortcuts();
    }

    mAdapter = new DashboardRecyclerAdapter(PersistenceUtils.fetchAllPolls(this));
    pollRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    mAdapter.swapArray(PersistenceUtils.fetchAllPolls(this));
  }

  @Override
  protected void onPause() {
    super.onPause();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
  }

  @RequiresApi(api = VERSION_CODES.N_MR1)
  private void setupShortcuts() {
    ShortcutManager manager = getSystemService(ShortcutManager.class);

    Intent i = new Intent(this, ListenActivity.class);
    i.setAction(Intent.ACTION_VIEW);

    List<ShortcutInfo> shortcuts = manager.getDynamicShortcuts();

    ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "short_listen")
        .setShortLabel(getString(R.string.discover))
        .setLongLabel(getString(R.string.subscribe))
        .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_listen))
        .setIntent(i)
        .build();

    for (ShortcutInfo shortcutInList : shortcuts) {
      if (shortcutInList.getId().equals("short_listen")) {
        manager.updateShortcuts(Collections.singletonList(shortcut));
        return;
      }
    }

    manager.setDynamicShortcuts(Collections.singletonList(shortcut));
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.fab_dashboard_add:
        Intent i = new Intent(getApplicationContext(), EditorActivity.class);
        startActivity(i);
        break;
      case R.id.fab_dashboard_add_debug:
        Poll poll = new Poll(this, "Generic Poll Title");
        poll.addQuestion(new Question("First Question", Constants.BINARY_MODE_TRUEFALSE));
        poll.addQuestion(new Question("Second Question", Constants.BINARY_MODE_YESNO));
        Question q = new Question("Third Question", Constants.BINARY_MODE_CUSTOM);
        q.addOption("First Option");
        q.addOption("Second Option");
        poll.addQuestion(q);
        poll.addQuestion(new Question("4th Question", Constants.RATE_MODE_STARS));
        poll.addQuestion(new Question("5th Question", Constants.RATE_MODE_SCORE));
        poll.addQuestion(new Question("6th Question", Constants.RATE_MODE_LIKEDISLIKE));
        q = new Question("7th Question", Constants.RATE_MODE_CUSTOM);
        q.setRateCustomLowHigh(2, 8);
        poll.addQuestion(q);
        q = new Question("8th Question", Constants.MULTI_MODE_EXCLUSIVE);
        q.addOption("First Option");
        q.addOption("Second Option");
        q.addOption("Third Option");
        q.addOption("Fourth Option");
        poll.addQuestion(q);
        q = new Question("9th Question", Constants.MULTI_MODE_MULTIPLE);
        q.addOption("First Option");
        q.addOption("Second Option");
        q.addOption("Third Option");
        q.addOption("Fourth Option");
        poll.addQuestion(q);

        PersistenceUtils.storePollInPreferences(this, poll);
        break;
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
        Intent editor = (new Intent(this, EditorActivity.class));
        startActivity(editor);
        return true;
      case R.id.action_listen:
        Intent listen = (new Intent(this, ListenActivity.class));
        startActivity(listen);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private class DashboardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_NORMAL = 1;

    private List<Poll> mPolls;

    DashboardRecyclerAdapter(List<Poll> polls) {
      mPolls = polls;
    }

    void swapArray(ArrayList<Poll> newArray) {
      mPolls.clear();
      mPolls.addAll(newArray);
      notifyDataSetChanged();
    }

    public void add(Poll poll) {
      mPolls.add(poll);
      notifyItemInserted(mPolls.size());
    }

    private boolean containsWithId(UUID uuid) {
      for (Poll p : mPolls) {
        if (p.getUuid().equals(uuid)) {
          return true;
        }
      }
      return false;
    }

    private void removePoll(UUID key) {
      for (Poll p : mPolls) {
        if (p.getUuid().equals(key)) {
          int index = mPolls.indexOf(p);
          mPolls.remove(index);
          mAdapter.notifyItemRemoved(index);
          //mAdapter.notifyItemRangeChanged(index, mPolls.size() - index);
          break;
        }
      }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
      View inflatedView;
      if (i == TYPE_EMPTY) {
        inflatedView = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.recyclerview_empty, viewGroup, false);
        return new EmptyHolder(inflatedView);
      } else {
        inflatedView = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.item_dashboard, viewGroup, false);
        return new PollHolder(inflatedView);
      }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
      if (getItemViewType(i) == TYPE_EMPTY) {
        EmptyHolder emptyHolder = (EmptyHolder) holder;
        emptyHolder.text.setText(R.string.dashboard_welcome);
      } else {
        final Poll poll = mPolls.get(i);
        final PollHolder pollHolder = (PollHolder) holder;

        pollHolder.mTitle.setText(poll.getPollTitle());
        pollHolder.mSubtitle.setText(String
            .format(getString(R.string.dashboard_question_counter), poll.getQuestionList().size()));
        // TODO: 19/05/2017 plural
        Date date = new Date(poll.getStartDate());
        pollHolder.mDate.setText(DateUtils.getRelativeTimeSpanString(
            date.getTime(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_ALL));
        pollHolder.mCircle.setText(String.valueOf(poll.getNumberOfVotes()));

        int[] rainbow = getResources().getIntArray(R.array.rainbow);
        int index = Math.abs(poll.getUuid().hashCode()) % rainbow.length;
        pollHolder.mCircle.getBackground().mutate().setTint(rainbow[index]);

        pollHolder.itemView.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            Intent detailsIntent = new Intent(DashboardActivity.this, DetailsActivity.class);
            detailsIntent.putExtra(Constants.EXTRA_POLL_ID,
                poll.getUuid());
            startActivity(detailsIntent);
          }
        });

        pollHolder.mPopupButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(final View v) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            MenuInflater menuInflater = popup.getMenuInflater();
            menuInflater.inflate(R.menu.popup_dashboard, popup.getMenu());

            if (poll.getNumberOfVotes() != 0) {
              MenuItem editItem = popup.getMenu().findItem(R.id.action_popup_edit);
              SpannableString s = new SpannableString(editItem.getTitle().toString());
              s.setSpan(new ForegroundColorSpan(
                      ContextCompat.getColor(v.getContext(), R.color.colorDisabledText)),
                  0, s.length(), 0);
              editItem.setTitle(s);
            }

            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                  case R.id.action_popup_delete:
                    UUID pollToRemove = poll.getUuid();
                    PersistenceUtils.deletePoll(getApplicationContext(), pollToRemove);
                    return true;
                  case R.id.action_popup_activate:
                    Intent activate = new Intent(DashboardActivity.this, DetailsActivity.class);
                    activate.putExtra(Constants.EXTRA_POLL_ID, poll.getUuid());
                    activate.putExtra(Constants.EXTRA_ACTIVATE, 0b1);
                    startActivity(activate);
                    return true;
                  case R.id.action_popup_edit:
                    if (poll.getNumberOfVotes() > 0) {
                      Snackbar.make(v, R.string.edit_only_without_answers,
                          Snackbar.LENGTH_LONG).show();
                    } else {
                      Intent editIntent = new Intent(DashboardActivity.this, EditorActivity.class);
                      editIntent
                          .putExtra(Constants.EXTRA_POLL_ID, poll.getUuid());
                      startActivity(editIntent);
                    }
                    return true;
                  default:
                    return false;
                }
              }
            });
            popup.show();
          }
        });
      }

    }

    @Override
    public int getItemCount() {
      return mPolls.size() == 0 ? 1 : mPolls.size();
    }

    @Override
    public int getItemViewType(int position) {
      return mPolls.size() == 0 ? TYPE_EMPTY : TYPE_NORMAL;
    }

    class PollHolder extends RecyclerView.ViewHolder {

      private TextView mTitle;
      private TextView mSubtitle;
      private TextView mDate;
      private TextView mCircle;
      private ImageButton mPopupButton;

      PollHolder(View itemView) {
        super(itemView);

        mTitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_title);
        mDate = (TextView) itemView.findViewById(R.id.textview_dashboard_item_date);
        mSubtitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_subtitle);
        mCircle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_circle);
        mPopupButton = (ImageButton) itemView.findViewById(R.id.button_dashboard_item_popup);
      }
    }
  }
}