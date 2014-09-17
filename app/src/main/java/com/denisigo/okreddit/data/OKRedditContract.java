package com.denisigo.okreddit.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Defines table and column names for the weather database.
 */
public class OKRedditContract {

    public static final String CONTENT_AUTHORITY = "com.denisigo.okreddit";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SUBREDDITS = "subreddits";
    public static final String PATH_POSTS = "posts";

    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /* Defines content of the subreddits table */
    public static final class SubredditEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDITS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;

        public static final String TABLE_NAME = "subreddits";

        public static final String COLUMN_SUBREDDIT_ID = "subreddit_id";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_HEADER_IMG = "header_img";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBSCRIBERS = "subscribers";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_PUBLIC_DESCRIPTION = "public_description";
        public static final String COLUMN_USER_IS_SUBSCRIBER = "user_is_subscriber";
        public static final String COLUMN_USER_IS_MODERATOR = "user_is_moderator";
        public static final String COLUMN_USER_IS_BANNED = "user_is_bannded";
        public static final String COLUMN_ORDER = "_order";

        public static Uri buildSubredditUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSubredditPostsUri(String subredditId) {
            return CONTENT_URI.buildUpon().appendPath(subredditId).appendPath("posts").build();
        }
        public static Uri buildSubredditPostsUri(String subredditId, int offset, int limit) {
            return CONTENT_URI.buildUpon().appendPath(subredditId).
                    appendPath("posts").
                    appendQueryParameter("offset", Integer.toString(offset)).
                    appendQueryParameter("limit", Integer.toString(limit)).
                    build();
        }

        public static String getLimitFromUri(Uri uri){
            return uri.getQueryParameter("limit");
        }
        public static String getOffsetFromUri(Uri uri){
            return uri.getQueryParameter("offset");
        }
        public static String getSubredditIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static final class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_SUBREDDIT = "subreddit";
        public static final String COLUMN_SELFTEXT_HTML = "selftext_html";
        public static final String COLUMN_SELFTEXT = "selftext";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_GILDED = "gilded";
        public static final String COLUMN_CLICKED = "clicked";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_OVER_18 = "over_18";
        public static final String COLUMN_HIDDEN = "hidden";
        public static final String COLUMN_THUMBNAIL = "thumbnail";
        public static final String COLUMN_SUBREDDIT_ID = "subreddit_id";
        public static final String COLUMN_EDITED = "edited";
        public static final String COLUMN_DOWNS = "downs";
        public static final String COLUMN_SAVED = "saved";
        public static final String COLUMN_IS_SELF = "is_self";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PERMALINK = "permalink";
        public static final String COLUMN_STICKED = "sticked";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CREATED_UTC = "created_utc";
        public static final String COLUMN_UPS = "ups";
        public static final String COLUMN_NUM_COMMENTS = "num_comments";
        public static final String COLUMN_VISITED = "visited";
        public static final String COLUMN_ORDER = "_order";

        public static Uri buildPostUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}