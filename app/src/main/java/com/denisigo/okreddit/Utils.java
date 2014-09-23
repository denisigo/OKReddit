package com.denisigo.okreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Utils {

    private static final String PREF_KEY_FIRSTLAUNCH = "PREF_KEY_FIRSTLAUNCH";

    /**
     * Checks whether internet is available
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }

    /**
     * Is this is the first time app is launching?
     * @param context
     * @return
     */
    public static boolean isFirstLaunch(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_FIRSTLAUNCH, true);
    }

    public static void setFirstLaunch(Context context, boolean state) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(PREF_KEY_FIRSTLAUNCH, state);
        editor.commit();
    }
}
