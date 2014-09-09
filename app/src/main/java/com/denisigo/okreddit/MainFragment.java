package com.denisigo.okreddit;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.HttpCookie;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public class MainFragment extends Fragment {

    private String sessionid = "";

    private static final String API_URL = "https://api.github.com";

    static class Contributor {
        String login;
        int contributions;
    }

    interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        List<Contributor> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo
        );
    }



    private class ListingData<T>{
        //String modhash;
        //List<T> children;
    }

    private static class Listing{
        String kind;
        //transient ListingData data;
    }

    private class Subreddit{

    }

    public interface RedditService {
        @GET("/subreddits/mine/subscriber.json")
        Listing mineSubreddits();

        @FormUrlEncoded
        @POST("/api/login")
        Response login(@Field("api_type") String api_type,
                   @Field("passwd") String passwd,
                   @Field("rem") Boolean rem,
                   @Field("user") String user);
    }



    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ggg gg = new ggg();
        gg.execute();



        return rootView;
    }


    private class ggg extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {


            RequestInterceptor requestInterceptor = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Cookie", "reddit_session="+sessionid);
                }
            };


            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://www.reddit.com/")
                    .setRequestInterceptor(requestInterceptor)
                    .build();




            RedditService service = restAdapter.create(RedditService.class);

            Response login = service.login("json", "x0Qs*%y$Uv", true, "denisigo");

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


            Listing subreddits = service.mineSubreddits();
            Log.i("aaa", subreddits.kind);



            /*
            // Create a very simple REST adapter which points the GitHub API endpoint.
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API_URL)
                    .build();

            // Create an instance of our GitHub API interface.
            GitHub github = restAdapter.create(GitHub.class);

            // Fetch and print a list of the contributors to this library.
            List<Contributor> contributors = github.contributors("square", "retrofit");
            for (Contributor contributor : contributors) {
                System.out.println(contributor.login + " (" + contributor.contributions + ")");
            }
            */


            return null;
        }
    }
}
