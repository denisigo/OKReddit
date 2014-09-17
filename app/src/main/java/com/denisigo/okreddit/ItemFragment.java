package com.denisigo.okreddit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.denisigo.okreddit.data.OKRedditContract;
import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.data.OKRedditContract.PostEntry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_SUBREDDIT_ID = "ARG_SUBREDDIT_ID";
    public static final String ARG_SUBREDDIT_DISPLAY_NAME = "ARG_SUBREDDIT_DISPLAY_NAME";
    public static final String ARG_OFFSET = "ARG_OFFSET";

    private static final int POSTS_LOADER = 0;

    private static final String[] POST_COLUMNS = {
            PostEntry.TABLE_NAME + "." + PostEntry._ID,
            PostEntry.COLUMN_POST_ID,
            PostEntry.COLUMN_TITLE,
            PostEntry.COLUMN_URL,
            PostEntry.COLUMN_THUMBNAIL,
            PostEntry.COLUMN_CREATED,
            PostEntry.COLUMN_AUTHOR,
            PostEntry.COLUMN_SCORE
    };

    public static final int COL_POST_ID = 0;
    public static final int COL_POST_POST_ID = 1;
    public static final int COL_POST_TITLE = 2;
    public static final int COL_POST_URL = 3;
    public static final int COL_POST_THUMBNAIL = 4;
    public static final int COL_POST_CREATED = 5;
    public static final int COL_POST_AUTHOR = 6;
    public static final int COL_POST_SCORE = 7;

    private ImageView mIvThumbnail;

    private Cursor mPost;
    private int mOffset = 0;
    String mSubredditName;
    String mSubredditId;

    public ItemFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_item, container, false);
        mIvThumbnail = (ImageView) rootView.findViewById(R.id.thumbnail);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mSubredditName = args.getString(ARG_SUBREDDIT_DISPLAY_NAME);
        mSubredditId = args.getString(ARG_SUBREDDIT_ID);
        mOffset = args.getInt(ARG_OFFSET);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(POSTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Populate psot view with current cursor data.
     */
    private void updatePost(){

        TextView tvSubredditName = (TextView)getView().findViewById(R.id.tvSubredditName);
        tvSubredditName.setText("/r/" + mSubredditName);

        if (mPost == null || mPost.getCount() == 0)
            return;

        mPost.moveToFirst();

        final String url = mPost.getString(COL_POST_URL);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, url);
                startActivity(intent);
            }
        };

        String postId = mPost.getString(COL_POST_POST_ID);

        TextView tvTitle = (TextView)getView().findViewById(R.id.title);
        tvTitle.setText(mPost.getString(COL_POST_TITLE));

        // Generate info text
        TextView tvPostInfo = (TextView)getView().findViewById(R.id.postInfo);
        long created = mPost.getLong(COL_POST_CREATED)*1000;
        String time = DateUtils.getRelativeDateTimeString(getActivity(), created, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0).toString();
        tvPostInfo.setText(getString(R.string.format_post_info, time, mPost.getString(COL_POST_AUTHOR)));

        TextView tvUrl = (TextView)getView().findViewById(R.id.url);
        tvUrl.setText(url);
        // Set on click handler to open webview activity
        tvUrl.setOnClickListener(onClickListener);

        TextView tvScore = (TextView)getView().findViewById(R.id.score);
        int score = mPost.getInt(COL_POST_SCORE);
        String strScore = "";
        if (score > 0)
            strScore = "+";
        tvScore.setText(strScore + score);

        // Load thumbnail image
        String thumbUrl = null;
        // If we have url, see if we can get higher resolution image from there
        if (url != null){
            // We support only imgur at the time, because another sources can send
            // us large pictures we don't want to load.
            // TODO: get some server proxy for image loading purposes

            // Check if it is from imgur and create thumbnail.
            if (ImgurUtils.isImgurFile(url))
                thumbUrl = ImgurUtils.getThumbnailUrl(url, ImgurUtils.THUMBNAIL_SIZE_M, "jpg");
        }

        // If we still don't have thumbnail url, get base reddit thumbnail
        if (thumbUrl == null)
            thumbUrl = mPost.getString(COL_POST_THUMBNAIL);

        // If we finally got thumbnail url launch async task to load image out of main thread
        if (thumbUrl != null) {
            ImageLoader loader = new ImageLoader();
            loader.execute(postId, thumbUrl);
        }

        // Set on click handler to open webview activity
        mIvThumbnail.setOnClickListener(onClickListener);
    }

    /**
     * Sets the thumbnail image. If null is provided, it assumes that image was unable to load
     * and displays error dummy.
     * @param bitmap
     */
    private void setThumbnail(Bitmap bitmap){

        // We could lost out view while loading image
        if (getView() == null)
            return;

        if (bitmap != null) {
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            //int newWidth = thumbnail.getWidth();
            int newWidth = mIvThumbnail.getWidth();
            float scaleFactor = (float) newWidth / (float) imageWidth;
            int newHeight = (int) (imageHeight * scaleFactor);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            mIvThumbnail.setImageBitmap(bitmap);
        } else {
            // Set dummy
        }
    }

    /**
     * Async loader of the post thumb image
     */
    private class ImageLoader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {

            String postId = strings[0];

            URL url = null;
            try {
                url = new URL(strings[1]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            Bitmap bmp = null;
            bmp = ImgUtils.getPostThumbnail(getActivity(), postId, url);
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            setThumbnail(bitmap);
        }
    }

    /**
     * Loader of the subreddit post.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = PostEntry.COLUMN_ORDER + " ASC";

        Uri uri = SubredditEntry.buildSubredditPostsUri(mSubredditId, mOffset, 1);

        return new CursorLoader(
                getActivity(),
                uri,
                POST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPost = data;
        Log.i("aaa", "FRAGMENT onLoadFinished");
        updatePost();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPost = null;
    }

}
