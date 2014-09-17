package com.denisigo.okreddit.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OKRedditSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static OKRedditSyncAdapter sOKRedditSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sOKRedditSyncAdapter == null) {
                sOKRedditSyncAdapter = new OKRedditSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sOKRedditSyncAdapter.getSyncAdapterBinder();
    }
}
