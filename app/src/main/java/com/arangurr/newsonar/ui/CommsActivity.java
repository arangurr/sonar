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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
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

  private TextView mDurationHeader;

  private Strategy.Builder mStrategyBuilder;

  private PublishOptions.Builder mPublishOptionsBuilder;
  private PublishOptions mPublishOptions;

  private Poll mCurrentPoll;

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
            Toast.makeText(CommsActivity.this, "Expired!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Publish expired");
            super.onExpired();
          }
        });

    mDurationHeader = (TextView) findViewById(R.id.textview_comms_duration_header);
    Spinner durationSpinner = (Spinner) findViewById(R.id.spinner_comms_duration);

    mDurationHeader.setOnClickListener(this);

    durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Strategy strategy;
        if (position < 6) {
          int[] ttlValues = getResources()
              .getIntArray(R.array.array_publish_durations_values);
          strategy = mStrategyBuilder.setTtlSeconds(ttlValues[position]).build();
          mDurationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          mDurationHeader.setClickable(false);
        } else {
          strategy = mStrategyBuilder.setTtlSeconds(Strategy.TTL_SECONDS_MAX).build();
          mDurationHeader.setCompoundDrawablesWithIntrinsicBounds(
              0, 0, R.drawable.ic_warning_24dp, 0);
          mDurationHeader.setClickable(true);

        }

        mPublishOptionsBuilder.setStrategy(strategy);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    durationSpinner.setSelection(3);

    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Nearby.MESSAGES_API)
        .addConnectionCallbacks(this)
        .enableAutoManage(this, this)
        .build();

    mMessageListener = new MessageListener() {
      @Override
      public void onFound(Message message) {
        super.onFound(message);
        String messageAsString = new String(message.getContent(), StandardCharsets.UTF_8);
        Log.d(TAG, "Found message" + messageAsString);
      }

      @Override
      public void onLost(Message message) {
        super.onLost(message);
        String messageAsString = new String(message.getContent());
        Log.d(TAG, "Message " + messageAsString + " was lost");
      }
    };
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_comms_publish:
        mPublishOptions = mPublishOptionsBuilder.build();
        publish();
        break;
      case R.id.textview_comms_duration_header:
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
  }

  @Override
  public void onConnectionSuspended(int i) {
    String cause;
    switch (i) {
      case CAUSE_SERVICE_DISCONNECTED:
        cause = "Service disconnected";
        break;
      case CAUSE_NETWORK_LOST:
        cause = "Network lost";
        break;
      default:
        cause = "Unknown" + i;
    }
    Log.i(TAG, "Google API connection suspended: " + cause);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.i(TAG, "Google Api connection failed");
  }

  @Override
  protected void onStop() {
    super.onStop();
    //unpublish();
    //unsubscribe();
  }

  private void subscribe() {
    Log.i(TAG, "Trying to subscribe");

    SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_SECONDS)
            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
            .build())
        .setCallback(new SubscribeCallback() {
          @Override
          public void onExpired() {
            super.onExpired();

            Log.d(TAG, "Subscription expired");

          }
        })
        .build();

    Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, subscribeOptions)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Subscribed successfully");
            } else {
              Log.d(TAG, "Couldn't subscribe due to status = " + status);
            }
          }
        });
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
            } else {
              Log.d(TAG, "Couldn't publish due to status = " + status);
            }
          }
        });
    Log.d(TAG, "Trying to publish");
  }

  private void unsubscribe() {
    Log.i(TAG, "unsubscribing");
    Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
  }

  private void unpublish() {
    Log.i(TAG, "Unpublishing");
    if (mActiveMessage != null) {
      Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage).setResultCallback(
          new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {

            }
          });
      mActiveMessage = null;
    }
  }
}
