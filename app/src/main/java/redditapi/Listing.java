package redditapi;

import java.util.List;

public class Listing<T>{
    public String kind;
    public ListingData<T> data;

    public static class ListingData<T>{
        public String modhash;
        public String before;
        public String after;
        public List<T> children;
    }
}
