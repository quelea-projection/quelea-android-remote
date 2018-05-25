package org.quelea.mobileremote.cache;

/*
 * File Cache class
 */

import android.content.Context;

import java.io.File;

public class FileCache {

    private File cacheDir;

    FileCache(Context context) {

        cacheDir = context.getCacheDir();

        if (!cacheDir.exists()) {
            // create cache dir in your application context
            cacheDir.mkdirs();
        }
    }

    File getFile(String url) {
        //Identify images by hashcode or encode by URLEncoder.encode.
        String filename = String.valueOf(url.hashCode());

        return new File(cacheDir, filename);

    }

    public void clear() {
        // list all files inside cache directory
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;

        //delete all cache directory files
        for (File f : files) {
            f.delete();
        }
    }

}