package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.arangurr.newsonar.BuildConfig;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

  private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

  private DashboardRecyclerAdapter mAdapter;
  private RecyclerView mPollRecyclerView;


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

    mPollRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_dashboard_polls);

    if (BuildConfig.DEBUG) {
      findViewById(R.id.fab_dashboard_add_debug).setVisibility(View.VISIBLE);
    }

    mAdapter = new DashboardRecyclerAdapter(PersistenceUtils.fetchAllPolls(this));
    mPollRecyclerView.setAdapter(mAdapter);
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

  class DashboardRecyclerAdapter extends RecyclerView.Adapter<DashboardRecyclerAdapter.ViewHolder> {

    private static final int EXPANDCOLLAPSE = 0x1;

    private List<Poll> mPolls;
    private int mExpandedPosition = RecyclerView.NO_POSITION;

    public DashboardRecyclerAdapter(List<Poll> polls) {
      mPolls = polls;
    }

    public void swapArray(ArrayList<Poll> newArray) {
      mPolls.clear();
      mPolls.addAll(newArray);
      notifyDataSetChanged();
    }

    public void add(Poll poll) {
      mPolls.add(poll);
      notifyItemInserted(mPolls.size() - 1);
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
    public DashboardRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
      View inflatedView = LayoutInflater.from(viewGroup.getContext())
          .inflate(R.layout.item_dashboard, viewGroup, false);

      final ViewHolder holder = new ViewHolder(inflatedView);

      return holder;
    }

    @Override
    public void onBindViewHolder(final DashboardRecyclerAdapter.ViewHolder viewHolder, int i) {

      viewHolder.mTitle.setText(mPolls.get(i).getPollTitle());
      Date date = new Date(mPolls.get(i).getStartDate());
      SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
      viewHolder.mSubtitle.setText(sdf.format(date));
      viewHolder.mCircle.setText(String.valueOf(i + 1));

      if (i == mExpandedPosition) {
        viewHolder.itemView.setActivated(true);
        viewHolder.mDeleteButton.setVisibility(View.VISIBLE);
      } else {
        viewHolder.itemView.setActivated(false);
        viewHolder.mDeleteButton.setVisibility(View.GONE);
      }

      viewHolder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          Intent detailsIntent = new Intent(DashboardActivity.this, DetailsActivity.class);
          detailsIntent.putExtra(Constants.EXTRA_POLL_ID,
              mPolls.get(viewHolder.getAdapterPosition()).getUuid());
          startActivity(detailsIntent);
        }
      });

      viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {

          //TransitionManager.beginDelayedTransition(mPollRecyclerView);

          // There is an expanded item
          if (mExpandedPosition != RecyclerView.NO_POSITION) {
            int prev = mExpandedPosition;
            notifyItemChanged(prev, EXPANDCOLLAPSE);
          }
          if (mExpandedPosition != viewHolder.getAdapterPosition()) {
            mExpandedPosition = viewHolder.getAdapterPosition();
            notifyItemChanged(mExpandedPosition, EXPANDCOLLAPSE);
          } else {
            mExpandedPosition = RecyclerView.NO_POSITION;
          }
          return true;
        }
      });

      viewHolder.mDeleteButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          int position = viewHolder.getAdapterPosition();
          UUID pollToRemove = mPolls.get(position).getUuid();
          PersistenceUtils.deletePoll(getApplicationContext(), pollToRemove);
          if (position == mExpandedPosition) {
            mExpandedPosition = RecyclerView.NO_POSITION;
          }
        }
      });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
      if (payloads.contains(EXPANDCOLLAPSE)) {
        if (position == mExpandedPosition) {
          holder.itemView.setActivated(true);
          holder.mDeleteButton.setVisibility(View.VISIBLE);
        } else {
          holder.itemView.setActivated(false);
          holder.mDeleteButton.setVisibility(View.GONE);
        }
      } else {
        onBindViewHolder(holder, position);
      }
    }

    @Override
    public int getItemCount() {
      return mPolls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      private TextView mTitle;
      private TextView mSubtitle;
      private TextView mCircle;
      private ImageButton mDeleteButton;

      public ViewHolder(View itemView) {
        super(itemView);

        mTitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_title);
        mSubtitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_subtitle);
        mCircle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_circle);
        mDeleteButton = (ImageButton) itemView.findViewById(R.id.button_dashboard_item_delete);
      }
    }
  }
}