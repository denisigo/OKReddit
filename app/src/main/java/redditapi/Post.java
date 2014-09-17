package redditapi;

public class Post {
    private String domain;
    //private ? banned_by;
    //private ? media_embed;
    private String subreddit;
    private String selftext_html;
    private String selftext;
    //private ? likes;
    //private ? secure_media;
    //private String link_flair_text;
    private String id;
    private int gilded;
    //private ? secure_media_embed;
    private boolean clicked;
    //private ? report_reasons;
    private String author;
    //private ? media;
    private int score;
    //private ? approved_by;
    private boolean over_18;
    private boolean hidden;
    private String thumbnail;
    private String subreddit_id;
    private boolean edited;
    //private ? link_flair_css_class;
    //private ? author_flair_css_class;
    private int downs;
    private boolean saved;
    private boolean is_self;
    private String name;
    private String permalink;
    private boolean sticked;
    private long created;
    private String url;
    //private String author_flair_text;
    private String title;
    private long created_utc;
    private int ups;
    private int num_comments;
    private boolean visited;
    //private ? num_reports;
    //private ? distinguished;


    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getSubredditId() {
        return subreddit_id;
    }

    public String getDomain() {
        return domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getSelftextHtml() {
        return selftext_html;
    }

    public String getSelftext() {
        return selftext;
    }

    public int getGilded() {
        return gilded;
    }

    public boolean isClicked() {
        return clicked;
    }

    public String getAuthor() {
        return author;
    }

    public int getScore() {
        return score;
    }

    public boolean isOver18() {
        return over_18;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public boolean isEdited() {
        return edited;
    }

    public int getDowns() {
        return downs;
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean isSelf() {
        return is_self;
    }

    public String getName() {
        return name;
    }

    public String getPermalink() {
        return permalink;
    }

    public boolean isSticked() {
        return sticked;
    }

    public long getCreated() {
        return created;
    }

    public String getTitle() {
        return title;
    }

    public long getCreatedUtc() {
        return created_utc;
    }

    public int getUps() {
        return ups;
    }

    public int getNumComments() {
        return num_comments;
    }

    public boolean isVisited() {
        return visited;
    }
}