package redditapi;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RedditService {


    /*
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
    */


    @GET("/subreddits/mine/subscriber.json")
    Listing<ItemWrapper<Subreddit>> mineSubreddits();

    @GET("/hot.json")
    Listing<ItemWrapper<Post>> posts();

    @FormUrlEncoded
    @POST("/api/login")
    Response login(@Field("api_type") String api_type,
                   @Field("passwd") String passwd,
                   @Field("rem") Boolean rem,
                   @Field("user") String user);
}
