package com.denisigo.okreddit;

import android.content.Intent;
import android.os.Bundle;

import com.denisigo.okreddit.service.InitAppService;
import com.denisigo.okreddit.sync.OKRedditSyncAdapter;


public class MainActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ViewFragment())
                    .commit();
        }

        createNavigationDrawer();
        initNavigationDrawer();

        // Launch initialization process if we're first time launching
        if (Utils.isFirstLaunch(this)) {
            startService(new Intent(this, InitAppService.class));
            Utils.setFirstLaunch(this, false);
        }

        // Every 3 hours perform full update.
        OKRedditSyncAdapter.initializeSyncAdapter(this);
    }


}
