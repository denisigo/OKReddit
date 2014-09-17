package com.denisigo.okreddit;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.denisigo.okreddit.data.OKRedditContract.PostEntry;
import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;

import java.net.MalformedURLException;
import java.net.URL;

public class FakeFragment extends Fragment {

    public FakeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("aaa", "FRAGMENT onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_item, container, false);

        Bundle args = getArguments();

        TextView tvSubredditName = (TextView)rootView.findViewById(R.id.tvSubredditName);
        tvSubredditName.setText(args.getString("ARG_SUBREDDIT_DISPLAY_NAME"));

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("aaa", "FRAGMENT onDestroy");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("aaa", "FRAGMENT onDestroy");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("aaa", "FRAGMENT onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

}
