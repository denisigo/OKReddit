package com.denisigo.okreddit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SUBREDDITS_LOADER = 0;

    private static final String[] SUBREDDIT_COLUMNS = {
            SubredditEntry.TABLE_NAME + "." + SubredditEntry._ID,
            SubredditEntry.COLUMN_DISPLAY_NAME
    };

    public static final int COL_SUBREDDIT_ID = 0;
    public static final int COL_SUBREDDIT_DISPLAY_NAME = 1;


    private ListView mListView;

    private SubredditsAdapter mSubredditsAdapter;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSubredditsAdapter = new SubredditsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.subreddits);
        mListView.setAdapter(mSubredditsAdapter);
        //mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {



        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SUBREDDITS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = SubredditEntry.COLUMN_DISPLAY_NAME + " ASC";

        Uri uri = SubredditEntry.CONTENT_URI;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
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
        mSubredditsAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSubredditsAdapter.swapCursor(null);
    }

}
