package com.denisigo.okreddit.service;

import android.app.IntentService;
import android.content.Intent;

import com.denisigo.okreddit.sync.Syncer;

/**
 * Service responsible only for loading new posts for particular subreddit
 */
public class LoadMorePostsService extends IntentService {
    public final String LOG_TAG = LoadMorePostsService.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT_TITLE = "EXTRA_SUBREDDIT_TITLE";
    public static final String EXTRA_AFTER_POST_NAME = "EXTRA_AFTER_POST_NAME";
    public static final String EXTRA_SUBREDDIT_ID = "EXTRA_SUBREDDIT_ID";

    private Syncer mSyncer;

    public LoadMorePostsService() {
        super("LoadMorePostsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSyncer = new Syncer(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String subredditTitle = intent.getStringExtra(EXTRA_SUBREDDIT_TITLE);
        String subredditId = intent.getStringExtra(EXTRA_SUBREDDIT_ID);
        String afterPostName = intent.getStringExtra(EXTRA_AFTER_POST_NAME);
        mSyncer.syncMorePosts(subredditTitle, subredditId, afterPostName);
    }
}
