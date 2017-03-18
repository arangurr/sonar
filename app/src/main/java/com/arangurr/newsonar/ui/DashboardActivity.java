package com.arangurr.newsonar.ui;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;

public class DashboardActivity extends AppCompatActivity implements GoogleApiClient
        .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DashboardActivity";
    private GoogleApiClient mGoogleApiClient;
    private Message mActiveMessage;
    private MessageListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();

        mMessageListener = new MessageListener() {

            @Override
            public void onFound(Message message) {
                super.onFound(message);
                String messageAsString = new String(message.getContent());
                Log.d(TAG, "onFound: " + messageAsString);
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                String messageAsString = new String(message.getContent());
                Log.d(TAG, "onLost: " + messageAsString);
            }
        };

        TextView tv = (TextView) findViewById(R.id.textView);

        tv.setText(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Constants.KEY_USERNAME, "unknown"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        publish("Hello World");
        subscribe();
    }

    private void subscribe() {
        Log.i(TAG, "subscribing");
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener);
    }

    private void publish(String message) {
        Log.i(TAG, "publish: " + message);
        mActiveMessage = new Message(message.getBytes());

        PublishOptions publishOptions = new PublishOptions.Builder()
                .setStrategy(new Strategy.Builder()
                        .setTtlSeconds(Constants.TTL_SECONDS)
                        .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
                        .build())
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.d(TAG, "publish expired");
                    }
                })
                .build();
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage, publishOptions);
    }

    @Override
    protected void onStop() {
        unpublish();
        unsubscribe();

        super.onStop();
    }

    private void unsubscribe() {
        Log.i(TAG, "unsubscribing");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    private void unpublish() {
        Log.i(TAG, "unpublishing");
        if (mActiveMessage != null) {
            Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage);
            mActiveMessage = null;
        }
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
}
