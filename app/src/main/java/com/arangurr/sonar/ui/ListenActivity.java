package com.arangurr.sonar.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.arangurr.sonar.BuildConfig;
import com.arangurr.sonar.Constants;
import com.arangurr.sonar.GsonUtils;
import com.arangurr.sonar.PersistenceUtils;
import com.arangurr.sonar.R;
import com.arangurr.sonar.data.Poll;
import com.arangurr.sonar.data.Question;
import com.arangurr.sonar.data.Vote;
import com.arangurr.sonar.ui.ListenRecyclerAdapter.OnItemClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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
import com.google.android.gms.nearby.messages.SubscribeOptions.Builder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ListenActivity extends AppCompatActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {

  private static final String TAG = "ListenActivity";

  private GoogleApiClient mGoogleApiClient;
  private MessageListener mMessageListener;

  private Vote mCurrentVote;

  private Switch mSwitch;
  private ProgressBar mStatusProgressBar;
  private TextView mStatusTextView;

  private ListenRecyclerAdapter mNearbyPollsAdapter;
  private Message mCurrentMessage;
  private RecyclerView mNearbyDevicesRecyclerView;

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

    mSwitch = (Switch) findViewById(R.id.switch_listen_subscribe);
    mStatusProgressBar = (ProgressBar) findViewById(R.id.progressbar_listen_status);
    mStatusTextView = (TextView) findViewById(R.id.textview_listen_status);
    mNearbyDevicesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_listen);

    if (BuildConfig.DEBUG) {
      findViewById(R.id.testButton).setVisibility(View.VISIBLE);
    }

    final List<Poll> nearbyPolls = new ArrayList<>();
    mNearbyPollsAdapter = new ListenRecyclerAdapter(nearbyPolls);
    mNearbyPollsAdapter.setItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(View view, Poll poll) {
        if (poll.isPasswordProtected()) {
          launchPasswordDialog(view, poll);
        } else {
          startVotingForPoll(poll);
        }
      }
    });

    mNearbyDevicesRecyclerView.setAdapter(mNearbyPollsAdapter);

    mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mGoogleApiClient.isConnected()) {
          if (isChecked) {
            subscribe();
          } else {
            unsubscribe();
            unpublish();
            mNearbyPollsAdapter.clear();
          }
        }
      }
    });

    buildGoogleApiClient();
  }

  private void launchPasswordDialog(View view, final Poll p) {
    final View dialogView = LayoutInflater.from(view.getContext())
        .inflate(R.layout.sheet_password, null);

    final AlertDialog passwordDialog = new AlertDialog.Builder(view.getContext())
        .setTitle(R.string.unlock_dialog_title)
        .setView(dialogView)
        .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, null)
        .create();

    passwordDialog.setOnShowListener(new OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        Button positiveButton = passwordDialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            TextInputLayout inputLayout =
                (TextInputLayout) dialogView.findViewById(R.id.textinputlayout_sheet);
            TextInputEditText inputEditText =
                (TextInputEditText) dialogView.findViewById(R.id.textinputedittext_sheet);

            if (inputEditText.getText().toString().equals(p.getPassword())) {
              startVotingForPoll(p);
              passwordDialog.dismiss();
            } else {
              inputLayout.setError(getString(R.string.wrong_password));
            }
          }
        });

      }
    });

    passwordDialog.show();
  }

  private void startVotingForPoll(Poll poll) {
    PersistenceUtils.deleteVote(this);
    String serialized = GsonUtils.serialize(poll);
    Intent voteIntent = new Intent(ListenActivity.this, VotingActivity.class);
    voteIntent.putExtra(Constants.EXTRA_SERIALIZED_POLL, serialized);
    getActivity().startActivityForResult(voteIntent, Constants.VOTE_REQUEST);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.VOTE_REQUEST) {
      switch (resultCode) {
        case RESULT_CANCELED:
          PersistenceUtils.deleteVote(this);
          break;
        case RESULT_OK:
          mCurrentVote = PersistenceUtils.fetchVote(this);
          mNearbyDevicesRecyclerView.setVisibility(View.INVISIBLE);
          publish();
          break;
      }
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.d(TAG, "Google API connected");
    mCurrentVote = PersistenceUtils.fetchVote(this);
    if (mSwitch.isChecked()) {
      if (mCurrentVote == null) {
        subscribe();
      } else {
        publish();
      }
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
    SubscribeOptions subscribeOptions = new Builder()
        .setStrategy(new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_10MIN)
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISCOVERY_MODE_DEFAULT)
            .build())
        .setCallback(new SubscribeCallback() {
          @Override
          public void onExpired() {
            super.onExpired();
            Log.d(TAG, "Subscription expired");
            mSwitch.setChecked(false);
            setStatus(null);
          }
        })
        .setFilter(new MessageFilter.Builder()
            .includeNamespacedType(Constants.NAMESPACE, Poll.TYPE)
            .build())
        .build();

    Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, subscribeOptions)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.d(TAG, "Operation successful");
              mSwitch.setEnabled(true);
              setStatus(getString(R.string.status_searching));
            } else {
              Log.d(TAG, "Operation unsuccessful due to " + status.getStatusMessage());
              Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT)
                  .show();
              mSwitch.setEnabled(false);

            }
          }
        });
    Log.d(TAG, "Trying to subscribe");
  }

  private void unsubscribe() {
    Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener).setResultCallback(
        new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              setStatus(null);
            }
          }
        }
    );
    Log.d(TAG, "Unsubscribing");
    setStatus(getString(R.string.cancelling));
  }

  private void publish() {
    String messageAsString = GsonUtils.serialize(mCurrentVote);

    mCurrentMessage = new Message(messageAsString.getBytes(StandardCharsets.UTF_8),
        Constants.NAMESPACE,
        Vote.TYPE);

    final PublishOptions options = new PublishOptions.Builder()
        .setStrategy(
            new Strategy.Builder()
                .setTtlSeconds(Constants.TTL_30SEC)
                .build())
        .setCallback(
            new PublishCallback() {
              @Override
              public void onExpired() {
                super.onExpired();
                mSwitch.setChecked(false);
                Log.d(TAG, "Vote publication expired");
                Snackbar
                    .make(mStatusTextView, R.string.status_vote_sent, Snackbar.LENGTH_INDEFINITE)
                    .show();
                setStatus(null);
                mStatusTextView.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    PersistenceUtils.deleteVote(getApplicationContext());
                    finish();
                  }
                }, 10000);
              }
            })
        .build();

    Nearby.Messages.publish(mGoogleApiClient, mCurrentMessage, options)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              mStatusProgressBar.setIndeterminate(false);
              mStatusProgressBar.setMax(Constants.TTL_30SEC * 100);
              mStatusProgressBar.setProgress(0);

              CountDownTimer timer = new CountDownTimer(Constants.TTL_30SEC * 1000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                  if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    mStatusProgressBar.setProgress((int) (millisUntilFinished / 10), true);
                  } else {
                    mStatusProgressBar.setProgress((int) (millisUntilFinished / 10));
                  }
                }

                @Override
                public void onFinish() {
                }
              }.start();

              Log.d(TAG, "Vote published successfully");
              mStatusTextView.setText(R.string.status_sending_answers);
            } else {
              Log.d(TAG, "Couldn't publish vote due to status = " + status);
              mStatusTextView.setText(R.string.status_could_not_send);
            }
          }
        });
    Log.d(TAG, "Trying to publish");
    setStatus(getString(R.string.status_trying_to_send));
  }

  private void unpublish() {
    if (mCurrentMessage != null) {
      Nearby.Messages.unpublish(mGoogleApiClient, mCurrentMessage)
          .setResultCallback(
              new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                  if (status.isSuccess()) {
                    Log.d(TAG, "Vote unpublished successfully");
                  } else {
                    Log.d(TAG, "Couldn't unpublish due to status = " + status);
                  }
                }
              });
      mCurrentMessage = null;
    }
  }

  private Activity getActivity() {
    Context context = this;
    if (context instanceof Activity) {
      return this;
    } else {
      context = this.getBaseContext();
    }
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

  public void launchVotingUi(View view) {
    // Sample Poll
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
    q = new Question("8th Question", Constants.MULTI_MODE_EXCLUSIVE);
    q.addOption("First Option");
    q.addOption("Second Option");
    q.addOption("Third Option");
    q.addOption("Fourth Option");
    poll.addQuestion(q);
    q = new Question("9th Question", Constants.MULTI_MODE_MULTIPLE);
    q.addOption("First Option");
    q.addOption("Second Option");
    q.addOption("Third Option");
    poll.addQuestion(q);

    String serialized = GsonUtils.serialize(poll);

    PersistenceUtils.deleteVote(this);

    Intent voteIntent = new Intent(this, VotingActivity.class);
    voteIntent.putExtra(Constants.EXTRA_SERIALIZED_POLL, serialized);
    startActivityForResult(voteIntent, Constants.VOTE_REQUEST);

  }

  private void setStatus(@Nullable String status) {
    mStatusTextView.setText(status);
    if (status == null) {
      mStatusProgressBar.setVisibility(View.INVISIBLE);
      mStatusTextView.setVisibility(View.INVISIBLE);
    } else {
      mStatusProgressBar.setVisibility(View.VISIBLE);
      mStatusTextView.setVisibility(View.VISIBLE);
    }

  }
}