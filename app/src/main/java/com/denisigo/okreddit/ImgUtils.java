package com.denisigo.okreddit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Images utils class handles tasks such as downloading and caching images etc.
 */
public class ImgUtils {

    private static final String POST_THUMBNAIL_FILENAME = "%s_thumb.jpg";

    /**
     * Returns URL for post thumbnail. It selects the best source for thumbnail based on post url.
     * @param postUrl
     * @param postThumbnailUrl
     * @return
     */
    private static URL getPostThumbnailUrl(String postUrl, String postThumbnailUrl){

        // Load thumbnail image
        String thumbUrl = null;
        // If we have url, see if we can get higher resolution image from there
        if (postUrl != null){
            // We support only imgur at the time, because another sources can send
            // us large pictures we don't want to load.
            // TODO: get some server proxy for image loading purposes

            // Check if it is from imgur and create thumbnail.
            if (ImgurUtils.isImgurFile(postUrl))
                thumbUrl = ImgurUtils.getThumbnailUrl(postUrl, ImgurUtils.THUMBNAIL_SIZE_M, "jpg");
        }

        // If we still don't have thumbnail url, get base reddit thumbnail
        if (thumbUrl == null)
            thumbUrl = postThumbnailUrl;

        URL url = null;
        try {
            url = new URL(thumbUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        return url;
    }

    /**
     * Gets post thumbnail image from cache or loads from internet
     * @param context
     * @param postId id of the post image belongs to
     * @param postUrl url of the post
     * @param thumbnailUrl url of the post standard thumbnail
     * @return Bitmap if image successfully retrieved from cache or loaded, null otherwise
     */
    public static Bitmap getPostThumbnail(Context context, String postId, String postUrl, String thumbnailUrl){

        // At first try to get image from cache
        String filename = String.format(POST_THUMBNAIL_FILENAME, postId);
        Bitmap bitmap = getCachedBitmap(context, filename);

        // If there is no image in cache, download it
        if (bitmap == null)
            bitmap = loadAndCachePostThumbnail(context, postId, postUrl, thumbnailUrl);

        return bitmap;
    }

    /**
     * Loads and caches post thumbnail
     * @param context
     * @param postId id of the post image belongs to
     * @param postUrl url of the post
     * @param thumbnailUrl url of the post standard thumbnail
     * @return
     */
    public static Bitmap loadAndCachePostThumbnail(Context context, String postId, String postUrl, String thumbnailUrl){
        // Reddit may provide odd value which means standard reddit face picture
        if (thumbnailUrl.equals("self"))
            return null;

        URL url = getPostThumbnailUrl(postUrl, thumbnailUrl);
        if (url != null) {
            String filename = String.format(POST_THUMBNAIL_FILENAME, postId);
            return loadAndCacheBitmap(context, filename, url);
        }
        return null;
    }

    /**
     * Loads and caches image in local storage
     * @param context
     * @param filename
     * @param url
     * @return
     */
    private static Bitmap loadAndCacheBitmap(Context context, String filename, URL url){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // If image downloaded successfully, cache it
        cacheBitmap(context, bitmap, filename);

        return bitmap;
    }

    /**
     * Tries to get cached image
     * @param context
     * @param filename
     * @return Bitmap if found, null otherwise
     */
    private static Bitmap getCachedBitmap(Context context, String filename){
        File file = new File(getCacheDir(context), filename);
        if (file != null && file.exists()) {
            return BitmapFactory.decodeFile(file.toString());
        }

        return null;
    }

    public static File getPostThumbnailFile(Context context, String postId){
        String filename = String.format(POST_THUMBNAIL_FILENAME, postId);
        return new File(getCacheDir(context), filename);
    }

    private static File getCacheDir(Context context){
        File cacheDir = context.getCacheDir();
        cacheDir = new File(cacheDir, "thumbs");
        if (!cacheDir.exists())
            cacheDir.mkdir();
        return cacheDir;
    }

    /**
     * Saves image to cache
     * @param context
     * @param bitmap
     * @param filename
     */
    private static void cacheBitmap(Context context, Bitmap bitmap, String filename){
        OutputStream fOut = null;
        File file = new File(getCacheDir(context), filename);
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
        }
    }

    /**
     * Clears images cache.
     */
    public static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
