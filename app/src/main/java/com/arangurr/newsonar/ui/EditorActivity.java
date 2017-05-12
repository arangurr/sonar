package com.arangurr.newsonar.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ArcMotion;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import java.util.UUID;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener,
    OnDismissListener {

  private Switch mPasswordSwitch;
  private EditText mPasswordEditText;
  private TextView mPrivacyTextView;
  private Spinner mPrivacySpinner;
  private CardView mFabCard;
  private CardView mConfigCard;
  private EditorRecyclerAdapter mAdapter;

  private boolean mIsCardAnimating = false;
  private boolean mIsFabAnimating = false;

  private Poll mPoll;
  private FloatingActionButton mFab;
  private OnSharedPreferenceChangeListener mPreferenceChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editor);
    setSupportActionBar(toolbar);

    if (savedInstanceState == null
        || savedInstanceState.getSerializable(Constants.EXTRA_POLL_ID) == null) {
      mPoll = new Poll(this);
    } else {
      UUID possibleId = (UUID) savedInstanceState.getSerializable(Constants.EXTRA_POLL_ID);
      if (possibleId != null) {
        mPoll = PersistenceUtils.fetchPollWithId(this, possibleId);
      }
    }

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
    toolbar.getNavigationIcon().mutate().setColorFilter(Color.WHITE, Mode.SRC_IN);

    mPasswordSwitch = (Switch) findViewById(R.id.switch_card_config_password);
    mPasswordEditText = (EditText) findViewById(R.id.edittext_card_config_password);
    mPrivacyTextView = (TextView) findViewById(R.id.textview_card_config_privacy_description);
    mPrivacySpinner = (Spinner) findViewById(R.id.spinner_card_config_privacy);
    mFab = (FloatingActionButton) findViewById(R.id.fab_editor_add);
    mFabCard = (CardView) findViewById(R.id.card_editor_fab);
    mConfigCard = (CardView) findViewById(R.id.card_editor_config);
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_editor);

    mAdapter = new EditorRecyclerAdapter(mPoll);
    recyclerView.setAdapter(mAdapter);

    mPasswordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPasswordEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mPoll.setPasswordProtected(isChecked);
      }
    });

    mPasswordEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mPoll.setPassword(s.toString());
      }
    });

    mPrivacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] privacyStrings = getResources().getStringArray(R.array
            .array_privacy_explained);

        mPrivacyTextView.setText(privacyStrings[position]);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    mFab.setOnClickListener(this);

    mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mAdapter.notifyDataSetChanged();
      }
    };

    setDefaultCardConfig();
  }

  @Override
  protected void onResume() {
    super.onResume();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
  }

  @Override
  protected void onPause() {
    super.onPause();
    getSharedPreferences(Constants.PREFS_POLLS, MODE_PRIVATE)
        .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    PersistenceUtils.storePollInPreferences(this, mPoll);
    outState.putSerializable(Constants.EXTRA_POLL_ID, mPoll.getUuid());
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.fab_editor_add:
        if (!mIsFabAnimating) {
          fabTransform();
        }
        break;
      case R.id.textview_card_binary:
        showBinaryDialog(v.getContext());
        if (!mIsFabAnimating) {
          reverseFabTransform();
        }
        break;
      case R.id.textview_card_multiple:
        if (!mIsFabAnimating) {
          reverseFabTransform();
        }
        break;
      case R.id.textview_card_rate:
        showRateDialog(v.getContext());
        if (!mIsFabAnimating) {
          reverseFabTransform();
        }
        break;
      case R.id.textview_card_close:
        if (!mIsFabAnimating) {
          reverseFabTransform();
        }
        break;
      case R.id.button_card_config_ok:
        reverseRevealSettingsCard();
        break;
      default:
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_editor, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_config:
        boolean isCardShown = mConfigCard.getVisibility() == View.VISIBLE;
        Drawable icon = item.getIcon();
        if (!mIsCardAnimating) {
          rotateIcon(icon, isCardShown);
          if (isCardShown) {
            reverseRevealSettingsCard();
          } else {
            revealSettingsCard();
          }
          mIsCardAnimating = true;
        }
        return true;
      case android.R.id.home:
        PersistenceUtils.deletePoll(this, mPoll.getUuid());
        finish();
        return true;
      case R.id.action_done:
        if (mPoll.getQuestionList().size() == 0) {
          PersistenceUtils.deletePoll(this, mPoll);
        } else {
          if (mPoll.getPollTitle() == null) {
            mPoll.setPollTitle("Untitled poll");
          }
          PersistenceUtils.storePollInPreferences(this, mPoll);
        }
        finish();
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void showBinaryDialog(Context context) {
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.editor_dialog_binary, null);

    final EditText title = (EditText) dialogView.findViewById(R.id.edittext_binary_title);
    final EditText option1 = (EditText) dialogView.findViewById(R.id.edittext_binary_option1);
    final EditText option2 = (EditText) dialogView.findViewById(R.id.edittext_binary_option2);
    final RadioGroup radiogroup = (RadioGroup) dialogView.findViewById(R.id.radiogroup_binary);

    radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        boolean custom = (checkedId == R.id.radiobutton_binary_custom);
        option1.setVisibility(custom ? View.VISIBLE : View.GONE);
        option2.setVisibility(custom ? View.VISIBLE : View.GONE);
      }
    });

    AlertDialog.Builder dialogBuilder = new Builder(context);
    dialogBuilder
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Question question;
            switch (radiogroup.getCheckedRadioButtonId()) {
              case R.id.radiobutton_binary_yesno:
                question = new Question(
                    title.getText().toString(),
                    Constants.BINARY_MODE_YESNO);
                break;
              case R.id.radiobutton_binary_truefalse:
                question = new Question(
                    title.getText().toString(),
                    Constants.BINARY_MODE_TRUEFALSE);
                break;
              default:
                question = new Question(
                    title.getText().toString(),
                    Constants.BINARY_MODE_CUSTOM);
                question.addOption(option1.getText().toString());
                question.addOption(option2.getText().toString());
            }
            mPoll.addQuestion(question);
            mAdapter.notifyDataSetChanged();
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .setView(dialogView)
        .setTitle("Binary Question")
        .setOnDismissListener(this);
    dialogBuilder.show();
  }

  private void showRateDialog(Context context) {
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.editor_dialog_rate, null);

    final EditText title = (EditText) dialogView.findViewById(R.id.edittext_rate_title);
    final EditText min = (EditText) dialogView.findViewById(R.id.edittext_rate_min);
    final EditText max = (EditText) dialogView.findViewById(R.id.edittext_rate_max);
    final RadioGroup radiogroup = (RadioGroup) dialogView.findViewById(R.id.radiogroup_rate);

    radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        min.setVisibility(checkedId == R.id.radiobutton_rate_custom ? View.VISIBLE : View.GONE);
        max.setVisibility(checkedId == R.id.radiobutton_rate_custom ? View.VISIBLE : View.GONE);
      }
    });

    AlertDialog.Builder dialogBuilder = new Builder(context);
    dialogBuilder
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Question rateQuestion;
            switch (radiogroup.getCheckedRadioButtonId()) {
              case R.id.radiobutton_rate_stars:
                rateQuestion = new Question(
                    title.getText().toString(),
                    Constants.RATE_MODE_STARS);
                break;
              case R.id.radiobutton_rate_likedislike:
                rateQuestion = new Question(
                    title.getText().toString(),
                    Constants.RATE_MODE_LIKEDISLIKE);
                break;
              case R.id.radiobutton_rate_score:
                rateQuestion = new Question(
                    title.getText().toString(),
                    Constants.RATE_MODE_SCORE);
                break;
              default:
                rateQuestion = new Question(
                    title.getText().toString(),
                    Constants.RATE_MODE_CUSTOM);
                rateQuestion.setRateCustomLowHigh(
                    Integer.valueOf(min.getText().toString()),
                    Integer.valueOf(max.getText().toString()));
                break;
            }
            mPoll.addQuestion(rateQuestion);
            mAdapter.notifyDataSetChanged();
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .setView(dialogView)
        .setTitle("Rate Question");
    dialogBuilder.show();

  }

  private void rotateIcon(Drawable icon, boolean reverse) {
    ObjectAnimator animator = ObjectAnimator.ofInt(
        icon,
        "level",
        reverse ? 0 : 10000,
        reverse ? 10000 : 0);
    animator.setInterpolator(AnimationUtils
        .loadInterpolator(getApplicationContext(), android.R.interpolator.fast_out_slow_in));
    animator.setDuration(getApplicationContext().getResources()
        .getInteger(android.R.integer.config_mediumAnimTime));
    animator.start();
  }

  private void fabTransform() {
    int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

    mIsFabAnimating = true;
    int fabCenterX = (mFab.getLeft() + mFab.getRight()) / 2;
    int fabCenterY = ((mFab.getTop() + mFab.getBottom()) / 2);
    int translateX = fabCenterX - (mFabCard.getLeft() + mFabCard.getWidth() / 2);
    int translateY = fabCenterY - (mFabCard.getTop() + mFabCard.getHeight() / 2);

    mFabCard.setTranslationX(translateX);
    mFabCard.setTranslationY(translateY);

    mFabCard.setVisibility(View.VISIBLE);

    // Reveal Card
    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mFabCard,
        mFabCard.getWidth() / 2,
        mFabCard.getHeight() / 2,
        mFab.getWidth() / 2,
        (float) Math.hypot(mFabCard.getWidth() / 2, mFabCard.getHeight() / 2))
        .setDuration(duration);

    // Move Card in curved Motion
    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(translateX, translateY, 0, 0);

    Animator move = ObjectAnimator
        .ofFloat(mFabCard, View.TRANSLATION_X, View.TRANSLATION_Y, motionPath)
        .setDuration(duration);

    // Fade Out Fab
    Animator fadeOut = ObjectAnimator.ofFloat(mFab, View.ALPHA, 0f)
        .setDuration(duration / 3);

    // Color Overlay
    ColorDrawable fabColor = new ColorDrawable(
        ContextCompat.getColor(this, R.color.colorAccentIntermediate));
    fabColor.setBounds(0, 0, mFabCard.getWidth(), mFabCard.getHeight());
    mFabCard.getOverlay().add(fabColor);

    // Icon Overlay
    Drawable fabIcon = ContextCompat.getDrawable(this, R.drawable.ic_add_24dp).mutate();
    fabIcon.setTint(ContextCompat.getColor(this, R.color.colorPrimaryText));
    int iconLeft = (mFabCard.getWidth() - fabIcon.getIntrinsicWidth()) / 2;
    int iconTop = (mFabCard.getHeight() - fabIcon.getIntrinsicHeight()) / 2;
    fabIcon.setBounds(iconLeft, iconTop,
        iconLeft + fabIcon.getIntrinsicWidth(),
        iconTop + fabIcon.getIntrinsicHeight());
    mFabCard.getOverlay().add(fabIcon);

    Animator fadeOutColor = ObjectAnimator.ofInt(fabColor, "alpha", 0).setDuration(duration);
    Animator fadeOutIcon = ObjectAnimator.ofInt(fabIcon, "alpha", 0).setDuration(duration);

    AnimatorSet show = new AnimatorSet();
    show.setInterpolator(AnimationUtils.loadInterpolator(
        mFab.getContext(),
        android.R.interpolator.fast_out_slow_in));

    show.playTogether(move, reveal, fadeOut, fadeOutColor, fadeOutIcon);
    show.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mFab.setVisibility(View.INVISIBLE);
        mIsFabAnimating = false;
        mFabCard.getOverlay().clear();
      }
    });

    show.start();
  }

  private void reverseFabTransform() {
    int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

    mIsFabAnimating = true;
    int cardCenterX = (mFabCard.getLeft() + mFabCard.getRight()) / 2;
    int cardCenterY = (mFabCard.getTop() + mFabCard.getBottom()) / 2;

    int translateX = cardCenterX - (mFab.getLeft() + mFab.getWidth() / 2);
    int translateY = cardCenterY - (mFab.getTop() + mFab.getHeight() / 2);

    mFab.setVisibility(View.VISIBLE);

    Animator inverseReveal = ViewAnimationUtils.createCircularReveal(
        mFabCard,
        mFabCard.getWidth() / 2,
        mFabCard.getHeight() / 2,
        (float) Math.hypot(mFabCard.getWidth() / 2, mFabCard.getHeight() / 2),
        mFab.getWidth() / 2)
        .setDuration(duration);

    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(0, 0, -translateX, -translateY);

    Animator move = ObjectAnimator.ofFloat(
        mFabCard,
        View.TRANSLATION_X,
        View.TRANSLATION_Y,
        motionPath)
        .setDuration(duration);

    // Color Overlay
    ColorDrawable fabColor = new ColorDrawable(
        ContextCompat.getColor(this, R.color.colorAccentIntermediate));
    fabColor.setBounds(0, 0, mFabCard.getWidth(), mFabCard.getHeight());
    fabColor.setAlpha(0);
    mFabCard.getOverlay().add(fabColor);

    // Icon Overlay
    Drawable fabIcon = ContextCompat.getDrawable(this, R.drawable.ic_add_24dp).mutate();
    fabIcon.setTint(ContextCompat.getColor(this, R.color.colorPrimaryText));
    int iconLeft = (mFabCard.getWidth() - fabIcon.getIntrinsicWidth()) / 2;
    int iconTop = (mFabCard.getHeight() - fabIcon.getIntrinsicHeight()) / 2;
    fabIcon.setBounds(iconLeft, iconTop,
        iconLeft + fabIcon.getIntrinsicWidth(),
        iconTop + fabIcon.getIntrinsicHeight());
    fabIcon.setAlpha(0);
    mFabCard.getOverlay().add(fabIcon);

    Animator fadeInColor = ObjectAnimator.ofInt(fabColor, "alpha", 255).setDuration(duration);
    Animator fadeInIcon = ObjectAnimator.ofInt(fabIcon, "alpha", 255).setDuration(duration);

    fadeInColor.setDuration(duration / 2);
    fadeInIcon.setDuration(duration / 2);

    fadeInColor.setStartDelay(duration / 2);
    fadeInIcon.setStartDelay(duration / 2);

    AnimatorSet animation = new AnimatorSet();
    animation.setInterpolator(AnimationUtils.loadInterpolator(
        mFabCard.getContext(),
        android.R.interpolator.fast_out_slow_in));
    animation.addListener(new AnimatorListenerAdapter() {


      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mFabCard.setVisibility(View.INVISIBLE);
        mFabCard.setAlpha(1f);
        mFab.setAlpha(1f);
        mFabCard.getOverlay().clear();
        mIsFabAnimating = false;
      }
    });

    animation.playTogether(move, inverseReveal, fadeInColor, fadeInIcon);

    animation.start();

  }

  private void revealSettingsCard() {
    View action = findViewById(R.id.action_config);
    int[] location = new int[2];
    action.getLocationOnScreen(location);

    int cx = (int) (mConfigCard.getX() + location[0] + action.getWidth() / 2);
    int cy = 0;

    float radius = (float) Math.hypot(cx, mConfigCard.getHeight());

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mConfigCard,
        cx,
        cy,
        0,
        radius);

    reveal.setInterpolator(
        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));
    reveal.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    reveal.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        mIsCardAnimating = false;
        super.onAnimationEnd(animation);
      }
    });
    reveal.start();
    mConfigCard.setVisibility(View.VISIBLE);
  }

  private void reverseRevealSettingsCard() {
    View action = findViewById(R.id.action_config);
    int[] location = new int[2];
    action.getLocationOnScreen(location);

    int cx = (int) (mConfigCard.getX() + location[0] + action.getWidth() / 2);
    int cy = 0;

    float radius = (float) Math.hypot(cx, mConfigCard.getHeight());

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mConfigCard,
        cx,
        cy,
        radius,
        0);

    reveal.setInterpolator(
        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));
    reveal.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    reveal.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        mConfigCard.setVisibility(View.INVISIBLE);
        mIsCardAnimating = false;
        super.onAnimationEnd(animation);
      }
    });
    reveal.start();
  }

  private void setDefaultCardConfig() {
    mPrivacySpinner.setSelection(1); // By default all votes will be anonymous
    mPrivacyTextView.setText(getResources().getStringArray(R.array.array_privacy_explained)[1]);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {

  }
}
