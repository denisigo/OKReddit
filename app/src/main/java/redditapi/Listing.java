package redditapi;

import java.util.List;

public class Listing<T>{
    String kind;
    ListingData<T> data;

    public static class ListingData<T>{
        String modhash;
        String before;
        String after;
        List<T> children;
    }
}
