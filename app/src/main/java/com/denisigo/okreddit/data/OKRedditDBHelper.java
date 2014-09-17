package com.denisigo.okreddit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.PostEntry;

import redditapi.Post;

public class OKRedditDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "okreddit.db";

    public OKRedditDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SUBREDDITS_TABLE = "CREATE TABLE " + SubredditEntry.TABLE_NAME + " (" +
                SubredditEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SubredditEntry.COLUMN_SUBREDDIT_ID + " TEXT UNIQUE NOT NULL, " +
                SubredditEntry.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_HEADER_IMG + " TEXT, " +
                SubredditEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_SUBSCRIBERS + " INTEGER NOT NULL, " +
                SubredditEntry.COLUMN_URL + " TEXT NOT NULL, " +
                SubredditEntry.COLUMN_PUBLIC_DESCRIPTION + " TEXT, " +
                SubredditEntry.COLUMN_USER_IS_SUBSCRIBER + " INTEGER, " +
                SubredditEntry.COLUMN_USER_IS_MODERATOR + " INTEGER, " +
                SubredditEntry.COLUMN_USER_IS_BANNED + " INTEGER, " +

                SubredditEntry.COLUMN_ORDER + " INTEGER, " +
                "UNIQUE (" + SubredditEntry.COLUMN_SUBREDDIT_ID +") ON CONFLICT REPLACE"+
                " );" +
                "CREATE INDEX order ON " + SubredditEntry.TABLE_NAME + "(" + SubredditEntry.COLUMN_ORDER +")";

        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PostEntry.COLUMN_DOMAIN + " TEXT, " +
                PostEntry.COLUMN_SUBREDDIT + " TEXT NOT NULL, " +
                PostEntry.COLUMN_SELFTEXT_HTML + " TEXT, " +
                PostEntry.COLUMN_SELFTEXT + " TEXT, " +
                PostEntry.COLUMN_POST_ID + " TEXT UNIQUE NOT NULL, " +
                PostEntry.COLUMN_GILDED + " INTEGER, " +
                PostEntry.COLUMN_CLICKED + " INTEGER, " +
                PostEntry.COLUMN_AUTHOR + " TEXT, " +
                PostEntry.COLUMN_SCORE + " INTEGER, " +
                PostEntry.COLUMN_OVER_18 + " INTEGER, " +
                PostEntry.COLUMN_HIDDEN + " INTEGER, " +
                PostEntry.COLUMN_THUMBNAIL + " TEXT, " +
                PostEntry.COLUMN_SUBREDDIT_ID + " TEXT NOT NULL, " +
                PostEntry.COLUMN_EDITED + " INTEGER, " +
                PostEntry.COLUMN_DOWNS + " INTEGER, " +
                PostEntry.COLUMN_SAVED + " INTEGER, " +
                PostEntry.COLUMN_IS_SELF + " INTEGER, " +
                PostEntry.COLUMN_NAME + " TEXT, " +
                PostEntry.COLUMN_PERMALINK + " TEXT, " +
                PostEntry.COLUMN_STICKED + " INTEGER, " +
                PostEntry.COLUMN_CREATED + " INTEGER, " +
                PostEntry.COLUMN_URL + " TEXT, " +
                PostEntry.COLUMN_TITLE + " TEXT, " +
                PostEntry.COLUMN_CREATED_UTC + " INTEGER, " +
                PostEntry.COLUMN_UPS + " INTEGER, " +
                PostEntry.COLUMN_NUM_COMMENTS + " INTEGER, " +
                PostEntry.COLUMN_VISITED + " INTEGER, " +

                PostEntry.COLUMN_ORDER + " INTEGER, " +
                " FOREIGN KEY (" + PostEntry.COLUMN_SUBREDDIT_ID + ") REFERENCES " +
                SubredditEntry.TABLE_NAME + " (" + SubredditEntry.COLUMN_SUBREDDIT_ID + "), " +
                "UNIQUE (" + PostEntry.COLUMN_POST_ID +") ON CONFLICT REPLACE"+
                " );" +
                "CREATE INDEX order ON " + SubredditEntry.TABLE_NAME + "(" + SubredditEntry.COLUMN_ORDER +")";

        /*
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +

                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";


        */
        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SUBREDDITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubredditEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
