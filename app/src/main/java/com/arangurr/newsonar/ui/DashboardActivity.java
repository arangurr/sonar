package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.R;
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

public class DashboardActivity extends AppCompatActivity implements GoogleApiClient
        .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DashboardActivity";
    private GoogleApiClient mGoogleApiClient;
    private Message mActiveMessage;
    private MessageListener mMessageListener;
    private TextView mReceivedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mReceivedTextView = (TextView) findViewById(R.id.textview_received);

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
                mReceivedTextView.setText(messageAsString);

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
        Button publishButton = (Button) findViewById(R.id.publishButton);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish("Hello World from button");
                subscribe();
            }
        });

        tv.setText(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Constants.KEY_USERNAME, "unknown"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
    }

    private void subscribe() {
        Log.i(TAG, "subscribing");

        SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
                .setStrategy(new Strategy.Builder()
                        .setTtlSeconds(Constants.TTL_SECONDS)
                        .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
                        .build())
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.d(TAG, "subscribe expired");
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

    private void publish(String message) {
        Log.i(TAG, "publish: " + message);
        mActiveMessage = new Message(message.getBytes(StandardCharsets.UTF_8));

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
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage, publishOptions)
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
    }

    @Override
    protected void onStop() {
        //unpublish();
        //unsubscribe();

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
                Intent i = (new Intent(this, EditorActivity.class));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
