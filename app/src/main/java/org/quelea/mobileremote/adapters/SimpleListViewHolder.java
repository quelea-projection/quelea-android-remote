package org.quelea.mobileremote.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.quelea.mobileremote.R;

class SimpleListViewHolder {
    TextView slideText;
    TextView slideTitle;
    ImageView slideImage;

    SimpleListViewHolder(View base) {
        slideText = base.findViewById(R.id.detailRight);
        slideTitle = base.findViewById(R.id.slide_title);
        slideImage = base.findViewById(R.id.slideImage);
    }
}