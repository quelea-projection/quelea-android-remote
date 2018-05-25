package org.quelea.mobileremote.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Utilities with global variables and static methods.
 * Created by Arvid on 2016-06-17.
 */
public class UtilsMisc {

    public static final String NEXT_ITEM = "NEXT_ITEM";
    public static final String NEXT_SLIDE = "NEXT_SLIDE";
    public static final String PREVIOUS_SLIDE = "PREVIOUS_SLIDE";
    public static final String PREVIOUS_ITEM = "PREVIOUS_ITEM";
    public static final String HIDE = "HIDE";

    public enum DownloadHtmlModes {
        STARTED,
        SCHEDULE,
        LYRICS,
        STATUS,
        CHECK,
        SEARCH,
        BIBLE,
        BOOKS,
        TRANSLATIONS,
        GETTHEMES,
        SUPPORTED,
        SLIDES
    }

    public enum TranslationProgress {
        FINISHED,
        PARTIAL,
        NONE
    }


    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                //Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (Exception ignored) {
        }
    }


    public static StringBuilder readAssets(String file, Context context) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder("");
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(file), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                stringBuilder.append(mLine).append("\n");
            }
        } catch (IOException e) {
            Log.e("Read assets", "File not found");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("Read assets", "Failed closing file");
                }
            }
        }
        return stringBuilder;
    }

    public static int getBackgroundColor(Activity mContext) {
        TypedValue a = new TypedValue();
        int color = 0;
        mContext.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            color = a.data;
        }
        return color;
    }

    public static void writeToFile(String filePath, String dataToWrite) {
        try {
            File myFile = new File(filePath);
            FileOutputStream out = new FileOutputStream(myFile);
            out.write(dataToWrite.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            Log.e("WriteToFile", "File not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e("WriteToFile", "Failed writing to file: " + e.getLocalizedMessage());
        }
    }

    public static String readFromFile(String file) {

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(new File(file)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            Log.e("ReadFromFile", "File not found: " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.e("ReadFromFile", "Failed reading file: " + e.getLocalizedMessage());
        }

        return stringBuilder.toString();
    }

}