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
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.data.Question;
import java.util.List;

/**
 * Created by Rodrigo on 14/04/2017.
 */

public class EditorRecyclerAdapter extends
    RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_TITLE = 1;
  private static final int TYPE_BINARY = 2;
  private static final int TYPE_MULTI = 3;
  private static final int TYPE_RATE = 4;

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
      case TYPE_BINARY:
        return new BinaryViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_card_binary, parent, false));
    }
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return TYPE_TITLE;
    } else {
      return TYPE_BINARY;
    }

  }


  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    switch (getItemViewType(position)) {
      case TYPE_TITLE:
        bindTitle((TitleHolder) holder);
        break;
      case TYPE_BINARY:
        bindBinary((BinaryViewHolder) holder, position);
        break;

    }

  }

  private void bindTitle(final TitleHolder holder) {
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

  private void bindBinary(BinaryViewHolder holder, int position) {
    holder.mCounter.setText(Integer.toString(position));
    holder.mQuestionTitle.setText(mItems.get(position - 1).getTitle());
    holder.mOption1.setText(mItems.get(position - 1).getOption(0).getOptionName());
    holder.mOption2.setText(mItems.get(position - 1).getOption(1).getOptionName());
  }

  @Override
  public int getItemCount() {
    return mItems.size() + 1; // Inlcude Title
  }


  public class BinaryViewHolder extends RecyclerView.ViewHolder {

    private TextView mQuestionTitle;
    private TextView mCounter;
    private TextView mOption1;
    private TextView mOption2;

    public BinaryViewHolder(View itemView) {
      super(itemView);
      mQuestionTitle = (TextView) itemView.findViewById(R.id.textview_editor_item_header_title);
      mCounter = (TextView) itemView.findViewById(R.id.textview_editor_item_header_counter);
      mOption1 = (TextView) itemView.findViewById(R.id.textview_editor_item_binary_option1);
      mOption2 = (TextView) itemView.findViewById(R.id.textview_editor_item_binary_option2);
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

