package com.arangurr.newsonar.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.ArcMotion;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
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
  private CardView mFabCard;
  private CardView mConfigCard;

  private Poll mPoll;
  private FloatingActionButton mFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editor);
    setSupportActionBar(toolbar);

    // If creating a new Poll.
    mPoll = new Poll();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mPasswordSwitch = (Switch) findViewById(R.id.switch_card_config_password);
    mPasswordEditText = (EditText) findViewById(R.id.edittext_card_config_password);
    mPrivacyTextView = (TextView) findViewById(R.id.textview_card_config_privacy_description);
    mPrivacySpinner = (Spinner) findViewById(R.id.spinner_card_config_privacy);
    mFab = (FloatingActionButton) findViewById(R.id.fab_editor_add);
    mFabCard = (CardView) findViewById(R.id.card_editor_fab);
    mConfigCard = (CardView) findViewById(R.id.card_editor_config);

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

    mFab.setOnClickListener(this);

    setDefaultCardConfig();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.fab_editor_add:
        fabTransform();
        break;
      case R.id.textview_card_binary:
      case R.id.textview_card_multiple:
      case R.id.textview_card_rate:
      case R.id.textview_card_close:
        reverseFabTransform();
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
        rotateIcon(icon, isCardShown);
        if (isCardShown) {
          reverseRevealSettingsCard();
        } else {
          revealSettingsCard();
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
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
    int fabCenterX = (mFab.getLeft() + mFab.getRight()) / 2;
    int fabCenterY = ((mFab.getTop() + mFab.getBottom()) / 2);
    int translateX = fabCenterX - (mFabCard.getLeft() + mFabCard.getWidth() / 2);
    int translateY = fabCenterY - (mFabCard.getTop() + mFabCard.getHeight() / 2);

    mFabCard.setTranslationX(translateX);
    mFabCard.setTranslationY(translateY);

    mFabCard.setVisibility(View.VISIBLE);

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mFabCard,
        mFabCard.getWidth() / 2,
        mFabCard.getHeight() / 2,
        mFab.getWidth() / 2,
        (float) Math.hypot(mFabCard.getWidth() / 2, mFabCard.getHeight() / 2))
        .setDuration(360);

    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(translateX, translateY, 0, 0);

    Animator move = ObjectAnimator
        .ofFloat(mFabCard, View.TRANSLATION_X, View.TRANSLATION_Y, motionPath);

    Animator fadeOut = ObjectAnimator.ofFloat(mFab, View.ALPHA, 0f)
        .setDuration(60);

    Animator color = ObjectAnimator.ofArgb(mFabCard, "backgroundColor",
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
        .setDuration(360);

    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMaximumAngle(90f);
    arcMotion.setMinimumVerticalAngle(0);
    arcMotion.setMinimumHorizontalAngle(15f);
    Path motionPath = arcMotion.getPath(0, 0, -translateX, -translateY);

    Animator move = ObjectAnimator.ofFloat(
        mFabCard,
        View.TRANSLATION_X,
        View.TRANSLATION_Y,
        motionPath);

    Animator fadeOut = ObjectAnimator.ofFloat(mFabCard, View.ALPHA, 0f);
    Animator fadeIn = ObjectAnimator.ofFloat(mFab, View.ALPHA, 1f);

    Animator color = ObjectAnimator.ofArgb(
        mFabCard,
        "backgroundColor",
        ContextCompat.getColor(this, android.R.color.white),
        ContextCompat.getColor(this, R.color.colorAccent))
        .setDuration(200);

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
      }
    });

    animation.playTogether(move, inverseReveal, color, fadeIn);

    animation.start();

  }

  private void revealSettingsCard() {
    int cx = mConfigCard.getRight();
    int cy = 0;

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mConfigCard,
        cx,
        cy,
        0,
        (float) Math.hypot(mConfigCard.getWidth(), mConfigCard.getHeight()));

    reveal.setInterpolator(
        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));
    reveal.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    reveal.start();
    mConfigCard.setVisibility(View.VISIBLE);
  }

  private void reverseRevealSettingsCard() {
    int cx = mConfigCard.getRight();
    int cy = 0;

    Animator reveal = ViewAnimationUtils.createCircularReveal(
        mConfigCard,
        cx,
        cy,
        (float) Math.hypot(mConfigCard.getWidth(), mConfigCard.getHeight()),
        0);

    reveal.setInterpolator(
        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in));
    reveal.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    reveal.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        mConfigCard.setVisibility(View.INVISIBLE);
        super.onAnimationEnd(animation);
      }
    });
    reveal.start();
  }

  private void setDefaultCardConfig() {
    mPrivacySpinner.setSelection(1); // By default all votes will be anonymous
    mPrivacyTextView.setText(getResources().getStringArray(R.array.array_privacy_explained)[1]);
  }
}
