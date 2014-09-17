package com.denisigo.okreddit;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SubredditsAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        //public final ImageView iconView;
        public final TextView displayName;


        public ViewHolder(View view) {
            //iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            displayName = (TextView) view.findViewById(R.id.displayName);
        }
    }

    public SubredditsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).
                inflate(R.layout.list_item_subreddit, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String displayName = cursor.getString(MainFragment.COL_SUBREDDIT_DISPLAY_NAME);
        viewHolder.displayName.setText(displayName);
    }
}