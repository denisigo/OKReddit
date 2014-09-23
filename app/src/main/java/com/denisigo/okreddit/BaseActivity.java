package com.denisigo.okreddit;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.denisigo.okreddit.sync.OKRedditSyncAdapter;


public class BaseActivity extends ActionBarActivity{

    protected static final int RC_LOGIN = 0;

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected FrameLayout mDrawerContent;
    protected Reddit mReddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReddit = new Reddit(this);
    }

    protected void createNavigationDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerContent = (FrameLayout)findViewById(R.id.drawer);
        View content = getLayoutInflater().inflate(R.layout.drawer, null);
        mDrawerContent.addView(content);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Inits navigation drawer based on current states.
     */
    protected void initNavigationDrawer(){

        boolean loggedIn = AuthUtils.isLoggedIn(this);

        LinearLayout itemUser = (LinearLayout)mDrawerContent.findViewById(R.id.userPanel);
        Button itemLogin = (Button)mDrawerContent.findViewById(R.id.login);
        Button itemManageSubreddits = (Button)mDrawerContent.findViewById(R.id.manageSubreddits);

        itemUser.setVisibility(View.GONE);
        itemLogin.setVisibility(View.GONE);
        itemManageSubreddits.setVisibility(View.GONE);

        // Show different controls whether user is logged in or not
        if (loggedIn){
            itemUser.setVisibility(View.VISIBLE);
            TextView username = (TextView)itemUser.findViewById(R.id.username);

            username.setText(getString(R.string.format_user_greeting, AuthUtils.getUsername(this)));

            itemManageSubreddits.setVisibility(View.VISIBLE);
        } else {
            itemLogin.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Drawer Login item handler. Opens login activity.
     * @param view
     */
    public void onDrawerLoginClick(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, RC_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LOGIN && resultCode == RESULT_OK){
            initNavigationDrawer();
            // Launch sync to load user's data
            OKRedditSyncAdapter.syncImmediately(this);
        }
    }

    /**
     * Drawer Logout item handler. Logs out the user
     * @param view
     */
    public void onDrawerLogoutClick(View view){
        mReddit.logoutUser();
        initNavigationDrawer();
    }

    /**
     * Drawer subreddits item handler.
     * @param view
     */
    public void onDrawerManageSubredditsClick(View view){
        startActivity(new Intent(this, SubredditsActivity.class));
    }

    /**
     * Several methods needed to handle configuration changes and activity state
     * changes
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
