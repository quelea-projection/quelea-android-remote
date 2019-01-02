package org.quelea.mobileremote.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.utils.UtilsMisc;

import java.util.List;

public class ScheduleAdapter extends BaseAdapter {
    private Activity mContext;
    private List<String> mList;


    public ScheduleAdapter(Activity context, List<String> list) {
        mContext = context;
        mList = list;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        SimpleListViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert li != null;
            v = li.inflate(R.layout.item_schedule, parent, false);
            viewHolder = new SimpleListViewHolder(v);

            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (SimpleListViewHolder) convertView.getTag();
        }
        if (position == ((MainActivity) mContext).getActiveItem()) {
            v.setBackgroundColor(mContext.getResources().getColor(R.color.live_item));
            viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
        } else if (position == ((MainActivity) mContext).getPreviewItem()) {
            v.setBackgroundColor(mContext.getResources().getColor(R.color.preview_item));
            viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
        } else {
            v.setBackgroundColor(UtilsMisc.getBackgroundColor(mContext));
            if (((MainActivity) mContext).getSettings().getTheme().equals("1"))
                viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_dark_theme));
            else {
                viewHolder.slideText.setTextColor(mContext.getResources().getColor(R.color.text_default));
                parent.setBackgroundResource(R.color.background);
            }
        }

        viewHolder.slideText.setText(mList.get(position));
        if (mList.get(position).equals("")) {
            viewHolder.slideText.setVisibility(View.GONE);
        } else {
            viewHolder.slideText.setVisibility(View.VISIBLE);
        }
        viewHolder.slideText.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf"));
        return v;
    }
}
