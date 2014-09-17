package redditapi;

import android.util.Log;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.Response;

public class RedditAPI{

    private static final String API_ENDPOINT = "https://www.reddit.com/";
    private String sessionid = "";

    private RedditService mService;

    public RedditAPI(){

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Cookie", "reddit_session="+sessionid);
                request.addHeader("User-Agent", "OK Reddit Android/1.0 by denisigo");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT)
                .setRequestInterceptor(requestInterceptor)
                .build();

        mService = restAdapter.create(RedditService.class);



    }

    public void login(String user, String passwd, boolean remember){
        Response login = mService.login("json", passwd, remember, user);

        List<Header> headers = login.getHeaders();
        Log.i("aaa", Integer.toString(login.getStatus()));
        for (Header header : headers) {
            if (header.getName() != null && header.getName().contains("Set-Cookie")) {
                Log.i("aaa", header.toString());
                List<HttpCookie> cookies = HttpCookie.parse(header.getValue());
                for (HttpCookie cookie : cookies) {
                    if (cookie.getName().contains("reddit_session")) {
                        sessionid = cookie.getValue();
                        Log.i("aaa", sessionid);
                    }
                }
            }
        }
    }

    public List<Subreddit> mineSubreddits(){
        Listing subreddits = mService.mineSubreddits();

        List<Subreddit> result = new ArrayList<Subreddit>(0);
        for (ItemWrapper<Subreddit> wrapper : ((Listing.ListingData<ItemWrapper<Subreddit>>)subreddits.data).children)
            result.add(wrapper.data);

        return result;
    }

    public List<Post> posts(){
        Listing posts = mService.posts();

        List<Post> result = new ArrayList<Post>(0);
        for (ItemWrapper<Post> wrapper : ((Listing.ListingData<ItemWrapper<Post>>)posts.data).children)
            result.add(wrapper.data);

        return result;
    }

}