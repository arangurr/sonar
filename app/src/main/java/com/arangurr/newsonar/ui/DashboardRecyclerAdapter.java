package com.arangurr.newsonar.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.arangurr.newsonar.R;
import com.arangurr.newsonar.data.Poll;
import com.arangurr.newsonar.ui.widget.OnItemClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Rodrigo on 08/04/2017.
 */

public class DashboardRecyclerAdapter extends
    RecyclerView.Adapter<DashboardRecyclerAdapter.ViewHolder> {

  private List<Poll> mPolls;
  private com.arangurr.newsonar.ui.widget.OnItemClickListener mItemClickListener;

  public DashboardRecyclerAdapter(List<Poll> polls) {
    Collections.sort(polls, new Comparator<Poll>() {
      @Override
      public int compare(Poll o1, Poll o2) {
        return (int) (o1.getStartDate() - o2.getStartDate());
      }
    });
    mPolls = polls;
  }

  public void swapArray(ArrayList<Poll> newArray) {
    mPolls.clear();
    mPolls.addAll(newArray);
    notifyDataSetChanged();
  }

  public void add(Poll poll) {
    mPolls.add(poll);
    notifyItemInserted(mPolls.size() - 1);
  }

  @Override
  public DashboardRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View inflatedView = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.item_dashboard, viewGroup, false);
    return new ViewHolder(inflatedView);
  }

  @Override
  public void onBindViewHolder(DashboardRecyclerAdapter.ViewHolder viewHolder, int i) {
    viewHolder.mTitle.setText(mPolls.get(i).getUuid().toString());
    Date date = new Date(mPolls.get(i).getStartDate());
    viewHolder.mSubtitle.setText(date.toString());
    viewHolder.mCircle.setText(String.valueOf(i));
  }

  @Override
  public int getItemCount() {
    return mPolls.size();
  }

  public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
    mItemClickListener = itemClickListener;
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mCircle;

    public ViewHolder(View itemView) {
      super(itemView);

      mTitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_title);
      mSubtitle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_subtitle);
      mCircle = (TextView) itemView.findViewById(R.id.textview_dashboard_item_circle);

      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
      if (mItemClickListener != null) {
        mItemClickListener.onItemClick(v, getAdapterPosition());
      }
    }
  }
}
