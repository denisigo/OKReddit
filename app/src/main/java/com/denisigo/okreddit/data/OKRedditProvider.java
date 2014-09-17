package com.denisigo.okreddit.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.PostEntry;

public class OKRedditProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private OKRedditDbHelper mOpenHelper;

    private static final int SUBREDDITS = 100;
    private static final int SUBREDDIT = 101;

    private static final int POSTS = 200;
    private static final int POST = 201;
    private static final int SUBREDDIT_POSTS = 202;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = OKRedditContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, OKRedditContract.PATH_SUBREDDITS, SUBREDDITS);
        matcher.addURI(authority, OKRedditContract.PATH_SUBREDDITS + "/*", SUBREDDIT);

        matcher.addURI(authority, OKRedditContract.PATH_POSTS, POSTS);
        matcher.addURI(authority, OKRedditContract.PATH_POSTS + "/*", POST);
        matcher.addURI(authority, OKRedditContract.PATH_SUBREDDITS + "/*/posts", SUBREDDIT_POSTS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new OKRedditDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case SUBREDDITS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        OKRedditContract.SubredditEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case POSTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        OKRedditContract.PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // Gets posts from selected subreddit with given limit and offset
            case SUBREDDIT_POSTS: {

                String offset = SubredditEntry.getOffsetFromUri(uri);
                String limit = SubredditEntry.getLimitFromUri(uri);

                if (limit != null){
                    if (offset != null){
                        limit = offset + "," + limit;
                    }
                }

                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostEntry.TABLE_NAME,
                        projection,
                        PostEntry.COLUMN_SUBREDDIT_ID+"=?",
                        new String[]{SubredditEntry.getSubredditIdFromUri(uri)},
                        null,
                        null,
                        sortOrder,
                        limit
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SUBREDDITS:
                return OKRedditContract.SubredditEntry.CONTENT_TYPE;
            case SUBREDDIT:
                return OKRedditContract.SubredditEntry.CONTENT_ITEM_TYPE;
            case POSTS:
                return OKRedditContract.SubredditEntry.CONTENT_TYPE;
            case POST:
                return OKRedditContract.SubredditEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SUBREDDITS: {
                long _id = db.insert(OKRedditContract.SubredditEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = OKRedditContract.SubredditEntry.buildSubredditUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case POSTS: {
                long _id = db.insert(OKRedditContract.PostEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = OKRedditContract.PostEntry.buildPostUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case SUBREDDIT:
                rowsDeleted = db.delete(
                        OKRedditContract.SubredditEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case POST:
                rowsDeleted = db.delete(
                        OKRedditContract.PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SUBREDDIT:
                rowsUpdated = db.update(OKRedditContract.SubredditEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBREDDITS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(OKRedditContract.SubredditEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            case POSTS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(OKRedditContract.PostEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
