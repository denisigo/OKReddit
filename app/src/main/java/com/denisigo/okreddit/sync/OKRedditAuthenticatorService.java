package com.denisigo.okreddit.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class OKRedditAuthenticatorService extends Service {
    private OKRedditAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new OKRedditAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}