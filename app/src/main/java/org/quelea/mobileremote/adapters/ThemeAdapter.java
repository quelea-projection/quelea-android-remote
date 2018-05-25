package org.quelea.mobileremote.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.quelea.mobileremote.cache.ImageLoader;
import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;

import java.util.List;

public class ThemeAdapter extends BaseAdapter {
    private Activity mContext;
    private List<String> mList;
    private ImageLoader imageLoader;


    public ThemeAdapter(Activity context, List<String> list) {
        mContext = context;
        mList = list;

        imageLoader = new ImageLoader(mContext.getApplicationContext());
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
        final ThemeViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert li != null;
            v = li.inflate(R.layout.item_theme_row, parent, false);
            viewHolder = new ThemeViewHolder();
            viewHolder.mTVItem = v.findViewById(R.id.themeName);
            viewHolder.mImageView = v.findViewById(R.id.themeImage);
            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (ThemeViewHolder) convertView.getTag();
        }
        viewHolder.mTVItem.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf"));
        viewHolder.mTVItem.setText(mList.get(position));
        imageLoader.DisplayImage(((MainActivity) mContext).getSettings().getIp() + "/themethumb" + position, viewHolder.mImageView);
        return v;
    }

    class ThemeViewHolder {
        TextView mTVItem;
        ImageView mImageView;
    }
}
