package com.arangurr.sonar.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.arangurr.sonar.R;

class EmptyHolder extends RecyclerView.ViewHolder {

  TextView text;

  EmptyHolder(View itemView) {
    super(itemView);
    text = (TextView) itemView.findViewById(R.id.textview_empty);
  }
}
