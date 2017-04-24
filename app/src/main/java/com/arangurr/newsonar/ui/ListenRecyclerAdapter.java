package com.arangurr.newsonar.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.arangurr.newsonar.Constants;
import com.arangurr.newsonar.GsonUtils;
import com.arangurr.newsonar.data.Poll;
import java.util.List;

/**
 * Created by Rodrigo on 22/04/2017.
 */

public class ListenRecyclerAdapter extends RecyclerView.Adapter<ListenRecyclerAdapter.ViewHolder> {

  private List<Poll> mFoundPolls;

  public ListenRecyclerAdapter(List<Poll> foundPolls) {
    mFoundPolls = foundPolls;
  }

  public void add(Poll p) {
    mFoundPolls.add(p);
    notifyItemInserted(mFoundPolls.size() - 1);
  }

  public void remove(Poll p) {
    int index = mFoundPolls.indexOf(p);
    if (index < 0) {
      for (Poll pollInList : mFoundPolls) {
        if (pollInList.getUuid().equals(p.getUuid())) {
          index = mFoundPolls.indexOf(pollInList);
          break;
        }
      }
      mFoundPolls.remove(index);
      notifyItemRemoved(index);
    }
  }

  public void clear() {
    mFoundPolls.clear();
    notifyDataSetChanged();
  }

  @Override
  public ListenRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View inflatedView = LayoutInflater.from(parent.getContext())
        .inflate(android.R.layout.simple_list_item_2, parent, false);
    return new ViewHolder(inflatedView);
  }

  @Override
  public void onBindViewHolder(ListenRecyclerAdapter.ViewHolder holder, int position) {
    holder.mText1.setText(mFoundPolls.get(position).getPollTitle());
    holder.mText2.setText(mFoundPolls.get(position).getOwnerName());
  }

  @Override
  public int getItemCount() {
    return mFoundPolls.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final TextView mText1;
    private final TextView mText2;

    public ViewHolder(View itemView) {
      super(itemView);

      mText1 = (TextView) itemView.findViewById(android.R.id.text1);
      mText2 = (TextView) itemView.findViewById(android.R.id.text2);

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      String serialized = GsonUtils.serialize(mFoundPolls.get(getAdapterPosition()));

      Intent voteIntent = new Intent(v.getContext(), VotingActivity.class);
      voteIntent.putExtra(Constants.EXTRA_SERIALIZED_POLL, serialized);
      ((Activity) v.getContext()).startActivityForResult(voteIntent, Constants.VOTE_REQUEST);
    }
  }
}