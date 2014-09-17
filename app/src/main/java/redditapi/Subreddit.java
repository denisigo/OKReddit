package redditapi;

public class Subreddit{
    private String id;
    private String display_name;
    private String header_img;
    private String title;
    private int subscribers;
    private String url;
    private String public_description;
    private boolean user_is_subscriber;
    private boolean user_is_moderator;
    private boolean user_is_banned;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return display_name;
    }

    public String getHeaderImg() {
        return header_img;
    }

    public String getTitle() {
        return title;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicDescription() {
        return public_description;
    }

    public boolean isUserSubscriber() {
        return user_is_subscriber;
    }

    public boolean isUserModerator() {
        return user_is_moderator;
    }

    public boolean isUserBanned() {
        return user_is_banned;
    }
}