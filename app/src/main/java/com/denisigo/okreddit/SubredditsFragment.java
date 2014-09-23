package com.denisigo.okreddit;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.sync.OKRedditSyncAdapter;

import java.util.ArrayList;
import java.util.List;

import redditapi.ItemWrapper;
import redditapi.Listing;
import redditapi.Subreddit;
import retrofit.RetrofitError;

public class SubredditsFragment extends Fragment {

    private static final String[] SUBREDDIT_COLUMNS = {
            SubredditEntry.TABLE_NAME + "." + SubredditEntry._ID,
            SubredditEntry.COLUMN_SUBREDDIT_ID,
            SubredditEntry.COLUMN_DISPLAY_NAME,
            SubredditEntry.COLUMN_TITLE
    };

    public static final int COL_SUBREDDIT_ID = 0;
    public static final int COL_SUBREDDIT_SUBREDDIT_ID = 1;
    public static final int COL_SUBREDDIT_DISPLAY_NAME = 2;
    public static final int COL_SUBREDDIT_TITLE = 3;

    private static final int MORE_SUBREDDITS_LIMIT = 25;
    private String mMoreSubredditsAfter;
    private boolean mMoreSubredditsStop = false;

    private boolean mSubredditsLoading = false;
    // Special subreddit that is being inserted at the bottom
    // of the list and is interpreted as a loader item by the adapter
    // (this is the most optimal approach I've came up with =))
    private Subreddit mSubredditsLoader;

    private List<Subreddit> mSubreddits;
    private List<String> mSubscribedSubredditsIds;

    private ListView mLvSubreddits;
    private SubredditsAdapter mSubredditsAdapter;
    private int mPosition;

    private Reddit mReddit;

    private boolean mIsOnline;
    // If user subscribed/unsubscribed for any subreddit
    private boolean mWasSomethingChanged = false;

    public SubredditsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_subreddits, container, false);

        mLvSubreddits = (ListView) rootView.findViewById(R.id.subreddits);
        mLvSubreddits.setAdapter(mSubredditsAdapter);

        mLvSubreddits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        // Set onScroll listener to be able to load more subreddits when
        // the user reaches the bottom of the list
        mLvSubreddits.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView lw, final int firstVisibleItem,
                                 final int visibleItemCount, final int totalItemCount) {
                if (totalItemCount > 0
                        && (firstVisibleItem + visibleItemCount) == totalItemCount) {
                    if (mIsOnline)
                        loadMoreSubreddits();
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsOnline = Utils.isNetworkConnected(getActivity());
        if (!mIsOnline){
            Toast toast = Toast.makeText(getActivity(), R.string.offline_unable_manage_subreddits, Toast.LENGTH_LONG);
            toast.show();
        }

        // TODO: maybe it would be better to use db+loader to cache loaded subreddits,
        // but at the time being, we will just retain instance
        setRetainInstance(true);

        // TODO: Sync subreddits if logged in?

        mSubredditsAdapter = new SubredditsAdapter();

        mSubreddits = new ArrayList<Subreddit>(0);
        mSubscribedSubredditsIds = new ArrayList<String>(0);

        mSubredditsLoader = new Subreddit();
        mSubredditsLoader.id = "[LOADER]";

        (new MySubredditsLoaderTask()).execute();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReddit = new Reddit(getActivity());
    }

    public class SubredditsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public SubredditsAdapter() {
            mInflater = getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mSubreddits.size();
        }

        @Override
        public Subreddit getItem(int i) {
            return mSubreddits.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {

            Subreddit subreddit = getItem(i);

            if (subreddit == mSubredditsLoader)
                return mInflater.inflate(R.layout.list_item_loader,
                        parent, false);

            if (convertView == null || convertView.getId() == R.id.loaderItem)
                convertView = mInflater.inflate(R.layout.list_item_subreddit,
                        parent, false);

            ViewHolder viewHolder = (ViewHolder)convertView.getTag();
            if (viewHolder == null)
                viewHolder = new ViewHolder(convertView);

            viewHolder.displayName.setText(subreddit.getDisplayName());
            viewHolder.title.setText(subreddit.getTitle());
            viewHolder.isSubscribed.setChecked(subreddit.isUserSubscriber());
            viewHolder.isSubscribed.setTag(i);

            // If we're online, allow to subscribe/unsubscribe. Otherwise disable checkbox
            if (mIsOnline) {
                viewHolder.isSubscribed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean checked = ((CheckBox)view).isChecked();
                        Subreddit subreddit = getItem((Integer) view.getTag());
                        subreddit.user_is_subscriber = checked;
                        (new SubscribeSubredditTask()).execute(subreddit.getId(),
                                checked ? Reddit.SUBSCRIBE_SUBSCRIBE : Reddit.SUBSCRIBE_UNSUBSCRIBE);
                        mWasSomethingChanged = true;
                    }
                });
            } else
                viewHolder.isSubscribed.setEnabled(false);

            return convertView;
        }

        /**
         * Cache of the children views for a forecast list item.
         */
        public class ViewHolder {
            public final TextView displayName;
            public final TextView title;
            public final CheckBox isSubscribed;

            public ViewHolder(View view) {
                displayName = (TextView) view.findViewById(R.id.displayName);
                title = (TextView) view.findViewById(R.id.title);
                isSubscribed = (CheckBox) view.findViewById(R.id.isSubscribed);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Start syncing if any changes were made here
        if (mWasSomethingChanged)
            OKRedditSyncAdapter.syncImmediately(getActivity());
    }

    /**
     * Launches new task for loading more subreddits if possible.
     */
    private void loadMoreSubreddits(){
        if (mMoreSubredditsStop)
            return;

        if (mSubredditsLoading)
            return;

        (new MoreSubredditsLoaderTask()).execute();
    }


    /**
     * Loads my subreddits.
     */
    private class MySubredditsLoaderTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            mSubredditsLoading = true;
            showSubredditsLoader();
        }

        @Override
        protected Cursor doInBackground(Void... args) {

            String sortOrder = SubredditEntry.COLUMN_DISPLAY_NAME + " ASC";

            return getActivity().getContentResolver().
                    query(SubredditEntry.CONTENT_URI, SUBREDDIT_COLUMNS, null, null, sortOrder);
        }

        @Override
        protected void onPostExecute(Cursor subreddits) {
            try {
                mSubredditsLoading = false;
                hideSubredditsLoader();

                mSubreddits.clear();
                mSubscribedSubredditsIds.clear();

                while (subreddits.moveToNext()) {

                    Subreddit subreddit = new Subreddit();
                    subreddit.display_name = subreddits.getString(COL_SUBREDDIT_DISPLAY_NAME);
                    subreddit.id = subreddits.getString(COL_SUBREDDIT_SUBREDDIT_ID);
                    subreddit.title = subreddits.getString(COL_SUBREDDIT_TITLE);
                    subreddit.user_is_subscriber = true;

                    mSubreddits.add(subreddit);

                    // Save id to be able to quickly determine whether we are subscribed for this post or not
                    mSubscribedSubredditsIds.add(subreddit.id);
                }

                mSubredditsAdapter.notifyDataSetChanged();

                // Start loading more subreddits from reddit if we're online
                if (mIsOnline)
                    loadMoreSubreddits();
            } finally {
                subreddits.close();
            }
        }
    }


    /**
     * Loads other subreddits from reddit.
     */
    private class MoreSubredditsLoaderTask extends AsyncTask<Void, Void, Listing<ItemWrapper<Subreddit>>> {

        @Override
        protected void onPreExecute() {
            mSubredditsLoading = true;
            showSubredditsLoader();
        }

        @Override
        protected Listing<ItemWrapper<Subreddit>> doInBackground(Void... args) {

            Listing<ItemWrapper<Subreddit>> subreddits = null;
            try {
                return mReddit.getService().popularSubreddits(mMoreSubredditsAfter, Integer.toString(MORE_SUBREDDITS_LIMIT));
            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Add all retrieved subreddits (except we are subscribed already) to our listview
         * @param subreddits
         */
        @Override
        protected void onPostExecute(Listing<ItemWrapper<Subreddit>> subreddits) {
            mSubredditsLoading = false;
            hideSubredditsLoader();

            if (subreddits == null)
                return;

            if (subreddits.data.after != null)
                mMoreSubredditsAfter = subreddits.data.after;

            if (subreddits.data.children != null) {
                for (ItemWrapper<Subreddit> subredditWrapper : subreddits.data.children) {
                    Subreddit subreddit = subredditWrapper.data;
                    // Add only if we don't have it already in subscribed
                    if(!mSubscribedSubredditsIds.contains(subreddit.id))
                        mSubreddits.add(subreddit);
                }
                mSubredditsAdapter.notifyDataSetChanged();
            } else {
                mMoreSubredditsStop = true;
            }
        }
    }

    /**
     * Add special loader subreddit at the bottom of the list
     */
    private void showSubredditsLoader() {
        mSubreddits.add(mSubredditsLoader);
        mSubredditsAdapter.notifyDataSetChanged();
    }

    /**
     * Remove loader
     */
    private void hideSubredditsLoader() {
        mSubreddits.remove(mSubredditsLoader);
        mSubredditsAdapter.notifyDataSetChanged();
    }


    /**
     * Subscribe/unsubsribe subreddit
     */
    private class SubscribeSubredditTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... args) {
            try {
                return mReddit.subscribe(args[0], args[1]);
            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
