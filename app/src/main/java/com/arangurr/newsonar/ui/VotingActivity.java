package com.arangurr.newsonar.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.PersistenceUtils;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Option;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import com.arangurr.newsonar.data.Vote;
import java.util.ArrayList;
import java.util.List;

public class VotingActivity extends AppCompatActivity implements OnClickListener {

  private Poll mPoll;
  private Vote mVote;
  private List<Question> mQuestionList;
  private ViewPager mViewPager;
  private QuestionPagerAdapter mQuestionPagerAdapter;
  private ImageButton mNextButton;
  private ImageButton mPreviousButton;
  private Button mSendButton;


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
    mNextButton = (ImageButton) findViewById(R.id.button_voting_next);
    mPreviousButton = (ImageButton) findViewById(R.id.button_voting_previous);
    mSendButton = (Button) findViewById(R.id.button_voting_send);

    mQuestionList = mPoll.getQuestionList();
    mQuestionPagerAdapter = new QuestionPagerAdapter();

    mViewPager.setOffscreenPageLimit(mPoll.getQuestionList().size());

    mNextButton.setOnClickListener(this);
    mPreviousButton.setOnClickListener(this);
    mSendButton.setOnClickListener(this);

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
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_likedislike, container, false);
          container.addView(view);
          bindLikeQuestion(mQuestionList.get(position), view, position);
          break;
        case Constants.RATE_MODE_STARS:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_rate_stars, container, false);
          container.addView(view);
          bindStarsQuestion(mQuestionList.get(position), view, position);
          break;
        case Constants.RATE_MODE_SCORE:
        case Constants.RATE_MODE_CUSTOM:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_rate, container, false);
          container.addView(view);
          bindRateQuestion(mQuestionList.get(position), view, position);
          break;
        case Constants.MULTI_MODE_EXCLUSIVE:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_multi_exclusive, container, false);
          container.addView(view);
          bindMultiExclusiveQuestion(mQuestionList.get(position), view, position);
          break;
        case Constants.MULTI_MODE_MULTIPLE:
          view = LayoutInflater.from(container.getContext())
              .inflate(R.layout.card_vote_multi_multiple, container, false);
          container.addView(view);
          bindMultiMultipleQuestion(mQuestionList.get(position), view, position);
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
      TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
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
          mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
        }
      });
    }

    private void bindRateQuestion(final Question question, View view, final int position) {
      final TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      final TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
      final TextView maxTv = (TextView) view.findViewById(R.id.textview_card_rate_vote_max);
      final TextView minTv = (TextView) view.findViewById(R.id.textview_card_rate_vote_min);
      final TextView selected = (TextView) view.findViewById(R.id.textview_card_rate_vote_selected);
      final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar_card_rate_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      final int range = question.getAllOptions().size() - 1;
      final int min = Integer.parseInt(question.getOption(0).getOptionName());
      final int max = min + range;

      maxTv.setText(String.valueOf(max));
      minTv.setText(String.valueOf(min));
      seekBar.setMax(range);

      seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (fromUser) {
            selected.setText(String.valueOf(progress + min));
          }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          mVote.attachResponse(question, question.getOption(seekBar.getProgress()));
          PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
          mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
        }
      });
    }

    private void bindStarsQuestion(final Question question, View view, int position) {
      final TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      final TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
      final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingbar_card_rate_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
          if (fromUser) {
            mVote.attachResponse(question, question.getOption((int) (rating - 1)));
            PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
            mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
          }
        }
      });
    }

    private void bindLikeQuestion(final Question question, View view, final int position) {
      final TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      final TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
      final ImageButton like = (ImageButton) view.findViewById(R.id.button_card_vote_like);
      final ImageButton dislike = (ImageButton) view.findViewById(R.id.button_card_vote_dislike);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      dislike.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (dislike.isSelected()) {
            dislike.setSelected(false);
          } else {
            if (like.isSelected()) {
              like.setSelected(false);
            }
            dislike.setSelected(true);
            mVote.attachResponse(question, question.getOption(0));
            PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
            mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
          }
        }
      });
      like.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (like.isSelected()) {
            like.setSelected(false);
          } else {
            if (dislike.isSelected()) {
              dislike.setSelected(false);
            }
            like.setSelected(true);
            mVote.attachResponse(question, question.getOption(1));
            PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
            mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
          }
        }
      });

    }

    private void bindMultiExclusiveQuestion(final Question question, View view, int position) {
      final TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      final TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
      final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radiogroup_card_multi_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      final ArrayList<Option> options = question.getAllOptions();

      for (int i = 0; i < options.size(); i++) {
        RadioButton radioButton = (RadioButton) LayoutInflater.from(view.getContext())
            .inflate(R.layout.card_vote_multi_radiobutton, radioGroup, false);
        radioButton.setText(options.get(i).getOptionName());
        radioButton.setId(options.get(i).getKey());
        radioGroup.addView(radioButton);
      }

      radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
          mVote.attachResponse(question, options.get(checkedId));
          PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
          mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
        }
      });

    }

    private void bindMultiMultipleQuestion(final Question question, View view, int position) {
      final TextView header = (TextView) view.findViewById(R.id.textview_card_vote_title);
      final TextView content = (TextView) view.findViewById(R.id.textview_card_vote_content);
      final LinearLayout container = (LinearLayout) view
          .findViewById(R.id.linearlayout_card_multi_multi_vote);

      header.setText(String.format(
          getString(R.string.voting_card_title),
          position + 1,
          mQuestionList.size()));
      content.setText(question.getTitle());

      final ArrayList<Option> options = question.getAllOptions();

      CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          List<Option> selected = new ArrayList<>();

          for (int i = 0; i < options.size(); i++) {
            if (((CheckBox) container.getChildAt(i)).isChecked()) {
              selected.add(options.get(i));
            }
          }
          if (selected.isEmpty()) {
            mVote.removeResponse(question);
          } else {
            Option[] optionArray = new Option[selected.size()];
            optionArray = selected.toArray(optionArray);

            mVote.attachResponse(question, optionArray);
          }
          PersistenceUtils.storeVoteInPreferences(getApplicationContext(), mVote);
          mSendButton.setEnabled(mVote.getSelectionList().size() == mQuestionList.size());
        }
      };

      for (int i = 0; i < options.size(); i++) {
        CheckBox checkBox = (CheckBox) LayoutInflater.from(view.getContext())
            .inflate(R.layout.card_vote_multi_checkbox, container, false);
        checkBox.setText(options.get(i).getOptionName());
        checkBox.setId(options.get(i).getKey());
        checkBox.setOnCheckedChangeListener(listener);
        container.addView(checkBox);
      }
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
