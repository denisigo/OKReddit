package com.denisigo.okreddit;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by denisigo on 16.09.14.
 */
public class ImgurUtils {

    public static Character THUMBNAIL_SIZE_S = 's';
    public static Character THUMBNAIL_SIZE_B = 'b';
    public static Character THUMBNAIL_SIZE_T = 't';
    public static Character THUMBNAIL_SIZE_M = 'm';
    public static Character THUMBNAIL_SIZE_L = 'l';
    public static Character THUMBNAIL_SIZE_H = 'h';

    private static Character[] THUMBNAIL_SIZES= {THUMBNAIL_SIZE_S, THUMBNAIL_SIZE_B,
            THUMBNAIL_SIZE_T, THUMBNAIL_SIZE_M, THUMBNAIL_SIZE_L, THUMBNAIL_SIZE_H};

    /**
     * Just checks whether URL belongs to imgur or not
     * @param url of the file
     * @return true or false
     */
    public static boolean isImgurFile(String url){
        return url.contains("imgur.com");
    }

    /**
     * Generates imgur thumbnail URL from provided URL with given size.
     * https://api.imgur.com/models/image
     * @param url base image URL
     * @param ext preferable extension if there is no one
     * @return url of the thumbnail
     */
    public static String getThumbnailUrl(String url, Character size, String ext){

        String thumbUrl = null;

        // If there is extension, try to append or change thumbnail size modificator to `size`.
        if (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif")){
            // Well, not the best solution I think....
            List<String> parts = new LinkedList<String>(Arrays.asList(url.split("\\.")));
            String extension = parts.remove(parts.size()-1);
            String filename = TextUtils.join(".", parts);
            Character lastChar = filename.charAt(filename.length()-1);

            // WTF, imgur can have filenames with ending characters like size modifiers,
            // but not modifiers! And replacing them with another one makes picture unavailable.
            // How they think I should to distinguish modifiers them from filename??
            // By lenght of filename maybe? But at the moment turn this off, lets suppose
            // that all url we get are without size modifiers.
            /*
            // If there is modificator, change it
            List<Character> modificators = Arrays.asList(THUMBNAIL_SIZES);
            if (modificators.contains(lastChar)){
                StringBuilder myName = new StringBuilder(filename);
                myName.setCharAt(filename.length()-1, size);
                filename = myName.toString();
            } else {
                // If there is no modificator, add it
                filename = filename + size;
            }
            */
            filename = filename + size;

            // Restore filename
            thumbUrl = filename + "." + extension;
        } else {
            // If it is not an album
            if (!url.contains("/a/") && !url.contains("/gallery/")) {
                // URLs without extension lead to main site, so add `i.` to lead to image
                url = url.replace("imgur.com", "i.imgur.com");
                // We add some extension because we don't know what extension file has actually
                // and we don't want to make another API call to figure it out
                thumbUrl = url + size + "." + ext;
            }
        }
        return thumbUrl;
    }
}
