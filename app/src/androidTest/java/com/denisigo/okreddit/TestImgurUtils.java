package com.denisigo.okreddit;

import android.test.AndroidTestCase;

public class TestImgurUtils extends AndroidTestCase {


    public void testIsImgurFile() throws Throwable {
        assertTrue(ImgurUtils.isImgurFile("http://i.imgur.com/r7KfAIa.jpg"));
        assertTrue(ImgurUtils.isImgurFile("https://i.imgur.com/ZUgeXQz.jpg"));
        assertFalse(ImgurUtils.isImgurFile("https://youtube.com"));
    }

    public void testGetThumbnailUrl() throws Throwable{

        // No size modificator initially
        String urlFrom = "http://i.imgur.com/r7KfAIa.png";
        String urlTo = "http://i.imgur.com/r7KfAIas.png";
        String url = ImgurUtils.getThumbnailUrl(urlFrom, ImgurUtils.THUMBNAIL_SIZE_S, "jpg");
        assertEquals(urlTo, url);

        // `m` size modificator should be replaced with `s`
        /*
        urlFrom = "http://i.imgur.com/r7KfAIam.gif";
        urlTo = "http://i.imgur.com/r7KfAIas.gif";
        url = ImgurUtils.getThumbnailUrl(urlFrom, ImgurUtils.THUMBNAIL_SIZE_S, "jpg");
        assertEquals(urlTo, url);
        */

        // no extension, add size modificator and extension
        urlFrom = "http://imgur.com/r7KfAIa";
        urlTo = "http://i.imgur.com/r7KfAIas.jpg";
        url = ImgurUtils.getThumbnailUrl(urlFrom, ImgurUtils.THUMBNAIL_SIZE_S, "jpg");
        assertEquals(urlTo, url);

        // no extension, leads to album (/a/), we cannot handle
        urlFrom = "http://imgur.com/a/YQ1Jj";
        url = ImgurUtils.getThumbnailUrl(urlFrom, ImgurUtils.THUMBNAIL_SIZE_S, "jpg");
        assertEquals(null, url);


    }
}