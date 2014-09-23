package com.denisigo.okreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class containing various authentication things.
 */
public class AuthUtils {

    private static final String PREF_KEY_SESSION_ID = "PREF_KEY_SESSION_ID";
    private static final String PREF_KEY_USERNAME = "PREF_KEY_USERNAME";

    private static SharedPreferences getPrefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getPrefsEditor(Context context){
        return getPrefs(context).edit();
    }

    public static boolean isLoggedIn(Context context){
        return AuthUtils.getSessionId(context) != null;
    }

    public static String getSessionId(Context context) {
        return getPrefs(context).getString(PREF_KEY_SESSION_ID, null);
    }

    private static void setSessionId(Context context, String sessionId) {
        SharedPreferences.Editor editor = getPrefsEditor(context);
        editor.putString(PREF_KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    private static void clearSessionId(Context context) {
        SharedPreferences.Editor editor = getPrefsEditor(context);
        editor.remove(PREF_KEY_SESSION_ID);
        editor.commit();
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString(PREF_KEY_USERNAME, null);
    }

    private static void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getPrefsEditor(context);
        editor.putString(PREF_KEY_USERNAME, username);
        editor.commit();
    }

    private static void clearUsername(Context context) {
        SharedPreferences.Editor editor = getPrefsEditor(context);
        editor.remove(PREF_KEY_USERNAME);
        editor.commit();
    }

    public static void setUser(Context context, String username, String sessionId){
        setUsername(context, username);
        setSessionId(context, sessionId);
    }

    public static void clearUser(Context context){
        clearUsername(context);
        clearSessionId(context);
    }
}
