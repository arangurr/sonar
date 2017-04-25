package com.arangurr.newsonar.ui;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
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

public class CommsActivity extends AppCompatActivity implements View.OnClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private static final String TAG = "CommsActivity";

  private GoogleApiClient mGoogleApiClient;
  private Message mActiveMessage;
  private MessageListener mMessageListener;

  private TextView mDurationTextView;
  private TextView mStatusTextView;
  private ProgressBar mStatusProgressBar;
  private ToggleButton mToggleButton;

  private Strategy.Builder mStrategyBuilder;

  private PublishOptions.Builder mPublishOptionsBuilder;
  private PublishOptions mPublishOptions;

  private Poll mCurrentPoll;
  private Spinner mDurationSpinner;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_comms);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      UUID pollId = (UUID) extras.getSerializable(Constants.EXTRA_POLL_ID);
      mCurrentPoll = PersistenceUtils.fetchPollWithId(this, pollId);
    } else {
      Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
      finish();
    }

    getSupportActionBar().setTitle(mCurrentPoll.getPollTitle());

    mStrategyBuilder = new Strategy.Builder()
        .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
        .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT);

    mPublishOptionsBuilder = new PublishOptions.Builder()
        .setCallback(new PublishCallback() {
          @Override
          public void onExpired() {
            Log.d(TAG, "Publish expired");
            mStatusTextView
                .setText("Poll no longer available.\nWaiting for votes a little longer...");
            super.onExpired();
          }
        });

    mDurationTextView = (TextView) findViewById(R.id.textview_comms_duration);
    mStatusTextView = (TextView) findViewById(R.id.textview_comms_status);
    mStatusProgressBar = (ProgressBar) findViewById(R.id.progressbar_comms_status);
    mToggleButton = (ToggleButton) findViewById(R.id.toggle_comms);
    mDurationSpinner = (Spinner) findViewById(R.id.spinner_comms_duration);

    mToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mGoogleApiClient.isConnected()) {
          if (isChecked) {
            mPublishOptions = mPublishOptionsBuilder.build();
            mStatusProgressBar.setVisibility(View.VISIBLE);
            publish();
            subscribe();
            mDurationSpinner.setEnabled(false);
          } else {
            unpublish();
            unsubscribe();
            mDurationSpinner.setEnabled(true);
          }
        }
      }
    });

    mDurationTextView.setOnClickListener(this);

    mDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Strategy strategy;
        if (position < 6) {
          int[] ttlValues = getResources()
              .getIntArray(R.array.array_publish_durations_values);
          strategy = mStrategyBuilder.setTtlSeconds(ttlValues[position]).build();
          mDurationTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          mDurationTextView.setClickable(false);
        } else {
          strategy = mStrategyBuilder.setTtlSeconds(Strategy.TTL_SECONDS_MAX).build();
          mDurationTextView.setCompoundDrawablesWithIntrinsicBounds(
              0, 0, R.drawable.ic_warning_24dp, 0);
          mDurationTextView.setClickable(true);

        }

        mPublishOptionsBuilder.setStrategy(strategy);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    mDurationSpinner.setSelection(3);

    mMessageListener = new MessageListener() {
      @Override
      public void onFound(Message message) {
        super.onFound(message);
        final String messageAsString = new String(message.getContent(), StandardCharsets.UTF_8);
        Vote v = GsonUtils.deserializeGson(messageAsString, Vote.class);
        if (v != null) {
          Log.d(TAG, "Trying to update Poll with found Vote");
          mCurrentPoll.updateWithVote(v);
          PersistenceUtils.storePollInPreferences(getBaseContext(), mCurrentPoll);
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
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.textview_comms_duration:
        Snackbar snackbar = Snackbar.make(v, "Longer times drain battery faster. " +
            "Please use accordingly.", LENGTH_LONG);
        snackbar.show();
        break;

      default:
        // Do nothing
    }

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
    // Subscribe for 30 seconds more than the currently set publication
    int[] ttlValues = getResources()
        .getIntArray(R.array.array_publish_durations_values);
    int currentTtl = ttlValues[mDurationSpinner.getSelectedItemPosition()];

    SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(currentTtl + Constants.TTL_SECONDS)
            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
            .build())
        .setCallback(new SubscribeCallback() {
          @Override
          public void onExpired() {
            super.onExpired();
            Log.d(TAG, "Subscription expired");
            mToggleButton.setChecked(false);
            mStatusProgressBar.setVisibility(View.GONE);
            mStatusTextView.setText("");
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
    String message = GsonUtils.serialize(mCurrentPoll);
    mActiveMessage = new Message(message.getBytes(StandardCharsets.UTF_8),  // Not documented
        Constants.NAMESPACE,                                                // Namespace
        Poll.TYPE);                                                         // Type of message

    Nearby.Messages.publish(mGoogleApiClient, mActiveMessage, mPublishOptions)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Published successfully");
              mStatusTextView.append("\nPoll is available");
            } else {
              Log.d(TAG, "Couldn't publish due to status = " + status);
            }
          }
        });
    Log.d(TAG, "Trying to publish");
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
              mStatusProgressBar.setVisibility(View.GONE);
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
}
