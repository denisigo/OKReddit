package com.denisigo.okreddit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.denisigo.okreddit.data.OKRedditContract.PostEntry;
import com.denisigo.okreddit.data.OKRedditContract.SubredditEntry;
import com.denisigo.okreddit.service.LoadMorePostsService;

import java.io.File;

import static android.support.v4.content.FileProvider.getUriForFile;

public class ItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_SUBREDDIT_ID = "ARG_SUBREDDIT_ID";
    public static final String ARG_SUBREDDIT_DISPLAY_NAME = "ARG_SUBREDDIT_DISPLAY_NAME";
    public static final String ARG_SUBREDDIT_TITLE = "ARG_SUBREDDIT_TITLE";
    public static final String ARG_OFFSET = "ARG_OFFSET";

    private static final int POSTS_LOADER = 0;

    // We load a bit more posts to have time to load more if provider returned less
    private static final int POSTS_LIMIT = 10;

    private static final String[] POST_COLUMNS = {
            PostEntry.TABLE_NAME + "." + PostEntry._ID,
            PostEntry.COLUMN_POST_ID,
            PostEntry.COLUMN_TITLE,
            PostEntry.COLUMN_URL,
            PostEntry.COLUMN_THUMBNAIL,
            PostEntry.COLUMN_CREATED,
            PostEntry.COLUMN_AUTHOR,
            PostEntry.COLUMN_SCORE,
            PostEntry.COLUMN_NAME,
            PostEntry.COLUMN_VOTED,
            PostEntry.COLUMN_SUBREDDIT_ID,
            PostEntry.COLUMN_PERMALINK
    };

    public static final int COL_POST_ID = 0;
    public static final int COL_POST_POST_ID = 1;
    public static final int COL_POST_TITLE = 2;
    public static final int COL_POST_URL = 3;
    public static final int COL_POST_THUMBNAIL = 4;
    public static final int COL_POST_CREATED = 5;
    public static final int COL_POST_AUTHOR = 6;
    public static final int COL_POST_SCORE = 7;
    public static final int COL_POST_NAME = 8;
    public static final int COL_POST_VOTED = 9;
    public static final int COL_POST_SUBREDDIT_ID = 10;
    public static final int COL_POST_PERMALINK = 11;

    private ProgressBar mPbThumbnailProgress;
    private ImageView mIvThumbnail;
    private ToggleButton mTbVoteUp;
    private ToggleButton mTbVoteDown;
    private LinearLayout mLlContent;
    private ProgressBar mPbProgress;
    private TextView mTvSubredditName;

    private ShareActionProvider mShareActionProvider;

    private Reddit mReddit;
    private Cursor mPost;
    private int mPostScore;
    private int mOffset = 0;
    String mSubredditDisplayName;
    String mSubredditTitle;
    String mSubredditId;

    public ItemFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_item, container, false);
        mIvThumbnail = (ImageView) rootView.findViewById(R.id.thumbnail);
        mPbThumbnailProgress = (ProgressBar)rootView.findViewById(R.id.thumbnailProgress);
        mTbVoteUp = (ToggleButton) rootView.findViewById(R.id.voteUp);
        mTbVoteDown = (ToggleButton) rootView.findViewById(R.id.voteDown);
        mTbVoteDown.setOnClickListener(mOnVoteClickListener);
        mTbVoteUp.setOnClickListener(mOnVoteClickListener);

        mLlContent = (LinearLayout)rootView.findViewById(R.id.content);
        mPbProgress = (ProgressBar)rootView.findViewById(R.id.postProgress);
        mTvSubredditName = (TextView)rootView.findViewById(R.id.subredditName);

        // Until post is loaded show progress and hide content
        showProgress(true);
        showContent(false);
        updateSubredditName();

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mSubredditDisplayName = args.getString(ARG_SUBREDDIT_DISPLAY_NAME);
        mSubredditTitle = args.getString(ARG_SUBREDDIT_TITLE);
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

    private void updateSubredditName(){
        mTvSubredditName.setText("/r/" + mSubredditDisplayName);
    }

    /**
     * Populate post view with current cursor data.
     */
    private void updatePost(){

        // TODO: well, there can be situation, when no posts more for this subreddit,
        // we should handle it somehow
        if(!mPost.moveToFirst())
            return;

        showContent(true);

        final String postUrl = mPost.getString(COL_POST_URL);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast toast = Toast.makeText(getActivity(), R.string.offline_unable_viewurl, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, postUrl);
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

        // Url
        TextView tvUrl = (TextView)getView().findViewById(R.id.url);
        tvUrl.setText(postUrl);
        // Set on click handler to open webview activity
        tvUrl.setOnClickListener(onClickListener);

        // Score
        mPostScore = mPost.getInt(COL_POST_SCORE);
        updatePostScore();

        // Vote buttons
        String voted = mPost.getString(COL_POST_VOTED);
        if (voted != null){
            if (voted.equals(Reddit.VOTE_UP)){
                mTbVoteUp.setChecked(true);
                mTbVoteUp.setEnabled(false);
            } else if (voted.equals(Reddit.VOTE_DOWN)){
                mTbVoteDown.setChecked(true);
                mTbVoteDown.setEnabled(false);
            }
        }

        // Launch async task to load image out of main thread
        String thumbnailUrl = mPost.getString(COL_POST_THUMBNAIL);
        // Reddit has special value meaning reddit face
        if (!thumbnailUrl.equals("self")){
            ImageLoader loader = new ImageLoader();
            loader.execute(postId, postUrl, mPost.getString(COL_POST_THUMBNAIL));
        } else
            setThumbnail(R.drawable.reddit_logo);

        // Set on click handler to open webview activity
        mIvThumbnail.setOnClickListener(onClickListener);
    }

    /**
     * Updates post score
     */
    private void updatePostScore(){
        TextView tvScore = (TextView)getView().findViewById(R.id.score);

        String strScore = "";
        if (mPostScore > 0) {
            strScore = "+";
            tvScore.setTextColor(getResources().getColor(R.color.score_positive));
        } else {
            tvScore.setTextColor(getResources().getColor(R.color.score_negative));
        }
        tvScore.setText(strScore + mPostScore);
    }

    /**
     * Shows thumbnail and hides progress
     */
    private void enableThumbnail(){
        mPbThumbnailProgress.setVisibility(View.GONE);
    }

    /**
     * Set resource image for thumbnail.
     * @param res
     */
    private void setThumbnail(int res){
        enableThumbnail();
        mIvThumbnail.setImageResource(R.drawable.reddit_logo);
    }

    /**
     * Sets the thumbnail image. If null is provided, it assumes that image was unable to load
     * and displays error dummy.
     * @param bitmap
     */
    private void setThumbnail(Bitmap bitmap){

        enableThumbnail();

        // We could lost out view while loading image
        if (getView() == null)
            return;

        if (bitmap != null) {
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            int newWidth = mIvThumbnail.getWidth();
            float scaleFactor = (float) newWidth / (float) imageWidth;
            int newHeight = (int) (imageHeight * scaleFactor);

            // newWidth or newHeight may be 0 for some reasons.
            // I suspect it is because mIvThumbnail.getWidth() == 0
            try {
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }catch (java.lang.IllegalArgumentException e){
                e.printStackTrace();
                mIvThumbnail.setImageResource(R.drawable.reddit_logo_error);
                return;
            }
            mIvThumbnail.setImageBitmap(bitmap);
        } else {
            mIvThumbnail.setImageResource(R.drawable.reddit_logo_error);
        }
    }

    /**
     * Async loader of the post thumb image
     */
    private class ImageLoader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            if (getActivity() != null)
                return ImgUtils.getPostThumbnail(getActivity(), strings[0], strings[1], strings[2]);
            return null;
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

        String sortOrder = String.format("%s ASC LIMIT %s, %s",
                PostEntry.COLUMN_ORDER, mOffset, POSTS_LIMIT);

        Uri uri = SubredditEntry.buildSubredditPostsUri(mSubredditId);

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
        updatePost();

        if (mShareActionProvider != null)
            updateShare();

        showProgress(false);

        // If cursor contains at least one post (means it is not inital syncing) AND
        // If cursor contains less posts than we needed, it means
        // that we have to load more posts right now
        if (data.getCount() > 0 && data.getCount() < POSTS_LIMIT){

            String afterPostName = null;

            // Load posts after the last exist (if any)
            if(data.moveToLast())
                afterPostName = data.getString(COL_POST_NAME);

            // Start service
            Intent intent = new Intent(getActivity(), LoadMorePostsService.class);
            intent.putExtra(LoadMorePostsService.EXTRA_SUBREDDIT_TITLE, mSubredditDisplayName);
            intent.putExtra(LoadMorePostsService.EXTRA_SUBREDDIT_ID, mSubredditId);
            intent.putExtra(LoadMorePostsService.EXTRA_AFTER_POST_NAME, afterPostName);
            getActivity().startService(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPost = null;
    }


    /**
     * Listener for vote buttons clicks
     */
    View.OnClickListener mOnVoteClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            if (!Utils.isNetworkConnected(getActivity())){
                Toast toast = Toast.makeText(getActivity(), R.string.offline_unable_vote, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (!AuthUtils.isLoggedIn(getActivity())){
                Toast toast = Toast.makeText(getActivity(), R.string.notloggedin_unable_vote, Toast.LENGTH_LONG);
                toast.show();
                return;
            }


            int change = 0;

            // Dealing with vote buttons o_O
            String direction = Reddit.VOTE_NONE;
            if (view.getId() == R.id.voteUp){
                    change = 1;

                if (mTbVoteDown.isChecked()) {
                    mTbVoteDown.setChecked(false);
                    change = 2;
                }

                mTbVoteUp.setEnabled(false);
                mTbVoteDown.setEnabled(true);

                direction = Reddit.VOTE_UP;
            } else if (view.getId() == R.id.voteDown){
                change = -1;

                if (mTbVoteUp.isChecked()) {
                    mTbVoteUp.setChecked(false);
                    change = -2;
                }

                mTbVoteDown.setEnabled(false);
                mTbVoteUp.setEnabled(true);

                direction = Reddit.VOTE_DOWN;
            }

            mPostScore += change;
            updatePostScore();

            // Lauch async vote api call
            String id = mPost.getString(COL_POST_POST_ID);
            String name = mPost.getString(COL_POST_NAME);
            VoteTaskArgs args = new VoteTaskArgs(id, name, direction);
            (new VoteTask()).execute(args);
        }
    };

    /**
     * In order to save resources, we cache Reddit object
     * @return
     */
    private Reddit getReddit(){
        if (mReddit == null)
            mReddit = new Reddit(getActivity());
        return mReddit;
    }

    /**
     * Async loader of the post thumb image
     */
    private class VoteTaskArgs{
        public String id;
        public String name;
        public String direction;
        public VoteTaskArgs(String id, String name,  String direction){
            this.id = id;
            this.name = name;
            this.direction = direction;
        }
    }

    private class VoteTask extends AsyncTask<VoteTaskArgs, Void, Boolean>{

        @Override
        protected Boolean doInBackground(VoteTaskArgs... args) {
            VoteTaskArgs args_ = args[0];
            return getReddit().vote(args_.id, args_.name, args_.direction);
        }
    }

    private void showProgress(boolean state){
        mPbProgress.setVisibility(state?View.VISIBLE:View.GONE);
    }
    private void showContent(boolean state){
        mLlContent.setVisibility(state?View.VISIBLE:View.GONE);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.item_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mPost != null)
            updateShare();
    }

    /**
     * Update share intent.
     */
    private void updateShare(){

        // http://developer.android.com/training/sharing/send.html
        // http://developer.android.com/reference/android/support/v4/content/FileProvider.html

        if(!mPost.moveToFirst())
            return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("image/jpeg");

        String text = mPost.getString(COL_POST_TITLE) + " - http://www.reddit.com" + mPost.getString(COL_POST_PERMALINK);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        File thumbFile = ImgUtils.getPostThumbnailFile(getActivity(), mPost.getString(COL_POST_POST_ID));
        if (thumbFile.exists()) {
            Uri contentUri = getUriForFile(getActivity(), getString(R.string.fileprovider_authority), thumbFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        mShareActionProvider.setShareIntent(shareIntent);
    }
}
