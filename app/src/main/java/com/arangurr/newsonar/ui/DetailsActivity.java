package com.arangurr.newsonar.ui;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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
import com.arangurr.newsonar.data.VoterIdPair;
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
import java.util.ArrayList;
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

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    mRecyclerView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          if (behavior.getState() == BottomSheetBehavior.STATE_DRAGGING
              || behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
          }
        }
        return false;
      }
    });

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
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return false;
    }
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

  private void showVotersDialog(ArrayList<VoterIdPair> voterList) {
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    for (VoterIdPair voter : voterList) {
      adapter.add(voter.getUserName());
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Voted by");
    builder.setAdapter(adapter, null);
    builder.show();
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
        case Constants.RATE_MODE_STARS:
          return R.layout.details_card_stars;
        case Constants.RATE_MODE_CUSTOM:
        case Constants.RATE_MODE_SCORE:
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
        case R.layout.details_card_stars:
          inflatedView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.details_card_stars, parent, false);
          return new StarsItemHolder(inflatedView);
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
        case R.layout.details_card_stars:
          bindStars((StarsItemHolder) holder, position);
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
      final Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));
      holder.option1.setText(q.getOption(0).getOptionName());
      holder.option2.setText(q.getOption(1).getOptionName());

      holder.header.setCompoundDrawables(null, null, null, null);

      int vote1 = q.getOption(0).getNumberOfVotes();
      int vote2 = q.getOption(1).getNumberOfVotes();

      holder.counter1.setText(String.valueOf(vote1));
      holder.counter2.setText(String.valueOf(vote2));

      holder.counter1.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showVotersDialog(q.getOption(0).getVoterList());
        }
      });
      holder.counter2.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showVotersDialog(q.getOption(1).getVoterList());
        }
      });

      holder.counter1.setClickable(vote1 > 0
          && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);
      holder.counter2.setClickable(vote2 > 0
          && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);

      if (vote1 + vote2 > 0) {
        int[] progressColors = getResources().getIntArray(R.array.progress_rainbow);
        setProgressProportions(holder.progress, q.getAllOptions(), progressColors, vote1 + vote2);

        if (vote1 == vote2) {
          holder.option1.setTypeface(holder.option1.getTypeface(), Typeface.BOLD);
          holder.option2.setTypeface(holder.option2.getTypeface(), Typeface.BOLD);
        } else {
          holder.option1.setTypeface(holder.option1.getTypeface(),
              vote1 > vote2 ? Typeface.BOLD : Typeface.NORMAL);
          holder.option2.setTypeface(holder.option2.getTypeface(),
              vote1 > vote2 ? Typeface.NORMAL : Typeface.BOLD);
        }
      }
    }

    private void bindRate(final MultipleItemHolder holder, final int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);

      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));
      holder.header.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          expandOrCollapseWithAnimation(holder.header, holder.switcher);
        }
      });

      int voters = q.getNumberOfVotes();
      int sum = 0;

      for (final Option o : q.getAllOptions()) {
        View rowView;
        rowView = holder.container.findViewWithTag(o.getKey());
        if (rowView == null) {
          rowView = LayoutInflater.from(holder.container.getContext())
              .inflate(R.layout.item_twolines_with_progress, holder.container, false);
          rowView.setTag(o.getKey());
          holder.container.addView(rowView);
        }

        sum += Integer.parseInt(o.getOptionName()) * o.getNumberOfVotes();

        TextView name = (TextView) rowView.findViewById(R.id.textview_item_card_option_name);
        TextView counter = (TextView) rowView.findViewById(R.id.textview_item_card_option_counter);
        ImageView imageColor = (ImageView) rowView.findViewById(R.id.imageview_item_card_option);
        ProgressBar progressBar = (ProgressBar) rowView
            .findViewById(R.id.progressbar_item_card_option_progress);

        name.setText("Rating: " + o.getOptionName());
        counter.setText(String.valueOf(o.getNumberOfVotes()));

        counter.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            showVotersDialog(o.getVoterList());
          }
        });

        counter.setClickable(o.getNumberOfVotes() > 0
            && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);

        imageColor.setVisibility(View.GONE);

        progressBar.setMax(voters);
        progressBar.setProgress(o.getNumberOfVotes());
      }

      float average = voters > 0 ? ((float) sum / voters) : 0;

      holder.summary.setText("Average rating: " + String.valueOf(average));
    }

    private void expandOrCollapseWithAnimation(TextView header, ViewSwitcher switcher) {
      TransitionSet set = new TransitionSet();
      set.addTransition(new Fade());
      set.addTransition(new ChangeBounds());
      set.setDuration(50);
      TransitionManager
          .beginDelayedTransition(mRecyclerView, set);

      boolean isExpanded = header.isSelected();
      header.setSelected(!isExpanded);
      switcher.setDisplayedChild(isExpanded ? 0 : 1);
    }

    private void bindLikeDislike(LikeDislikeHolder holder, int position) {
      final Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      holder.header.setCompoundDrawables(null, null, null, null);

      int voteDislike = q.getOption(0).getNumberOfVotes();
      int voteLike = q.getOption(1).getNumberOfVotes();

      holder.like.setText(String.valueOf(voteLike));
      holder.dislike.setText(String.valueOf(voteDislike));

      holder.dislike.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showVotersDialog(q.getOption(0).getVoterList());
        }
      });
      holder.like.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showVotersDialog(q.getOption(1).getVoterList());
        }
      });

      holder.dislike.setClickable(voteDislike > 0
          && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);
      holder.like.setClickable(voteLike > 0
          && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);

      // TODO: 18/05/2017 disable click if poll is private

      if (voteDislike + voteLike > 0) {
        int[] progressColors = getResources().getIntArray(R.array.progress_like_dislike);

        setProgressProportions(holder.progress, q.getAllOptions(), progressColors,
            voteDislike + voteLike);

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

    private void setProgressProportions(LinearLayout progress, ArrayList<Option> options,
        int[] progressColors, int total) {
      progress.setVisibility(View.VISIBLE);
      for (Option o : options) {
        View view = progress.findViewWithTag(o.getKey());
        if (view == null) {
          view = new View(progress.getContext());
          if (o.getKey() < progressColors.length) {
            view.setBackgroundColor(progressColors[o.getKey()]);
          }
          view.setTag(o.getKey());
          progress.addView(view);
        }
        LayoutParams params = new LayoutParams(
            0,
            LayoutParams.MATCH_PARENT);
        params.weight = o.getNumberOfVotes() / ((float) total);
        view.setLayoutParams(params);
      }
    }

    private void bindMultipleOption(final MultipleItemHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      holder.header.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          expandOrCollapseWithAnimation(holder.header, holder.switcher);
        }
      });
      int voters = q.getNumberOfVotes();

      int[] rainbowColors = getResources().getIntArray(R.array.progress_rainbow);

      for (final Option option : q.getAllOptions()) {
        View rowView = holder.container.findViewWithTag(option.getKey());
        if (rowView == null) {
          rowView = LayoutInflater.from(holder.container.getContext())
              .inflate(R.layout.item_twolines_with_progress, holder.container, false);
          rowView.setTag(option.getKey());
          holder.container.addView(rowView);
        }

        TextView name = ((TextView) rowView.findViewById(R.id.textview_item_card_option_name));
        TextView counter = ((TextView) rowView
            .findViewById(R.id.textview_item_card_option_counter));
        ImageView imageColor = (ImageView) rowView.findViewById(R.id.imageview_item_card_option);
        ProgressBar progressBar = (ProgressBar) rowView
            .findViewById(R.id.progressbar_item_card_option_progress);

        name.setText(option.getOptionName());
        counter.setText(String.valueOf(option.getNumberOfVotes()));

        counter.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            showVotersDialog(option.getVoterList());
          }
        });

        counter.setClickable(option.getNumberOfVotes() > 0
            && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);

        imageColor.getDrawable().mutate().setTint(rainbowColors[option.getKey()]);
        progressBar.setMax(voters);
        progressBar.setProgress(option.getNumberOfVotes());
      }

      if (voters > 0) {
        setProgressProportions(holder.progress, q.getAllOptions(),
            getResources().getIntArray(R.array.progress_rainbow), voters);
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

    private void bindStars(final StarsItemHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(String.format("%s. %s", String.valueOf(position + 1), q.getTitle()));

      holder.header.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          expandOrCollapseWithAnimation(holder.header, holder.switcher);
        }
      });

      int voters = q.getNumberOfVotes();
      int sum = 0;

      for (int i = 0; i < q.getAllOptions().size(); i++) {
        final Option option = q.getOption(i);
        View rowView = holder.container.findViewWithTag(option.getKey());
        if (rowView == null) {
          rowView = LayoutInflater.from(holder.container.getContext())
              .inflate(R.layout.item_twolines_with_progress, holder.container, false);
          rowView.setTag(option.getKey());
          holder.container.addView(rowView, 0);
        }
        sum += Integer.parseInt(option.getOptionName()) * option.getNumberOfVotes();

        TextView name = (TextView) rowView.findViewById(R.id.textview_item_card_option_name);
        TextView counter = (TextView) rowView.findViewById(R.id.textview_item_card_option_counter);
        ImageView colorImage = (ImageView) rowView.findViewById(R.id.imageview_item_card_option);
        ProgressBar progressBar = (ProgressBar) rowView
            .findViewById(R.id.progressbar_item_card_option_progress);

        name.setText(option.getOptionName());
        Drawable star = getDrawable(R.drawable.ic_star_24dp);
        star.setTint(ContextCompat.getColor(name.getContext(), R.color.colorRatePrimary));
        name.setCompoundDrawablePadding(8);
        name.setCompoundDrawablesWithIntrinsicBounds(star, null, null, null);
        counter.setText(String.valueOf(option.getNumberOfVotes()));

        counter.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            showVotersDialog(option.getVoterList());
          }
        });

        counter.setClickable(option.getNumberOfVotes() > 0
            && mCurrentPoll.getPrivacySetting() == Constants.PRIVACY_PUBLIC);

        colorImage.setVisibility(View.GONE);

        progressBar.setMax(voters);
        progressBar.setProgress(option.getNumberOfVotes());
      }

      float average = voters > 0 ? ((float) sum / voters) : 0;

      holder.summary.setText(String.valueOf(average));
      holder.stars.setRating(average);
    }


    class DualItemHolder extends RecyclerView.ViewHolder {

      TextView header;
      TextView option1;
      TextView option2;
      TextView counter1;
      TextView counter2;
      LinearLayout progress;

      public DualItemHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        option1 = (TextView) itemView.findViewById(R.id.textview_details_item_binary_option1);
        option2 = (TextView) itemView.findViewById(R.id.textview_details_item_binary_option2);
        counter1 = (TextView) itemView.findViewById(R.id.textview_details_item_count1);
        counter2 = (TextView) itemView.findViewById(R.id.textview_details_item_count2);
        progress = (LinearLayout) itemView
            .findViewById(R.id.linearlayout_details_item_progress);
      }
    }

    class MultipleItemHolder extends RecyclerView.ViewHolder {

      TextView header;
      LinearLayout container;
      ViewSwitcher switcher;
      TextView summary;
      LinearLayout progress;


      public MultipleItemHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        container = (LinearLayout) itemView
            .findViewById(R.id.linearlayout_item_card_container);
        switcher = (ViewSwitcher) itemView.findViewById(R.id.viewswitcher_item_card);
        summary = (TextView) itemView.findViewById(R.id.textview_item_card_summary);
        progress = (LinearLayout) itemView
            .findViewById(R.id.linearlayout_details_item_progress);
      }
    }

    class LikeDislikeHolder extends RecyclerView.ViewHolder {

      TextView header;
      TextView like;
      TextView dislike;
      LinearLayout progress;

      public LikeDislikeHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        like = (TextView) itemView.findViewById(R.id.textview_details_item_count2);
        dislike = (TextView) itemView.findViewById(R.id.textview_details_item_count1);
        progress = (LinearLayout) itemView.findViewById(R.id.linearlayout_details_item_progress);
      }
    }

    class StarsItemHolder extends RecyclerView.ViewHolder {

      TextView header;
      LinearLayout container;
      ViewSwitcher switcher;
      TextView summary;
      RatingBar stars;

      public StarsItemHolder(View itemView) {
        super(itemView);

        header = (TextView) itemView.findViewById(R.id.textview_details_item_header_title);
        container = (LinearLayout) itemView.findViewById(R.id.linearlayout_item_card_container);
        switcher = (ViewSwitcher) itemView.findViewById(R.id.viewswitcher_item_card);
        summary = (TextView) itemView.findViewById(R.id.textview_item_card_summary);
        stars = (RatingBar) itemView.findViewById(R.id.ratingbar_item_card_summary);
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
