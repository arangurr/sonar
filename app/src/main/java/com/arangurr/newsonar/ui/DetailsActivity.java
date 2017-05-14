package com.arangurr.newsonar.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Option;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import com.arangurr.newsonar.data.Vote;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class DetailsActivity extends AppCompatActivity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = "DetailsActivity";
  private static final String EXTRA_COUNTER = "counter";
  private static final String EXTRA_CHRONO = "chrono";


  private GoogleApiClient mGoogleApiClient;
  private Message mActiveMessage;
  private MessageListener mMessageListener;

  private TextView mStatusTextView;
  private ProgressBar mStatusProgressBar;
  private ToggleButton mToggleButton;
  //private ViewSwitcher mViewSwitcher;
  private RecyclerView mRecyclerView;
  private TextView mCounterTextView;
  private Chronometer mChronometer;

  private SimpleRecyclerViewAdapter mAdapter;

  private Poll mCurrentPoll;
  private int mVoteCount;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      UUID pollId = (UUID) extras.getSerializable(Constants.EXTRA_POLL_ID);
      mCurrentPoll = PersistenceUtils.fetchPollWithId(this, pollId);
    } else {
      Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
      finish();
    }

    getSupportActionBar().setTitle(mCurrentPoll.getPollTitle());

    mStatusTextView = (TextView) findViewById(R.id.textview_details_status);
    mStatusProgressBar = (ProgressBar) findViewById(R.id.progressbar_details_status);
    mToggleButton = (ToggleButton) findViewById(R.id.toggle_details);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_details);
    LinearLayout bottomSheet = (LinearLayout) findViewById(R.id.bottomsheet_details);
    mChronometer = (Chronometer) findViewById(R.id.chronometer_details);
    mCounterTextView = (TextView) findViewById(R.id.textview_details_counter);

    final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
    behavior.setBottomSheetCallback(new BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING && !mToggleButton.isChecked()) {
          behavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Prevent expanding
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      }
    });
    mAdapter = new SimpleRecyclerViewAdapter();
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setAdapter(mAdapter);

    mToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          mVoteCount = 0;
          mChronometer.setBase(SystemClock.elapsedRealtime());
          mChronometer.start();
          behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
          //setStatusView();
        } else {
          behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          //setDetailsView();
        }

        if (mGoogleApiClient.isConnected()) {
          if (isChecked) {
            publish();
            subscribe();
          } else {
            unpublish();
            unsubscribe();
          }
        }
      }
    });

    mMessageListener = new MessageListener() {
      @Override
      public void onFound(Message message) {
        super.onFound(message);
        final String messageAsString = new String(message.getContent(), StandardCharsets.UTF_8);
        Vote v = GsonUtils.deserializeGson(messageAsString, Vote.class);
        if (v != null) {
          Log.d(TAG, "Trying to update Poll with found Vote");
          if (v.getPollId().equals(mCurrentPoll.getUuid())) {
            switch (mCurrentPoll.getPrivacySetting()) {
              case Constants.PRIVACY_PUBLIC:
              case Constants.PRIVACY_PRIVATE:
                mStatusTextView.setText(
                    String.format("Got vote from %s", v.getVoterIdPair().getUserName()));
                break;
              case Constants.PRIVACY_SECRET:
                // Do nothing
                break;
            }
            mCurrentPoll.updateWithVote(v);
            mVoteCount++;
            mCounterTextView.setText(String.format("%d votes in this session", mVoteCount));
            PersistenceUtils.storePollInPreferences(getBaseContext(), mCurrentPoll);
            mAdapter.notifyDataSetChanged();
          }
        } else {
          Log.d(TAG, "Could not update Poll with found Vote");
        }
      }

      @Override
      public void onLost(Message message) {
        super.onLost(message);
        String messageAsString = new String(message.getContent());
        Log.d(TAG, "Message " + messageAsString + " was lost");
      }
    };

    buildGoogleApiClient();
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putInt(EXTRA_COUNTER, mVoteCount);
    outState.putLong(EXTRA_CHRONO, mChronometer.getBase());
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mVoteCount = savedInstanceState.getInt(EXTRA_COUNTER);
    mChronometer.setBase(savedInstanceState.getLong(EXTRA_CHRONO));
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(TAG, "Connected");
    if (mToggleButton.isChecked()) {
      publish();
      subscribe();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.e(TAG, "Google API connection suspended: " + i);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.i(TAG, "Google Api connection failed");
  }

  private void subscribe() {
    SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_10MIN)
            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
            .build())
        .setCallback(new SubscribeCallback() {
          @Override
          public void onExpired() {
            super.onExpired();
            Log.d(TAG, "Subscription expired");
            mStatusProgressBar.setVisibility(View.INVISIBLE);
            mStatusTextView.setText("Stopping...");
            mToggleButton.postDelayed(new Runnable() {
              @Override
              public void run() {
                mToggleButton.setChecked(false);
              }
            }, Constants.DELAY_2SEC);
          }
        })
        .setFilter(new MessageFilter.Builder()
            .includeNamespacedType(Constants.NAMESPACE, Vote.TYPE)
            .build())
        .build();

    Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, subscribeOptions)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Subscribed successfully");
              mStatusTextView.append("\nNow listening for votes");
            } else {
              Log.d(TAG, "Couldn't subscribe due to status = " + status);
            }
          }
        });

    Log.d(TAG, "Trying to subscribe");
    mStatusTextView.append("\nStarting to listen for votes");
  }

  private void publish() {
    String message = GsonUtils.serializeExcluding(mCurrentPoll);
    mActiveMessage = new Message(message.getBytes(StandardCharsets.UTF_8),  // Not documented
        Constants.NAMESPACE,                                                // Namespace
        Poll.TYPE);                                                         // Type of message

    PublishOptions publishOptions = new PublishOptions.Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_10MIN + 10)  // Wait just a little bit longer
            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
            .build())
        .setCallback(new PublishCallback() {
          @Override
          public void onExpired() {
            Log.d(TAG, "Publish expired");
            mStatusTextView
                .setText("Poll no longer available.\nWaiting for votes a little longer...");
            super.onExpired();
          }
        })
        .build();

    Nearby.Messages.publish(mGoogleApiClient, mActiveMessage, publishOptions)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Published successfully");
              mStatusTextView.setText("Poll is available");
            } else {
              Log.d(TAG, "Couldn't publish due to status = " + status);
            }
          }
        });
    Log.d(TAG, "Trying to publish");
    mStatusProgressBar.setVisibility(View.VISIBLE);
    mStatusTextView.setText("Trying to make poll available");
  }

  private void unsubscribe() {
    Log.d(TAG, "Unsubscribing");
    Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener).
        setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Unsubscribed successfully");
              mStatusTextView.setText("");
              mStatusProgressBar.setVisibility(View.INVISIBLE);
            } else {
              Log.d(TAG, "Could not unsubscribe due to status " + status);
            }
          }
        });
  }

  private void unpublish() {
    Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage)
        .setResultCallback(
            new ResultCallback<Status>() {
              @Override
              public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                  Log.d(TAG, "Poll unpublished successfully");
                  mStatusTextView.setText("");
                } else {
                  Log.d(TAG, "Couldn't unpublish due to status = " + status);
                }
              }
            });
    mActiveMessage = null;
    Log.d(TAG, "Unpublishing poll");
  }

  private void buildGoogleApiClient() {
    if (mGoogleApiClient != null) {
      return;
    }
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Nearby.MESSAGES_API)
        .addConnectionCallbacks(this)
        .enableAutoManage(this, this)
        .build();
  }

  class SimpleRecyclerViewAdapter extends
      RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Override
    public int getItemViewType(int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      switch (q.getQuestionMode()) {
        case Constants.BINARY_MODE_CUSTOM:
        case Constants.BINARY_MODE_TRUEFALSE:
        case Constants.BINARY_MODE_YESNO:
          return R.layout.details_card_twoitems;
        case Constants.RATE_MODE_LIKEDISLIKE:
          return R.layout.details_card_likedislike;
        case Constants.RATE_MODE_CUSTOM:
        case Constants.RATE_MODE_SCORE:
        case Constants.RATE_MODE_STARS:
        case Constants.MULTI_MODE_EXCLUSIVE:
        case Constants.MULTI_MODE_MULTIPLE:
          return R.layout.details_card_multiple;
      }
      return android.R.layout.simple_list_item_1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View inflatedView;
      switch (viewType) {
        case R.layout.details_card_twoitems:
          inflatedView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.details_card_twoitems, parent, false);
          return new DualItemHolder(inflatedView);
        case R.layout.details_card_likedislike:
          inflatedView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.details_card_likedislike, parent, false);
          return new LikeDislikeHolder(inflatedView);
        case R.layout.details_card_multiple:
          inflatedView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.details_card_multiple, parent, false);
          return new MultipleItemHolder(inflatedView);
      }
      inflatedView = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      return new SimpleHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      switch (getItemViewType(position)) {
        case R.layout.details_card_twoitems:
          bindDualItem((DualItemHolder) holder, position);
          break;
        case R.layout.details_card_multiple:
          int mode = mCurrentPoll.getQuestionList().get(position).getQuestionMode();
          if (mode == Constants.MULTI_MODE_EXCLUSIVE || mode == Constants.MULTI_MODE_MULTIPLE) {
            bindMultipleOption((MultipleItemHolder) holder, position);
            break;
          }
          bindRate((MultipleItemHolder) holder, position);
          break;
        case R.layout.details_card_likedislike:
          bindLikeDislike((LikeDislikeHolder) holder, position);
          break;
        default:
          ((SimpleHolder) holder).mText1.setText(String.valueOf(position));
      }
    }

    @Override
    public int getItemCount() {
      return mCurrentPoll.getQuestionList().size();
    }

    private void bindDualItem(DualItemHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));
      holder.option1.setText(q.getOption(0).getOptionName());
      holder.option2.setText(q.getOption(1).getOptionName());
      holder.counter1.setText(String.valueOf(q.getOption(0).getNumberOfVotes()));
      holder.counter2.setText(String.valueOf(q.getOption(1).getNumberOfVotes()));

      int vote1 = q.getOption(0).getNumberOfVotes();
      int vote2 = q.getOption(1).getNumberOfVotes();

      if (vote1 + vote2 > 0) {
        holder.counter1.setVisibility(View.VISIBLE);
        holder.counter2.setVisibility(View.VISIBLE);

        int level1 = (int) (vote1 / ((float) vote1 + vote2) * 10000);
        int level2 = (int) (vote2 / ((float) vote1 + vote2) * 10000);

        holder.option1.getBackground().setLevel(level1);
        holder.option2.getBackground().setLevel(level2);

        if (vote1 == vote2) {
          holder.option1.setTypeface(holder.option1.getTypeface(), Typeface.BOLD);
          holder.option2.setTypeface(holder.option2.getTypeface(), Typeface.BOLD);
        } else {
          holder.option1.setTypeface(holder.option1.getTypeface(),
              vote1 > vote2 ? Typeface.BOLD : Typeface.NORMAL);
          holder.option2.setTypeface(holder.option2.getTypeface(),
              vote1 > vote2 ? Typeface.NORMAL : Typeface.BOLD);
        }
      } else {
        holder.counter1.setVisibility(View.GONE);
        holder.counter2.setVisibility(View.GONE);
      }
    }

    private void bindRate(final MultipleItemHolder holder, final int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);

      int voters = q.getNumberOfVotes();

      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      holder.header.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          TransitionManager.beginDelayedTransition(mRecyclerView);

          boolean isExpanded = holder.header.isSelected();

          holder.header.setSelected(!isExpanded);
          holder.switcher.setDisplayedChild(isExpanded ? 0 : 1);
        }
      });

      int sum = 0;

      for (Option o : q.getAllOptions()) {
        View rowView;
        rowView = holder.container.findViewWithTag(o.getKey());
        if (rowView == null) {
          rowView = LayoutInflater.from(holder.container.getContext())
              .inflate(android.R.layout.simple_list_item_2, holder.container, false);
          rowView.setBackgroundResource(R.drawable.dw_level_start);
          rowView.setTag(o.getKey());
          holder.container.addView(rowView);
        }

        sum += Integer.parseInt(o.getOptionName()) * o.getNumberOfVotes();

        ((TextView) rowView.findViewById(android.R.id.text1))
            .setText(o.getOptionName());
        ((TextView) rowView.findViewById(android.R.id.text2))
            .setText(String.format("Votes: %d", o.getNumberOfVotes()));

        int level = (int) ((float) o.getNumberOfVotes() / voters * 10000);
        rowView.getBackground().setLevel(level);
      }

      float average = voters > 0 ? sum / voters : 0;

      holder.summary.setText(String.format("Average rating: %.2f", average));
    }

    private void bindLikeDislike(LikeDislikeHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      int voteDislike = q.getOption(0).getNumberOfVotes();
      int voteLike = q.getOption(1).getNumberOfVotes();

      holder.like.setText(String.valueOf(voteLike));
      holder.dislike.setText(String.valueOf(voteDislike));

      if (voteDislike + voteLike > 0) {

        int levelDislike = (int) (voteDislike / ((float) voteDislike + voteLike) * 10000);
        int levelLike = (int) (voteLike / ((float) voteDislike + voteLike) * 10000);

        holder.like.getBackground().setLevel(levelLike);
        holder.dislike.getBackground().setLevel(levelDislike);

        if (voteDislike == voteLike) {
          holder.dislike.setTypeface(holder.dislike.getTypeface(), Typeface.BOLD);
          holder.like.setTypeface(holder.like.getTypeface(), Typeface.BOLD);
        } else {
          holder.dislike.setTypeface(holder.dislike.getTypeface(),
              voteDislike > voteLike ? Typeface.BOLD : Typeface.NORMAL);
          holder.like.setTypeface(holder.like.getTypeface(),
              voteDislike > voteLike ? Typeface.NORMAL : Typeface.BOLD);
        }
      }
    }

    private void bindMultipleOption(final MultipleItemHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      int voters = q.getNumberOfVotes();

      holder.header.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          boolean isExpanded = holder.header.isSelected();
          TransitionManager.beginDelayedTransition(mRecyclerView);

          holder.header.setSelected(!isExpanded);
          holder.switcher.setDisplayedChild(isExpanded ? 0 : 1);
        }
      });

      for (Option option : q.getAllOptions()) {
        View rowView = holder.container.findViewWithTag(option.getKey());
        if (rowView == null) {
          rowView = LayoutInflater.from(holder.container.getContext())
              .inflate(android.R.layout.simple_list_item_2, holder.container, false);
          rowView.setTag(option.getKey());
          rowView.setBackgroundResource(R.drawable.dw_level_start);
          holder.container.addView(rowView);
        }

        ((TextView) rowView.findViewById(android.R.id.text1)).setText(option.getOptionName());
        ((TextView) rowView.findViewById(android.R.id.text2))
            .setText(String.format("Votes: %d", option.getNumberOfVotes()));

        int level = (int) ((float) option.getNumberOfVotes() / voters * 10000);
        rowView.getBackground().setLevel(level);
      }
      if (voters > 0) {
        List<Option> mostVoted = q.getMostVotedOptions();
        if (mostVoted.size() != 1) {
          StringBuilder summary = new StringBuilder("Most voted options: ");
          for (Option o : mostVoted) {
            summary.append(o.getOptionName());
            summary.append(", ");
          }
          summary.deleteCharAt(summary.length() - 1);
          summary.deleteCharAt(summary.length() - 1);  // This deletes last ", "
          holder.summary.setText(summary.toString());
        } else {
          holder.summary.setText("Most voted option: " + mostVoted.get(0).getOptionName());
        }
      } else {
        holder.summary.setText("No votes yet");
      }
    }

    class DualItemHolder extends RecyclerView.ViewHolder {

      TextView header;
      TextView option1;
      TextView option2;
      TextView counter1;
      TextView counter2;

      public DualItemHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        option1 = (TextView) itemView.findViewById(R.id.textview_details_item_binary_option1);
        option2 = (TextView) itemView.findViewById(R.id.textview_details_item_binary_option2);
        counter1 = (TextView) itemView
            .findViewById(R.id.textview_details_item_binary_option1_count);
        counter2 = (TextView) itemView
            .findViewById(R.id.textview_details_item_binary_option2_count);
      }
    }

    class MultipleItemHolder extends RecyclerView.ViewHolder {

      TextView header;
      LinearLayout container;
      ViewSwitcher switcher;
      TextView summary;

      public MultipleItemHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        container = (LinearLayout) itemView
            .findViewById(R.id.linearlayout_item_card_container);
        switcher = (ViewSwitcher) itemView.findViewById(R.id.viewswitcher_item_card);
        summary = (TextView) itemView.findViewById(R.id.textview_item_card_summary);
      }
    }

    class LikeDislikeHolder extends RecyclerView.ViewHolder {

      TextView header;
      TextView like;
      TextView dislike;

      public LikeDislikeHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        like = (TextView) itemView
            .findViewById(R.id.textview_details_item_likedislike_like_counter);
        dislike = (TextView) itemView
            .findViewById(R.id.textview_details_item_likedislike_dislike_counter);

      }
    }

    class SimpleHolder extends RecyclerView.ViewHolder {

      private TextView mText1;

      public SimpleHolder(View itemView) {
        super(itemView);

        mText1 = (TextView) itemView.findViewById(android.R.id.text1);
      }
    }
  }
}
