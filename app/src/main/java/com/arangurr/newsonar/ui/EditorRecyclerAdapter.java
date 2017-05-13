package com.arangurr.newsonar.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Option;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import java.util.List;

/**
 * Created by Rodrigo on 14/04/2017.
 */

public class EditorRecyclerAdapter extends
    RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_TITLE = 1;
  private static final int TYPE_QUESTION = 2;
  private Poll mPoll;

  private List<Question> mItems;

  public EditorRecyclerAdapter(Poll p) {
    mPoll = p;
    mItems = p.getQuestionList();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_TITLE:
        return new TitleHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_editor_title, parent, false));
      case TYPE_QUESTION:
        return new SimpleHolder(LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_2, parent, false));
    }
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_TITLE;
    } else {
      return TYPE_QUESTION;
    }

  }


  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    switch (getItemViewType(position)) {
      case TYPE_TITLE:
        bindTitle((TitleHolder) holder);
        break;
      case TYPE_QUESTION:
        bindQuestion((SimpleHolder) holder, position);
        break;

    }

  }

  private void bindTitle(final TitleHolder holder) {
    holder.mPollTitle.setHorizontallyScrolling(false);
    holder.mPollTitle.setMaxLines(3);
    if (mPoll.getPollTitle() != null) {
      holder.mPollTitle.setText(mPoll.getPollTitle());
    } else {
      holder.mPollTitle.requestFocus();
    }

    holder.mPollTitle.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mPoll.setPollTitle(s.toString());
      }
    });
  }

  private void bindQuestion(SimpleHolder holder, int position) {
    holder.mQuestionTitle.setText(
        String.format("%d. %s", position, mItems.get(position - 1).getTitle()));

    StringBuilder sb = new StringBuilder();
    sb.append("Options:");
    for (Option o : mItems.get(position - 1).getAllOptions()) {
      sb.append("\t\t");
      sb.append(o.getOptionName());
    }
    holder.mQuestionSubtitle.setText(sb.toString());
  }

  @Override
  public int getItemCount() {
    return mItems.size() + 1; // Inlcude Title
  }


  public class SimpleHolder extends RecyclerView.ViewHolder {

    private TextView mQuestionTitle;
    private TextView mQuestionSubtitle;

    public SimpleHolder(View itemView) {
      super(itemView);
      mQuestionTitle = (TextView) itemView.findViewById(android.R.id.text1);
      mQuestionSubtitle = (TextView) itemView.findViewById(android.R.id.text2);
    }

  }

  public class TitleHolder extends RecyclerView.ViewHolder {

    private EditText mPollTitle;

    public TitleHolder(View itemView) {
      super(itemView);
      mPollTitle = (EditText) itemView.findViewById(R.id.edittext_editor_title);
    }
  }
}

