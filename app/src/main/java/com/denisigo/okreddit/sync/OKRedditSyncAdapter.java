package com.denisigo.okreddit.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.denisigo.okreddit.R;
import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.PostEntry;

import java.util.List;
import java.util.Vector;

import redditapi.Post;
import redditapi.RedditAPI;
import redditapi.Subreddit;

public class OKRedditSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    public final String LOG_TAG = OKRedditSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private final Context mContext;

    public OKRedditSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "Creating SyncAdapter");
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");


        RedditAPI api = new RedditAPI();

        api.login("denisigo", "x0Qs*%y$Uv", true);

        List<Subreddit> subreddits = api.mineSubreddits();

        Log.i(LOG_TAG, subreddits.get(0).getDisplayName());

        Vector<ContentValues> cVVector = new Vector<ContentValues>(subreddits.size());

        for (Subreddit subreddit : subreddits) {
            ContentValues weatherValues = new ContentValues();

            weatherValues.put(SubredditEntry.COLUMN_SUBREDDIT_ID, subreddit.getId());
            weatherValues.put(SubredditEntry.COLUMN_DISPLAY_NAME, subreddit.getDisplayName());
            weatherValues.put(SubredditEntry.COLUMN_HEADER_IMG, subreddit.getHeaderImg());
            weatherValues.put(SubredditEntry.COLUMN_TITLE, subreddit.getTitle());
            weatherValues.put(SubredditEntry.COLUMN_SUBSCRIBERS, subreddit.getSubscribers());
            weatherValues.put(SubredditEntry.COLUMN_URL, subreddit.getUrl());
            weatherValues.put(SubredditEntry.COLUMN_PUBLIC_DESCRIPTION, subreddit.getPublicDescription());
            weatherValues.put(SubredditEntry.COLUMN_USER_IS_SUBSCRIBER, subreddit.isUserSubscriber());
            weatherValues.put(SubredditEntry.COLUMN_USER_IS_MODERATOR, subreddit.isUserModerator());
            weatherValues.put(SubredditEntry.COLUMN_USER_IS_BANNED, subreddit.isUserBanned());
            weatherValues.put(SubredditEntry.COLUMN_ORDER, System.currentTimeMillis());

            cVVector.add(weatherValues);
        }


        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            Log.i(LOG_TAG, Integer.toString(mContext.getContentResolver().bulkInsert(SubredditEntry.CONTENT_URI, cvArray)));
        }


        List<Post> posts = api.posts();

        Log.i(LOG_TAG, posts.get(0).getUrl());

        cVVector.clear();

        for (Post post : posts) {
            ContentValues weatherValues = new ContentValues();

            String subredditId = post.getSubredditId().split("_")[1];

            weatherValues.put(PostEntry.COLUMN_DOMAIN, post.getDomain());
            weatherValues.put(PostEntry.COLUMN_SUBREDDIT, post.getSubreddit());
            weatherValues.put(PostEntry.COLUMN_SELFTEXT_HTML, post.getSelftextHtml());
            weatherValues.put(PostEntry.COLUMN_SELFTEXT, post.getSelftext());
            weatherValues.put(PostEntry.COLUMN_POST_ID, post.getId());
            weatherValues.put(PostEntry.COLUMN_GILDED, post.getGilded());
            weatherValues.put(PostEntry.COLUMN_CLICKED, post.isClicked());
            weatherValues.put(PostEntry.COLUMN_AUTHOR, post.getAuthor());
            weatherValues.put(PostEntry.COLUMN_SCORE, post.getScore());
            weatherValues.put(PostEntry.COLUMN_OVER_18, post.isOver18());
            weatherValues.put(PostEntry.COLUMN_HIDDEN, post.isHidden());
            weatherValues.put(PostEntry.COLUMN_THUMBNAIL, post.getThumbnail());
            weatherValues.put(PostEntry.COLUMN_SUBREDDIT_ID, subredditId);
            weatherValues.put(PostEntry.COLUMN_EDITED, post.isEdited());
            weatherValues.put(PostEntry.COLUMN_DOWNS, post.getDowns());
            weatherValues.put(PostEntry.COLUMN_SAVED, post.isSaved());
            weatherValues.put(PostEntry.COLUMN_IS_SELF, post.isSelf());
            weatherValues.put(PostEntry.COLUMN_NAME, post.getName());
            weatherValues.put(PostEntry.COLUMN_PERMALINK, post.getPermalink());
            weatherValues.put(PostEntry.COLUMN_STICKED, post.isSticked());
            weatherValues.put(PostEntry.COLUMN_CREATED, post.getCreated());
            weatherValues.put(PostEntry.COLUMN_URL, post.getUrl());
            weatherValues.put(PostEntry.COLUMN_TITLE, post.getTitle());
            weatherValues.put(PostEntry.COLUMN_CREATED_UTC, post.getCreatedUtc());
            weatherValues.put(PostEntry.COLUMN_UPS, post.getUps());
            weatherValues.put(PostEntry.COLUMN_NUM_COMMENTS, post.getNumComments());
            weatherValues.put(PostEntry.COLUMN_VISITED, post.isVisited());

            weatherValues.put(PostEntry.COLUMN_ORDER, System.currentTimeMillis());

            cVVector.add(weatherValues);
        }


        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            Log.i(LOG_TAG, "Posts: " + Integer.toString(mContext.getContentResolver().bulkInsert(PostEntry.CONTENT_URI, cvArray)));
        }

        return;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context An app context
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1)
            // here.
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        // Schedule the sync for periodic execution
        OKRedditSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Let's do a sync to get things started.
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}