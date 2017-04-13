package com.arangurr.newsonar.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.ArcMotion;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

  private Switch mPasswordSwitch;
  private EditText mPasswordEditText;
  private TextView mPrivacyTextView;
  private Spinner mPrivacySpinner;
  private LinearLayout mSettingsHeader;
  private LinearLayout mSettingsContent;
  private CardView mCard;

  private Poll mPoll;
  private FloatingActionButton mFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);


    // If creating a new Poll.
    mPoll = new Poll();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mPasswordSwitch = (Switch) findViewById(R.id.switch_editor_password);
    mPasswordEditText = (EditText) findViewById(R.id.edittext_editor_password);
    mPrivacyTextView = (TextView) findViewById(R.id.textview_editor_privacy_explanation);
    mPrivacySpinner = (Spinner) findViewById(R.id.spinner_editor_privacy_selector);
    mSettingsHeader = (LinearLayout) findViewById(R.id.linearlayout_editor_header_settings);
    mSettingsContent = (LinearLayout) findViewById(R.id.linearlayout_editor_content_settings);
    mFab = (FloatingActionButton) findViewById(R.id.fab_editor_add);
    mCard = (CardView) findViewById(R.id.card_editor);

    mPasswordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPasswordEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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

    mSettingsHeader.setOnClickListener(this);
    mFab.setOnClickListener(this);

    setDefaults();
  }

  private void setDefaults() {
    mPrivacySpinner.setSelection(1); // By default all votes will be anonymous
    mPrivacyTextView.setText(getResources().getStringArray(R.array.array_privacy_explained)[1]);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.fab_editor_add:
        fabTransform();
        break;
      case R.id.linearlayout_editor_header_settings:
        boolean newState = !v.isActivated();
        v.setActivated(newState);
        mSettingsContent.setVisibility(newState ? View.GONE : View.VISIBLE);
        break;
      /*case R.id.button_editor_add:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] array = getResources().getStringArray(R.array
            .array_binaryquestion_modes);
        builder.setTitle("Binary Question").setItems(array, new DialogInterface
            .OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            BinaryQuestion q;
            switch (which) {
              case 0:
                q = new BinaryQuestion("Test Title",
                    Constants.BINARY_MODE_YESNO);
                break;
              case 1:
                q = new BinaryQuestion("Test Title",
                    Constants.BINARY_MODE_TRUEFALSE);
                break;
              case 2:
                q = new BinaryQuestion("Test Title",
                    Constants.BINARY_MODE_UPDOWNVOTE);
                break;
              case 3:
                q = new BinaryQuestion("Test Title",
                    Constants.BINARY_MODE_CUSTOM);
                // TODO: 05/04/2017 add custom options
                break;
            }
          }
        });
        builder.show();
        break;*/
      default:
    }
  }


  private void fabTransform() {
    int fabCenterX = (mFab.getLeft() + mFab.getRight()) / 2;
    int fabCenterY = ((mFab.getTop() + mFab.getBottom()) / 2);
    int translateX = fabCenterX - (mCard.getLeft() + mCard.getWidth() / 2);
    int translateY = fabCenterY - (mCard.getTop() + mCard.getHeight() / 2);

    mCard.setTranslationX(translateX);
    mCard.setTranslationY(translateY);

    mCard.setVisibility(View.VISIBLE);

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mCard,
        mCard.getWidth() / 2,
        mCard.getHeight() / 2,
        mFab.getWidth() / 2,
        (float) Math.hypot(mCard.getWidth() / 2, mCard.getHeight() / 2))
        .setDuration(360);

    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(translateX, translateY, 0, 0);

    Animator move = ObjectAnimator
        .ofFloat(mCard, View.TRANSLATION_X, View.TRANSLATION_Y, motionPath);

    Animator fadeOut = ObjectAnimator.ofFloat(mFab, View.ALPHA, 0f)
        .setDuration(60);

    Animator color = ObjectAnimator.ofArgb(mCard, "backgroundColor",
        ContextCompat.getColor(this, R.color.colorAccent),
        ContextCompat.getColor(this, android.R.color.white))
        .setDuration(360);

    AnimatorSet show = new AnimatorSet();
    show.setInterpolator(AnimationUtils.loadInterpolator(
        mFab.getContext(),
        android.R.interpolator.fast_out_slow_in));
    show.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mFab.setVisibility(View.INVISIBLE);
      }
    });

    show.playTogether(move, color, reveal, fadeOut);

    show.start();
  }

  private void reverseFabTransform() {
    int cardCenterX = (mCard.getLeft() + mCard.getRight()) / 2;
    int cardCenterY = (mCard.getTop() + mCard.getBottom()) / 2;

    int translateX = cardCenterX - (mFab.getLeft() + mFab.getWidth() / 2);
    int translateY = cardCenterY - (mFab.getTop() + mFab.getHeight() / 2);

    mFab.setVisibility(View.VISIBLE);

    Animator inverseReveal = ViewAnimationUtils.createCircularReveal(
        mCard,
        mCard.getWidth() / 2,
        mCard.getHeight() / 2,
        (float) Math.hypot(mCard.getWidth() / 2, mCard.getHeight() / 2),
        mFab.getWidth() / 2)
        .setDuration(360);

    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(0, 0, -translateX, -translateY);

    Animator move = ObjectAnimator.ofFloat(
        mCard,
        View.TRANSLATION_X,
        View.TRANSLATION_Y,
        motionPath);

    Animator fadeOut = ObjectAnimator.ofFloat(mCard, View.ALPHA, 0f);
    Animator fadeIn = ObjectAnimator.ofFloat(mFab, View.ALPHA, 1f);

    Animator color = ObjectAnimator.ofArgb(
        mCard,
        "backgroundColor",
        ContextCompat.getColor(this, android.R.color.white),
        ContextCompat.getColor(this, R.color.colorAccent))
        .setDuration(200);

    AnimatorSet animation = new AnimatorSet();
    animation.setInterpolator(AnimationUtils.loadInterpolator(
        mCard.getContext(),
        android.R.interpolator.fast_out_slow_in));
    animation.addListener(new AnimatorListenerAdapter() {


      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        mCard.setVisibility(View.INVISIBLE);
        mCard.setAlpha(1f);
      }
    });

    animation.playTogether(move, inverseReveal, color, fadeIn);

    animation.start();

  }
}
