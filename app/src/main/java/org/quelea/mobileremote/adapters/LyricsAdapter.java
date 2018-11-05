package org.quelea.mobileremote.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.cache.ImageLoader;
import org.quelea.mobileremote.utils.UtilsMisc;

import java.util.List;

public class LyricsAdapter extends BaseAdapter {
    private MainActivity mContext;
    private List<String> mList;
    public ImageLoader imageLoader;

    public LyricsAdapter(MainActivity context, List<String> list) {
        mContext = context;
        mList = list;

        // Create ImageLoader object to download and show image in list
        // Call ImageLoader constructor to initialize FileCache
        imageLoader = new ImageLoader(context);

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v;
        final SimpleListViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert li != null;
            v = li.inflate(R.layout.item_lyrics_section, parent, false);
            viewHolder = new SimpleListViewHolder(v);

            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (SimpleListViewHolder) convertView.getTag();
        }
        viewHolder.mTVItem.setText(mList.get(position));
        viewHolder.mTVItem.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf"));
        if (mContext.isPresentation()) {
            String url = mContext.getSettings().getIp() + "/slides/slide" + (position + 1) + ".png";
            imageLoader.DisplayImage(url, viewHolder.mImageView);
            viewHolder.mTVItem.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.mImageView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, mContext.getResources().getDisplayMetrics());
            viewHolder.mImageView.requestLayout();
            viewHolder.mImageView.setVisibility(View.VISIBLE);
            viewHolder.mTVItem.bringToFront();
        } else {
            viewHolder.mImageView.setVisibility(View.INVISIBLE);
            viewHolder.mImageView.getLayoutParams().height = 80;
            viewHolder.mImageView.requestLayout();
        }
        if (position == mContext.getActiveVerse()) {
            v.setBackgroundColor(mContext.getResources().getColor(R.color.live_item));
            viewHolder.mTVItem.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.mImageView.setAlpha(0.5f);
            if (mContext.isTextHidden()) {
                viewHolder.mTVItem.setTypeface(viewHolder.mTVItem.getTypeface(), Typeface.ITALIC);
                viewHolder.mImageView.setColorFilter(Color.rgb(60, 60, 60), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                viewHolder.mTVItem.setTypeface(viewHolder.mTVItem.getTypeface(), Typeface.NORMAL);
                viewHolder.mImageView.clearColorFilter();
            }
            viewHolder.mTVItem.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.mTVItem.bringToFront();

        } else {
            v.setBackgroundColor(UtilsMisc.getBackgroundColor(mContext));
            viewHolder.mTVItem.setVisibility(View.VISIBLE);
            viewHolder.mImageView.setAlpha(1f);
            viewHolder.mImageView.clearColorFilter();
            if (mContext.getSettings().getTheme().equals("1")) {
                viewHolder.mTVItem.setTextColor(mContext.getResources().getColor(R.color.text_dark_theme));
            } else {
                viewHolder.mTVItem.setTextColor(mContext.getResources().getColor(R.color.text_default));
            }
        }
        return v;
    }

}




