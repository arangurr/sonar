package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import com.arangurr.newsonar.data.Vote;
import java.util.List;

public class VotingActivity extends AppCompatActivity implements OnClickListener {

  private Poll mPoll;
  private Vote mVote;
  private List<Question> mQuestionList;
  private ViewPager mViewPager;
  private QuestionPagerAdapter mQuestionPagerAdapter;
  private FloatingActionButton mNextFab;
  private FloatingActionButton mPrevFab;
  private FloatingActionButton mSendFab;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_voting);

    Bundle extras = getIntent().getExtras();
    mPoll = GsonUtils
        .deserializeGson(extras.getString(Constants.EXTRA_SERIALIZED_POLL), Poll.class);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    mVote = PersistenceUtils.fetchVote(this);
    if (mVote == null) {
      mVote = new Vote(mPoll.getUuid(),
          prefs.getString(Constants.KEY_UUID, null),
          prefs.getString(Constants.KEY_USERNAME, null));
    }

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_voting);
    mViewPager = (ViewPager) findViewById(R.id.viewpager_voting);
    mNextFab = (FloatingActionButton) findViewById(R.id.button_voting_next);
    mPrevFab = (FloatingActionButton) findViewById(R.id.button_voting_previous);
    mSendFab = (FloatingActionButton) findViewById(R.id.button_voting_send);

    mQuestionList = mPoll.getQuestionList();
    mQuestionPagerAdapter = new QuestionPagerAdapter();

    mViewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        if (position != mQuestionList.size() - 1) {
          if (mNextFab.getVisibility() == View.GONE || mPrevFab.getVisibility() == View.INVISIBLE) {
            mNextFab.show();
          }
        } else {
          mNextFab.hide();
        }
        if (position != 0) {
          if (mPrevFab.getVisibility() == View.GONE || mPrevFab.getVisibility() == View.INVISIBLE) {
            mPrevFab.show();
          }
        } else {
          mPrevFab.hide();
        }
      }
    });
    mViewPager.setOffscreenPageLimit(mPoll.getQuestionList().size());

    mNextFab.setOnClickListener(this);
    mPrevFab.setOnClickListener(this);
    mSendFab.setOnClickListener(this);

    setSupportActionBar(toolbar);
    Drawable cancelDrawable = getDrawable(R.drawable.ic_clear_24dp);
    cancelDrawable.setTint(ContextCompat.getColor(this, R.color.colorPrimaryTextDark));
    getSupportActionBar().setHomeAsUpIndicator(cancelDrawable);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(mPoll.getPollTitle());

    mViewPager.setAdapter(mQuestionPagerAdapter);
    int pagerMarginInPixels = getResources().getDimensionPixelSize(R.dimen.pager_margin);
    mViewPager.setPageMargin(pagerMarginInPixels);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button_voting_send:
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.EXTRA_SERIALIZED_VOTE, GsonUtils.serialize(mVote));
        setResult(RESULT_OK, resultIntent);
        finish();
        break;
      case R.id.button_voting_next:
        moveNext();
        break;
      case R.id.button_voting_previous:
        movePrevious();
        break;
    }
  }

  public void moveNext() {
    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
  }

  public void movePrevious() {
    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
  }

  private class QuestionPagerAdapter extends PagerAdapter {

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      View view;
      switch (mQuestionList.get(position).getQuestionMode()) {
        case Constants.BINARY_MODE_CUSTOM:
        case Constants.BINARY_MODE_TRUEFALSE:
        case Constants.BINARY_MODE_YESNO:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_binary, container, false);
          container.addView(view);
          bindBinaryQuestion(mQuestionList.get(position), view, position);
          break;
        case Constants.RATE_MODE_LIKEDISLIKE:
        case Constants.RATE_MODE_SCORE:
        case Constants.RATE_MODE_STARS:
        case Constants.RATE_MODE_CUSTOM:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_rate, container, false);
          container.addView(view);
          bindRateQuestion(mQuestionList.get(position), view, position);
          break;
        default:
          view = LayoutInflater.from(container.getContext())
              .inflate(android.R.layout.simple_list_item_1, container, false);
          ((TextView) view.findViewById(android.R.id.text1))
              .setText(mQuestionList.get(position).toString());
          container.addView(view);
      }

      return view;

    }

    private void bindBinaryQuestion(final Question question, View view, final int position) {
      TextView header = (TextView) view.findViewById(R.id.textview_card_binary_vote_title);
      TextView content = (TextView) view.findViewById(R.id.textview_card_binary_vote_content);
      RadioButton rb1 = (RadioButton) view.findViewById(R.id.radiobutton1_card_binary_vote);
      RadioButton rb2 = (RadioButton) view.findViewById(R.id.radiobutton2_card_binary_vote);
      RadioGroup rg = (RadioGroup) view.findViewById(R.id.radiogroup_card_binary_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());
      rb1.setText(question.getOption(0).getOptionName());
      rb2.setText(question.getOption(1).getOptionName());

      rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
          switch (i) {
            case R.id.radiobutton1_card_binary_vote:
              mVote.attachResponse(mQuestionList.get(position), question.getOption(0));
              PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
              break;
            case R.id.radiobutton2_card_binary_vote:
              mVote.attachResponse(mQuestionList.get(position), question.getOption(1));
              PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
              break;
          }
          if (mVote.getSelectionList().size() == mQuestionList.size()) {
            mSendFab.show();
          } else {
            mViewPager.postDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                       moveNext();
                                     }
                                   },
                getResources().getInteger(android.R.integer.config_longAnimTime));
          }
        }
      });
    }

    private void bindRateQuestion(final Question question, View view, final int position) {
      TextView header = (TextView) view.findViewById(R.id.textview_card_rate_vote_title);
      TextView content = (TextView) view.findViewById(R.id.textview_card_rate_vote_content);
      TextView maxTv = (TextView) view.findViewById(R.id.textview_card_rate_vote_max);
      TextView minTv = (TextView) view.findViewById(R.id.textview_card_rate_vote_min);
      SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar_card_rate_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      int range = question.getAllOptions().size() -1;
      int min = Integer.parseInt(question.getOption(0).getOptionName());
      int max = min + range;

      maxTv.setText(String.valueOf(max));
      minTv.setText(String.valueOf(min));
      seekBar.setMax(range);

      seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
      });
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
    }

    @Override
    public int getCount() {
      return mQuestionList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }
  }
}
