package com.arangurr.newsonar.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.nearby.messages.SubscribeOptions.Builder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ListenActivity extends AppCompatActivity implements ConnectionCallbacks,
    OnConnectionFailedListener, ResultCallback<Status> {

  private static final String TAG = "ListenActivity";

  private GoogleApiClient mGoogleApiClient;
  private MessageListener mMessageListener;

  private SubscribeOptions mSubscribeOptions;

  private Switch mSwitch;

  private ArrayAdapter mNearbyPollsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_listen);

    mMessageListener = new MessageListener() {
      @Override
      public void onFound(Message message) {
        super.onFound(message);
        final String messageAsString = new String(message.getContent(), StandardCharsets.UTF_8);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mNearbyPollsAdapter.add(GsonUtils.deserializeGson(messageAsString, Poll.class));
          }
        });
        Log.d(TAG, "Found message " + messageAsString);
      }

      @Override
      public void onLost(Message message) {
        super.onLost(message);
        final String messageAsString = new String(message.getContent(), StandardCharsets.UTF_8);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mNearbyPollsAdapter.remove(GsonUtils.deserializeGson(messageAsString, Poll.class));
          }
        });
        Log.d(TAG, "Message " + messageAsString + " was lost");
      }
    };

    mSubscribeOptions = new Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(Strategy.TTL_SECONDS_DEFAULT)
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISCOVERY_MODE_DEFAULT)
            .build())
        .setCallback(new SubscribeCallback() {
          @Override
          public void onExpired() {
            super.onExpired();
            Log.d(TAG, "Suscription expired");
            mSwitch.setChecked(false);
          }
        })
        .build();

    mSwitch = (Switch) findViewById(R.id.switch_listen_subscribe);

    final List<Poll> nearbyPolls = new ArrayList<>();
    mNearbyPollsAdapter = new ArrayAdapter<>(
        this,
        android.R.layout.simple_list_item_1,
        nearbyPolls);
    final ListView nearbyDevicesListView = (ListView) findViewById(
        R.id.listview_listen);
    if (nearbyDevicesListView != null) {
      nearbyDevicesListView.setAdapter(mNearbyPollsAdapter);
    }

    mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mGoogleApiClient.isConnected()) {
          if (isChecked) {
            subscribe();
          } else {
            unsubscribe();
            mNearbyPollsAdapter.clear();
          }
        }
      }
    });

    buildGoogleApiClient();
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(TAG, "Google API connected");
    if (mSwitch.isChecked()) {
      subscribe();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.e(TAG, "Google API connection suspended: " + i);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e(TAG, "Google API connection failed with code: " + connectionResult.getErrorCode());
    finish();
  }

  private void subscribe() {
    Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, mSubscribeOptions)
        .setResultCallback(this);
    Log.d(TAG, "Trying to subscribe");
  }

  private void unsubscribe() {
    Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    Log.d(TAG, "Unsubscribing");
  }

  @Override
  public void onResult(@NonNull Status status) {
    if (status.isSuccess()) {
      Log.d(TAG, "Operation successful");
      mSwitch.setEnabled(true);
    } else {
      Log.d(TAG, "Operation unsuccessful due to " + status.getStatusMessage());
      Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
      mSwitch.setEnabled(false);
      Snackbar.make(mSwitch, "test", Snackbar.LENGTH_INDEFINITE)
          .setAction("No Internet connection", new OnClickListener() {
            @Override
            public void onClick(View v) {
              subscribe();
            }
          })
          .show();
    }
  }

  /*
  private void dealWithStatus(Status status) {
    switch (status.getStatusCode()) {
      case NearbyMessagesStatusCodes.APP_NOT_OPTED_IN:
        try {
          status.startResolutionForResult(this, Constants.REQUEST_RESOLVE_ERROR);
        } catch (SendIntentException e) {
          e.printStackTrace();
        }
        break;
      case CommonStatusCodes.NETWORK_ERROR:
        Snackbar.make(findViewById(R.id.switch_listen_subscribe),
            "No connectivity. Please connect to the Internet",
            Snackbar.LENGTH_LONG)
            .show();
        mSwitch.setChecked(false);
        break;
      default:
        Log.d(TAG, "Status code unknown");
    }
  }
*/

  private Activity getActivity() {
    Context context = this.getBaseContext();
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return null;
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