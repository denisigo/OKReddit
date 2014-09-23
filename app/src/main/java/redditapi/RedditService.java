package redditapi;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RedditService {

    static final String HEADER_MODHASH = "X-Modhash";


    @GET("/subreddits/mine/subscriber.json")
    Listing<ItemWrapper<Subreddit>> mineSubreddits(
            @Query("after") String after,
            @Query("limit") String limit
    );

    @GET("/subreddits/popular.json")
    Listing<ItemWrapper<Subreddit>> popularSubreddits(
            @Query("after") String after,
            @Query("limit") String limit
    );

    @GET("/hot.json")
    Listing<ItemWrapper<Post>> posts(
            @Query("after") String after,
            @Query("limit") String limit);

    @GET("/r/{subredditDisplayName}/hot.json")
    Listing<ItemWrapper<Post>> subredditPosts(
            @Path("subredditDisplayName") String subredditDisplayName,
            @Query("after") String after,
            @Query("limit") String limit);

    @FormUrlEncoded
    @POST("/api/login")
    Response login(@Field("api_type") String api_type,
                   @Field("passwd") String passwd,
                   @Field("rem") Boolean rem,
                   @Field("user") String user);


    @FormUrlEncoded
    @POST("/api/vote")
    Response vote(@Header(HEADER_MODHASH) String modhash,
                   @Field("dir") String dir,
                   @Field("id") String id);

    @FormUrlEncoded
    @POST("/api/subscribe")
    Response subscribe(@Header(HEADER_MODHASH) String modhash,
                  @Field("action") String action,
                  @Field("sr") String sr);

    @GET("/api/me.json")
    Item<Account> me();
}
