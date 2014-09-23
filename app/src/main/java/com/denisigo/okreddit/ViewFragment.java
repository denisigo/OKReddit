package com.denisigo.okreddit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.SyncstateEntry;
import com.denisigo.okreddit.sync.OKRedditSyncAdapter;
import com.denisigo.okreddit.sync.Syncer;

public class ViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SUBREDDITS_LOADER = 0;
    private static final int SYNCSTATE_LOADER = 1;

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


    private static final String[] SYNC_COLUMNS = {
            SyncstateEntry.TABLE_NAME + "." + SyncstateEntry._ID,
            SyncstateEntry.COLUMN_TYPE,
            SyncstateEntry.COLUMN_STATE
    };
    public static final int COL_SYNCSTATE_ID = 0;
    public static final int COL_SYNCSTATE_TYPE = 1;
    public static final int COL_SYNCSTATE_STATE = 2;

    private Cursor mSubreddits;

    private ViewPager mPager;
    private ItemsAdapter mPagerAdapter;
    private int mPosition;

    public ViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mPagerAdapter = new ItemsAdapter(getActivity().getSupportFragmentManager());

        View rootView = inflater.inflate(R.layout.fragment_view, container, false);

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                mPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        if (savedInstanceState != null){
            mPosition = savedInstanceState.getInt("CURRENT_ITEM");
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("CURRENT_ITEM", mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SUBREDDITS_LOADER, null, this);
        getLoaderManager().initLoader(SYNCSTATE_LOADER, null, this);
        showProgress(true);
    }

    /**
     * Well, here we implement some trick to get cyclic page view.
     * I believe no body would try to load 9999 posts at a time =)
     */
    private class ItemsAdapter extends FragmentStatePagerAdapter{
        public ItemsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            int offset = position / mSubreddits.getCount();

            position = position % mSubreddits.getCount();

            mSubreddits.moveToPosition(position);

            String subredditId = mSubreddits.getString(COL_SUBREDDIT_SUBREDDIT_ID);
            String subredditDisplayName = mSubreddits.getString(COL_SUBREDDIT_DISPLAY_NAME);
            String subredditTitle = mSubreddits.getString(COL_SUBREDDIT_TITLE);

            Fragment fragment = new ItemFragment();
            Bundle args = new Bundle();
            args.putString(ItemFragment.ARG_SUBREDDIT_ID, subredditId);
            args.putString(ItemFragment.ARG_SUBREDDIT_DISPLAY_NAME, subredditDisplayName);
            args.putString(ItemFragment.ARG_SUBREDDIT_TITLE, subredditTitle);
            args.putInt(ItemFragment.ARG_OFFSET, offset);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            if (mSubreddits != null && mSubreddits.getCount() != 0)
                return 9999;
            return 0;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == SUBREDDITS_LOADER) {
            String sortOrder = SubredditEntry.COLUMN_DISPLAY_NAME + " ASC";

            Uri uri = SubredditEntry.CONTENT_URI;

            return new CursorLoader(
                    getActivity(),
                    uri,
                    SUBREDDIT_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        } else if (id == SYNCSTATE_LOADER){
            Uri uri = SyncstateEntry.CONTENT_URI;

            return new CursorLoader(
                    getActivity(),
                    uri,
                    SYNC_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == SUBREDDITS_LOADER) {
            mSubreddits = data;
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(mPosition);

            showProgress(false);

        } else if (loader.getId() == SYNCSTATE_LOADER){
            if (data.moveToFirst()) {
                int state = data.getInt(COL_SYNCSTATE_STATE);

                // If state is running, destroy loader to avoid its auto updates until
                // sync is finished totally
                if (state == Syncer.SYNC_STATE_RUNNING){
                    getLoaderManager().destroyLoader(SUBREDDITS_LOADER);
                    showProgress(true);
                } else if (state == Syncer.SYNC_STATE_STOPPED){
                    // This stuff is being invoked every time config changes too!
                    getLoaderManager().initLoader(SUBREDDITS_LOADER, null, this);
                    showProgress(false);
                    // TODO: somehow refresh pager after syncing
                    // This thing do the work, but after config changes app crashes
                    //mPager.setAdapter(mPagerAdapter);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == SUBREDDITS_LOADER) {
            mSubreddits = null;
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (Utils.isNetworkConnected(getActivity()))
                OKRedditSyncAdapter.syncImmediately(getActivity());
            else {
                Toast toast = Toast.makeText(getActivity(), R.string.offline_unable_sync, Toast.LENGTH_LONG);
                toast.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show progressbar overlay
     * @param state
     */
    private void showProgress(boolean state){
        FrameLayout loader = (FrameLayout)getView().findViewById(R.id.progress);
        loader.setVisibility(state?View.VISIBLE:View.GONE);
    }

}
