package org.quelea.mobileremote.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.quelea.mobileremote.R;

class SimpleListViewHolder {
    TextView mTVItem;
    ImageView mImageView;

    SimpleListViewHolder(View base) {
        mTVItem = base.findViewById(R.id.detailRight);
        mImageView = base.findViewById(R.id.slideImage);
    }
}