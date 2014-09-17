package com.denisigo.okreddit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Images utils class handles tasks such as downloading and caching images etc.
 */
public class ImgUtils {

    /**
     * Gets post thumbnail image from cache or loads from internet
     * @param context
     * @param postId id of the post image belongs to
     * @param url url of the image to download
     * @return Bitmap if image successfully retrieved from cache or loaded, null otherwise
     */
    public static Bitmap getPostThumbnail(Context context, String postId, URL url){

        String filename = postId + "_thumb.jpg";

        // At first try to get image from cache
        Bitmap bitmap = getCachedBitmap(context, filename);

        // If there is no image in cache, download it
        if (bitmap == null){
            try {
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
            }

            // If image downloaded successfully, cache it
            if (bitmap != null)
                cacheBitmap(context, bitmap, filename);
        }

        return bitmap;
    }

    /**
     * Tries to get cached image
     * @param context
     * @param filename
     * @return Bitmap if found, null otherwise
     */
    private static Bitmap getCachedBitmap(Context context, String filename){
        String cacheDir = context.getCacheDir().toString();
        String path = cacheDir + "/" + filename;
        return BitmapFactory.decodeFile(path);
    }

    /**
     * Saves image to cache
     * @param context
     * @param bitmap
     * @param filename
     */
    private static void cacheBitmap(Context context, Bitmap bitmap, String filename){
        String cacheDir = context.getCacheDir().toString();

        OutputStream fOut = null;
        File file = new File(cacheDir, filename);
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
