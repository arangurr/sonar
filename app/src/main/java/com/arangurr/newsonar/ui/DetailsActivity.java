package com.arangurr.newsonar.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
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
import java.util.UUID;

public class DetailsActivity extends AppCompatActivity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = "DetailsActivity";

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
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
          behavior.setState(mToggleButton.isChecked() ?
              BottomSheetBehavior.STATE_EXPANDED :  // Prevent collapsing
              BottomSheetBehavior.STATE_COLLAPSED); // Prevent expanding
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

  /*private void setStatusView() {
    if (mViewSwitcher.getNextView().getId() == R.id.linearlayout_details_status) {
      mViewSwitcher.showNext();
    }
  }

  private void setDetailsView() {
    if (mViewSwitcher.getNextView().getId() == R.id.recyclerview_details) {
      mViewSwitcher.showNext();
      mAdapter.notifyDataSetChanged();
    }
  }
*/

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
        case Constants.BINARY_MODE_UPDOWNVOTE:
        case Constants.BINARY_MODE_YESNO:
          return R.layout.item_card_binary;
      }
      return android.R.layout.simple_list_item_1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View inflatedView;
      switch (viewType) {
        case R.layout.item_card_binary:
          inflatedView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_card_binary, parent, false);
          return new BinaryHolder(inflatedView);
//        case R.layout.item_card_multi:
//        case R.layout.item_card_rate:
      }
      inflatedView = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      return new SimpleHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      switch (getItemViewType(position)) {
        case R.layout.item_card_binary:
          bindBinary((BinaryHolder) holder, position);
          break;
        default:
          ((SimpleHolder) holder).mText1.setText(String.valueOf(position));
      }
    }

    @Override
    public int getItemCount() {
      return mCurrentPoll.getQuestionList().size();
    }

    private void bindBinary(BinaryHolder holder, int position) {
      Question q = mCurrentPoll.getQuestionList().get(position);
      holder.header.setText(q.getTitle());
      holder.headerNumber.setText(String.valueOf(position));
      holder.option1.setText(String.format(
          "%1$s, (%2$d)",
          q.getOption(0).getOptionName(),
          q.getOption(0).getNumberOfVotes()));
      holder.option2.setText(String.format(
          "%1$s, (%2$d)",
          q.getOption(1).getOptionName(),
          q.getOption(1).getNumberOfVotes()));
    }

    class BinaryHolder extends RecyclerView.ViewHolder {

      TextView headerNumber;
      TextView header;
      TextView option1;
      TextView option2;

      public BinaryHolder(View itemView) {
        super(itemView);

        headerNumber = (TextView) itemView.findViewById(R.id.textview_editor_item_header_counter);
        header = (TextView) itemView.findViewById(R.id.textview_editor_item_header_title);
        option1 = (TextView) itemView.findViewById(R.id.textview_editor_item_binary_option1);
        option2 = (TextView) itemView.findViewById(R.id.textview_editor_item_binary_option2);
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
