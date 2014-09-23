package redditapi;

public class Subreddit{
    public String id;
    public String display_name;
    public String header_img;
    public String title;
    public int subscribers;
    public String url;
    public String public_description;
    public boolean user_is_subscriber;
    public boolean user_is_moderator;
    public boolean user_is_banned;

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