package com.denisigo.okreddit.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.denisigo.okreddit.data.OKRedditContract;
import com.denisigo.okreddit.sync.OKRedditSyncAdapter;

import java.util.Vector;

/**
 * Service responsible for initializing the app when it is being launched for the first time
 */
public class InitAppService extends IntentService {
    public final String LOG_TAG = InitAppService.class.getSimpleName();

    public InitAppService() {
        super("InitAppService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Add basic subreddits
        Vector<ContentValues> cVVector = new Vector<ContentValues>(0);

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID, "2qh33");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_DISPLAY_NAME, "funny");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBSCRIBERS, 0);
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_URL, "");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_TITLE, "funny");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_ORDER, System.currentTimeMillis());
        cVVector.add(weatherValues);

        weatherValues = new ContentValues();
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID, "2qh0u");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_DISPLAY_NAME, "pics");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBSCRIBERS, 0);
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_URL, "");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_TITLE, "/r/Pics");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_ORDER, System.currentTimeMillis());
        cVVector.add(weatherValues);

        weatherValues = new ContentValues();
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID, "2qh1o");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_DISPLAY_NAME, "aww");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBSCRIBERS, 0);
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_URL, "");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_TITLE, "The cutest things on the internet!");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_ORDER, System.currentTimeMillis());
        cVVector.add(weatherValues);

        weatherValues = new ContentValues();
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBREDDIT_ID, "2qh61");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_DISPLAY_NAME, "WTF");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_SUBSCRIBERS, 0);
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_URL, "");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_TITLE, "WTF?!");
        weatherValues.put(OKRedditContract.SubredditEntry.COLUMN_ORDER, System.currentTimeMillis());
        cVVector.add(weatherValues);

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContentResolver().bulkInsert(OKRedditContract.SubredditEntry.CONTENT_URI, cvArray);
        }

        // And lauch sync immediately
        OKRedditSyncAdapter.syncImmediately(getApplicationContext());
    }
}
