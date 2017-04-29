package com.arangurr.newsonar.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import java.util.List;

/**
 * Created by Rodrigo on 22/04/2017.
 */

public class ListenRecyclerAdapter extends RecyclerView.Adapter<ListenRecyclerAdapter.ViewHolder> {

  private List<Poll> mFoundPolls;
  private OnItemClickListener mItemClickListener;

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

  public void setItemClickListener(OnItemClickListener listener) {
    mItemClickListener = listener;
  }

  @Override
  public ListenRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View inflatedView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_listen, parent, false);
    return new ViewHolder(inflatedView);
  }

  @Override
  public void onBindViewHolder(ListenRecyclerAdapter.ViewHolder holder, int position) {
    holder.mText1.setText(mFoundPolls.get(position).getPollTitle());
    holder.mText2.setText(mFoundPolls.get(position).getOwnerName());

    if (mFoundPolls.get(position).isPasswordProtected()) {
      holder.mLock.setVisibility(View.VISIBLE);
    } else {
      holder.mLock.setVisibility(View.GONE);
    }
  }

  @Override
  public int getItemCount() {
    return mFoundPolls.size();
  }

  public interface OnItemClickListener {

    void onItemClick(View view, Poll poll);
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final TextView mText1;
    private final TextView mText2;
    private final ImageView mLock;

    public ViewHolder(View itemView) {
      super(itemView);

      mText1 = (TextView) itemView.findViewById(R.id.textview_listen_text1);
      mText2 = (TextView) itemView.findViewById(R.id.textview_listen_text2);
      mLock = (ImageView) itemView.findViewById(R.id.button_listen_item_lock);

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      if (mItemClickListener != null) {
        mItemClickListener.onItemClick(v, mFoundPolls.get(getAdapterPosition()));
      }
    }
  }
}