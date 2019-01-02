package org.quelea.mobileremote.adapters;

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
        viewHolder.slideText.setText(mList.get(position));
        viewHolder.slideText.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf"));
        if (mContext.isPresentation()) {
            String url = mContext.getSettings().getIp() + "/slides/slide" + (position + 1) + ".png";
            imageLoader.DisplayImage(url, viewHolder.slideImage);
            viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.slideImage.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, mContext.getResources().getDisplayMetrics());
            viewHolder.slideImage.requestLayout();
            viewHolder.slideImage.setVisibility(View.VISIBLE);
            viewHolder.slideText.bringToFront();
        } else {
            viewHolder.slideImage.setVisibility(View.INVISIBLE);
            viewHolder.slideImage.getLayoutParams().height = 80;
            viewHolder.slideImage.requestLayout();
        }
        if (position == mContext.getActiveVerse()) {
            v.setBackgroundColor(mContext.getResources().getColor(R.color.live_item));
            viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.slideImage.setAlpha(0.5f);
            if (mContext.isTextHidden()) {
                viewHolder.slideText.setTypeface(viewHolder.slideText.getTypeface(), Typeface.ITALIC);
                viewHolder.slideImage.setColorFilter(Color.rgb(60, 60, 60), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                viewHolder.slideText.setTypeface(viewHolder.slideText.getTypeface(), Typeface.NORMAL);
                viewHolder.slideImage.clearColorFilter();
            }
            viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
            viewHolder.slideText.bringToFront();

        } else {
            v.setBackgroundColor(UtilsMisc.getBackgroundColor(mContext));
            viewHolder.slideText.setVisibility(View.VISIBLE);
            viewHolder.slideImage.setAlpha(1f);
            viewHolder.slideImage.clearColorFilter();
            if (mContext.getSettings().getTheme().equals("1")) {
                viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_dark_theme));
            } else {
                viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
            }
        }
        if (mContext.getSlideTitles().get(position) != null) {
            viewHolder.slideTitle.setText(String.format("%s%s", mContext.getSlideTitles().get(position), mContext.getSlideTitles().get(position).isEmpty() ? "" : "\n"));
        } else {
            viewHolder.slideTitle.setText("");
        }
        return v;
    }
}




