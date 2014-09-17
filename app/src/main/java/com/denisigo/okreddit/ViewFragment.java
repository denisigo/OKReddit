package com.denisigo.okreddit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;

import java.util.List;

public class ViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SUBREDDITS_LOADER = 0;

    private static final String[] SUBREDDIT_COLUMNS = {
            SubredditEntry.TABLE_NAME + "." + SubredditEntry._ID,
            SubredditEntry.COLUMN_SUBREDDIT_ID,
            SubredditEntry.COLUMN_DISPLAY_NAME
    };

    public static final int COL_SUBREDDIT_ID = 0;
    public static final int COL_SUBREDDIT_SUBREDDIT_ID = 1;
    public static final int COL_SUBREDDIT_DISPLAY_NAME = 2;


    private ListView mListView;

    private Cursor mSubreddits;

    private ViewPager mPager;
    private ItemsAdapter mPagerAdapter;
    private int mPosition;

    public ViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("aaa", "onCreateView");

        mPagerAdapter = new ItemsAdapter(getActivity().getSupportFragmentManager());

        View rootView = inflater.inflate(R.layout.fragment_view, container, false);

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                mPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        if (savedInstanceState != null){
            mPosition = savedInstanceState.getInt("CURRENT_ITEM");
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("aaa", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("aaa", "onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("CURRENT_ITEM", mPosition);
        super.onSaveInstanceState(outState);
        Log.i("aaa", "onSaveInstanceState");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("aaa", "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SUBREDDITS_LOADER, null, this);
        Log.i("aaa", "onActivityCreated");
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

            Fragment fragment = new ItemFragment();
            Bundle args = new Bundle();
            args.putString(ItemFragment.ARG_SUBREDDIT_ID, subredditId);
            args.putString(ItemFragment.ARG_SUBREDDIT_DISPLAY_NAME, subredditDisplayName);
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
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("aaa", "onLoadFinished");
        mSubreddits = data;
        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSubreddits = null;
        mPagerAdapter.notifyDataSetChanged();
    }

}
