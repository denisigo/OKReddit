package com.denisigo.okreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.denisigo.okreddit.data.OKRedditContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.util.List;

import redditapi.Account;
import redditapi.Item;
import redditapi.RedditService;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

public class Reddit {

    private static final String PREF_KEY_REDDITAPI_MODHASH = "PREF_KEY_REDDITAPI_MODHASH";

    public static final String VOTE_UP = "1";
    public static final String VOTE_NONE = "0";
    public static final String VOTE_DOWN = "-1";

    public static final String SUBSCRIBE_SUBSCRIBE = "sub";
    public static final String SUBSCRIBE_UNSUBSCRIBE = "unsub";

    private static final String API_ENDPOINT = "https://www.reddit.com/";

    private Context mContext;
    private RedditService mService;

    public Reddit(Context context){

        mContext = context;

        // Force retrofit to add useful headers to every call
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                String sessionId = AuthUtils.getSessionId(mContext);

                if (sessionId != null)
                    request.addHeader("Cookie", "reddit_session=" + sessionId);

                request.addHeader("User-Agent", "OK Reddit Android/1.0 by denisigo");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT)
                .setRequestInterceptor(requestInterceptor)
                .build();

        mService = restAdapter.create(RedditService.class);
    }

    /**
     * Returns raw retrofit reddit api object.
     * @return
     */
    public RedditService getService(){
        return mService;
    }

    /**
     * Get fresh modhash either from previous call or by calling API
     * @return
     */
    public String getModhash(){

        // A modhash is a token that the reddit API requires to help prevent CSRF.
        // http://www.reddit.com/dev/api#modhashes

        // Modhashes are included in the "Listing" responses as well,
        // so if that method has got modhash from its response, it
        // may save it to storage, so check it first
        String modhash = loadModhash(mContext);

        if (modhash != null){
            // We used this modhash already, so delete it from storage.
            // It seems like reddit accepts several API calls with the
            // same modhash, but for any case, let's force a new one for every call
            saveModhash(mContext, null);
        } else {
            // There was no modhash in the storage, request it from API
            try {
                Item<Account> me = mService.me();
                modhash = me.data.modhash;
            } catch (RetrofitError e){
                e.printStackTrace();
            }
        }
        return modhash;
    }

    /**
     * Loads modhash from local storage
     * @param context
     * @return
     */
    private String loadModhash(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_REDDITAPI_MODHASH, null);
    }

    /**
     * Saves modhash to local storage.
     * If some request returns new modhash as well, it should be saved here
     * @param context
     * @param modhash
     */
    public void saveModhash(Context context, String modhash) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_KEY_REDDITAPI_MODHASH, modhash);
        editor.commit();
    }

    /**
     * Handler login process by calling API endpoint and retrieving session_id value
     * @param user user name
     * @param passwd user password
     * @param remember whether rememer the user or not
     * @return String session id if login succeed
     */
    private String login(String user, String passwd, boolean remember){
        Response login = mService.login("json", passwd, remember, user);

        List<Header> headers = login.getHeaders();
        for (Header header : headers) {
            if (header.getName() != null && header.getName().contains("Set-Cookie")) {
                List<HttpCookie> cookies = HttpCookie.parse(header.getValue());
                for (HttpCookie cookie : cookies) {
                    if (cookie.getName().contains("reddit_session")) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Perfomr user log in and stores session id locally if succeed
     * @param username
     * @param password
     * @return
     */
    public boolean loginUser(String username, String password){
        String sessionId = login(username, password, true);
        if (sessionId != null){
            AuthUtils.setUser(mContext, username, sessionId);
            return true;
        }
        return false;
    }

    /**
     * Just logs user out
     */
    public void logoutUser(){
        // TODO: add call to reddit api?
        AuthUtils.clearUser(mContext);
    }

    /**
     * @param id String id of the post
     * @param name String "name" of the post including type for example t2_9234nlk
     * @param direction -1 or 1
     * @return boolean true if vote succeed false otherwise
     */
    public boolean vote(String id, String name, String direction){

        boolean result = false;

        try {
            String modhash = getModhash();
            //Log.i("aaa", name + " " + direction + " " + modhash);
            Response vote = mService.vote(modhash, direction, name);
            //Log.i("aaa", readRetrofitResponse(vote));
            //Log.i("aaa", "status : " + vote.getStatus());
            result = vote.getStatus() == 200;
        } catch (RetrofitError error){
            error.printStackTrace();
            return false;
        }

        // So now we need to update our database
        mContext.getContentResolver().update(OKRedditContract.PostEntry.buildVotePostUri(id, direction), null, null, null);

        return result;
    }

    /**
     * Subscribe/unsubscribe reddit
     * @param id String id of the subreddit
     * @return boolean true if vote succeed false otherwise
     */
    public boolean subscribe(String id, String action){

        id = "t5_" + id;

        boolean result = false;

        try {
            String modhash = getModhash();
            Response subscribe = mService.subscribe(modhash, action, id);
            result = subscribe.getStatus() == 200;
        } catch (RetrofitError error){
            error.printStackTrace();
            return false;
        }

        return result;
    }

    /**
     * Reads input stream of retrofit response body into string.
     * @param response
     * @return String
     */
    private String readRetrofitResponse(Response response){
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(response.getBody().in()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}