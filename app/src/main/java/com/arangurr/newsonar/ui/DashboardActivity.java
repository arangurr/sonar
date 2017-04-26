package com.arangurr.newsonar.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import com.arangurr.newsonar.BuildConfig;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.BinaryQuestion;
import com.arangurr.newsonar.data.Poll;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

  private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

  private DashboardRecyclerAdapter mAdapter;
  private RecyclerView mPollRecyclerView;

  private MyItemAnimator mAnimator;

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

    mPollRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_dashboard_polls);

    if (BuildConfig.DEBUG) {
      findViewById(R.id.fab_dashboard_add_debug).setVisibility(View.VISIBLE);
    }

    mAnimator = new MyItemAnimator();
    mAdapter = new DashboardRecyclerAdapter(PersistenceUtils.fetchAllPolls(this));
    mPollRecyclerView.setAdapter(mAdapter);
    mPollRecyclerView.setItemAnimator(mAnimator);
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
        Poll poll = new Poll(this, "Sample Poll Title");
        poll.addQuestion(new BinaryQuestion("First Title", Constants.BINARY_MODE_TRUEFALSE));
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

  private static class MyItemAnimator extends SlideInItemAnimator {

    private boolean animateMoves = false;

    MyItemAnimator() {
      super();
    }

    void setAnimateMoves(boolean animateMoves) {
      this.animateMoves = animateMoves;
    }

    @Override
    public boolean animateMove(
        RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
      if (!animateMoves) {
        dispatchMoveFinished(holder);
        return false;
      }
      return super.animateMove(holder, fromX, fromY, toX, toY);
    }
  }

  class DashboardRecyclerAdapter extends RecyclerView.Adapter<DashboardRecyclerAdapter.ViewHolder> {

    private static final int EXPAND = 0x1;
    private static final int COLLAPSE = 0x2;

    private final android.transition.Transition mExpandCollapse;

    private List<Poll> mPolls;
    private int mExpandedPosition = RecyclerView.NO_POSITION;

    public DashboardRecyclerAdapter(List<Poll> polls) {
      mPolls = polls;

      mExpandCollapse = new AutoTransition();
      mExpandCollapse.setDuration(120);
      mExpandCollapse.setInterpolator(AnimationUtils
          .loadInterpolator(DashboardActivity.this, android.R.interpolator.fast_out_slow_in));
      mExpandCollapse.addListener(new TransitionListener() {
        @Override
        public void onTransitionStart(Transition transition) {
          mPollRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              return true;
            }
          });
        }

        @Override
        public void onTransitionEnd(Transition transition) {
          mAnimator.setAnimateMoves(true);
          mPollRecyclerView.setOnTouchListener(null);

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
      });
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

    private void setExpanded(ViewHolder holder, boolean isExpanded) {
      holder.itemView.setActivated(isExpanded);
      holder.mDeleteButton.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public DashboardRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
      View inflatedView = LayoutInflater.from(viewGroup.getContext())
          .inflate(R.layout.item_dashboard, viewGroup, false);

      final ViewHolder holder = new ViewHolder(inflatedView);

      holder.itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          final int position = holder.getAdapterPosition();
          if (position == RecyclerView.NO_POSITION) {
            return;
          }

          TransitionManager.beginDelayedTransition(mPollRecyclerView, mExpandCollapse);
          mAnimator.setAnimateMoves(false);

          // There is an expanded item
          if (mExpandedPosition != RecyclerView.NO_POSITION) {
            setExpanded(holder, false); // Collapse the previously expanded item
            notifyItemChanged(mExpandedPosition, COLLAPSE);
          }
          // Expand this item
          if (mExpandedPosition != position) {
            mExpandedPosition = position;
            setExpanded(holder, true);
            notifyItemChanged(position, EXPAND);
          } else {
            // Item was collapsed, only need to set the expanded position to NO_POSITION
            mExpandedPosition = RecyclerView.NO_POSITION;
          }
        }
      });
      return holder;
    }

    @Override
    public void onBindViewHolder(DashboardRecyclerAdapter.ViewHolder viewHolder, int i) {
      final int position = viewHolder.getAdapterPosition();
      final boolean isExpanded = position == mExpandedPosition;

      viewHolder.mTitle.setText(mPolls.get(i).getPollTitle());
      Date date = new Date(mPolls.get(i).getStartDate());
      SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
      viewHolder.mSubtitle.setText(sdf.format(date));
      viewHolder.mCircle.setText(String.valueOf(i + 1));

      setExpanded(viewHolder, isExpanded);
    }

    @Override
    public void onBindViewHolder(DashboardRecyclerAdapter.ViewHolder holder, int position,
        List<Object> payloads) {
      if (payloads.contains(EXPAND) || payloads.contains(COLLAPSE)) {
        setExpanded(holder, position == mExpandedPosition);
      } else {
        onBindViewHolder(holder, position);
      }
    }

    @Override
    public int getItemCount() {
      return mPolls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

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

        //itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
        Intent i = new Intent(v.getContext(), CommsActivity.class);
        i.putExtra(Constants.EXTRA_POLL_ID, mPolls.get(getAdapterPosition()).getUuid());
        ((Activity) v.getContext()).startActivity(i);
      }
    }
  }
}