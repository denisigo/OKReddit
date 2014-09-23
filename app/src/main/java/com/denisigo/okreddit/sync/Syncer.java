package com.denisigo.okreddit.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;

import com.denisigo.okreddit.ImgUtils;
import com.denisigo.okreddit.Reddit;
import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.PostEntry;
import com.denisigo.okreddit.data.OKRedditContract.SyncstateEntry;

import java.util.List;
import java.util.Vector;

import redditapi.ItemWrapper;
import redditapi.Listing;
import redditapi.Post;
import redditapi.Subreddit;
import retrofit.RetrofitError;

/**
 * Class responsible for any syncing stuff.
 */
public class Syncer {

    public static final int SYNC_STATE_STOPPED = 0;
    public static final int SYNC_STATE_RUNNING = 1;

    // Bunch size for one request
    private static final int SUBREDDITS_LIMIT = 100;
    private static final int POSTS_LIMIT = 25;
    // How much posts we can load per one "session"
    private static final int POSTS_TOTAL_LIMIT = 25;
    // How much images we can load and cache
    private static final int POST_THUMBNAIL_CACHE_LIMIT = 5;
    // Pause between sibling requests to not abuse reddit api, ms
    private static final int REQUEST_DELAY = 1500;

    private Context mContext;
    private Reddit mReddit;

    public Syncer(Context context){
        mContext = context;
        mReddit = new Reddit(context);
    }

    /**
     * Sync posts - remove existing ones and download new ones.
     */
    public void syncPosts(){
        // TODO: posts from /r/videos not loading o_O

        // Clear all existing posts
        mContext.getContentResolver().delete(PostEntry.CONTENT_URI, null, null);

        // Clear image cache
        ImgUtils.clearCache(mContext);

        // Get posts for all our subreddits
        final String[] columns = {
                SubredditEntry.COLUMN_DISPLAY_NAME,
                SubredditEntry.COLUMN_SUBREDDIT_ID
        };
        int COL_SUBREDDIT_DISPLAY_NAME = 0;
        int COL_SUBREDDIT_ID = 1;

        Cursor subreddits = mContext.getContentResolver().query(SubredditEntry.CONTENT_URI,
                columns, null, null, null);
        try {
            while (subreddits.moveToNext()) {
                String subredditDisplayName = subreddits.getString(COL_SUBREDDIT_DISPLAY_NAME);
                String subredditId = subreddits.getString(COL_SUBREDDIT_ID);
                syncMorePosts(subredditDisplayName, subredditId, null);

            }
        } finally {
            subreddits.close();
        }
    }

    /**
     * Download and store new posts from some place
     * @param subredditDisplayName String display name of the subreddit
     * @param subredditId String id of the subreddit
     * @param afterPostName String post name from which we want to get new posts
     */
    public void syncMorePosts(String subredditDisplayName, String subredditId, String afterPostName){
        int thumbnailCacheCounter = 0;
        int limitCounter = 0;
        String after = afterPostName;

        Listing<ItemWrapper<Post>> posts = null;
        while (true){
            try {
                posts = mReddit.getService().subredditPosts(subredditDisplayName, after, Integer.toString(POSTS_LIMIT));
            } catch (RetrofitError e){
                e.printStackTrace();
                return;
            }

            if (posts.data.children != null) {
                // Store posts to db
                Vector<ContentValues> addedPosts = storePosts(subredditId, posts.data.children);

                // TODO: move caching thumbnails to another service/thread since it may take
                // a lot of time
                /*
                // Cache some first thumbnails
                for(ContentValues post : addedPosts){
                    if (thumbnailCacheCounter < POST_THUMBNAIL_CACHE_LIMIT) {
                        ImgUtils.loadAndCachePostThumbnail(mContext,
                                post.getAsString(PostEntry.COLUMN_POST_ID),
                                post.getAsString(PostEntry.COLUMN_URL),
                                post.getAsString(PostEntry.COLUMN_THUMBNAIL));
                        thumbnailCacheCounter++;
                    }
                }
                */
            }

            // Stop loading if we reached total limit
            limitCounter += POSTS_LIMIT;
            if (limitCounter >= POSTS_TOTAL_LIMIT)
                break;

            // If there is after value in response, using it next time
            if (posts.data.after != null) {
                after = posts.data.after;
                SystemClock.sleep(REQUEST_DELAY);
            } else
                break;
        }
    }

    private Vector<ContentValues> storePosts(String subredditId, List<ItemWrapper<Post>> posts){

        Vector<ContentValues> cVVector = new Vector<ContentValues>(posts.size());

        for (ItemWrapper<Post> postWrapper : posts) {
            Post post = postWrapper.data;

            ContentValues weatherValues = new ContentValues();

            String subredditId_ = post.getSubredditId().split("_")[1];

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
            weatherValues.put(PostEntry.COLUMN_SUBREDDIT_ID, subredditId_);
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
            // We use uri for particular subreddit in order to cursorloader auto notify to work
            Uri uri = SubredditEntry.buildSubredditPostsUri(subredditId);
            mContext.getContentResolver().bulkInsert(uri, cvArray);
        }

        return cVVector;
    }

    public void syncSubreddits(){
        // Clear all existing subreddits
        mContext.getContentResolver().delete(SubredditEntry.CONTENT_URI, null, null);

        String after = null;

        Listing<ItemWrapper<Subreddit>> subreddits = null;
        while (true){
            try {
                subreddits = mReddit.getService().mineSubreddits(after, Integer.toString(SUBREDDITS_LIMIT));
            } catch (RetrofitError e){
                e.printStackTrace();
                return;
            }

            if (subreddits.data.children != null)
                storeSubreddits(subreddits.data.children);

            if (subreddits.data.after != null) {
                after = subreddits.data.after;
                SystemClock.sleep(REQUEST_DELAY);
            } else
                break;
        }
    }

    private void storeSubreddits(List<ItemWrapper<Subreddit>> subreddits){
        Vector<ContentValues> cVVector = new Vector<ContentValues>(subreddits.size());

        for (ItemWrapper<Subreddit> subredditWrapper : subreddits) {
            Subreddit subreddit = subredditWrapper.data;

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
            mContext.getContentResolver().bulkInsert(SubredditEntry.CONTENT_URI, cvArray);
        }
    }

    /**
     * Saves to the content provider that sync is running or not
     */
    public void setSyncState(int state){

        // Get posts for all our subreddits
        final String[] columns = {
                SyncstateEntry._ID,
                SyncstateEntry.COLUMN_TYPE
        };
        int COL_ID = 0;
        int COL_TYPE = 1;

        Cursor syncstate = mContext.getContentResolver().
                query(SyncstateEntry.CONTENT_URI, columns, null, null, null);

        long id = 0;
        boolean exists = false;

        if (syncstate.moveToFirst()){
            exists = true;
            id = syncstate.getLong(COL_ID);
        }
        syncstate.close();

        ContentValues values = new ContentValues();
        values.put(SyncstateEntry.COLUMN_TYPE, "full");
        values.put(SyncstateEntry.COLUMN_STATE, state);

        if(!exists)
            mContext.getContentResolver().insert(SyncstateEntry.CONTENT_URI, values);
        else
            mContext.getContentResolver().update(SyncstateEntry.CONTENT_URI, values,
                    SyncstateEntry._ID + "=?", new String[]{Long.toString(id)});
    }

}
