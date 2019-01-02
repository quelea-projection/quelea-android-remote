package org.quelea.mobileremote.network;

import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.utils.UtilsMisc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;

import static org.quelea.mobileremote.activities.MainActivity.settingsHelper;
import static org.quelea.mobileremote.network.DownloadHandler.downloadHtml;
import static org.quelea.mobileremote.network.DownloadHandler.downloadWithProgress;

/**
 * Class to handle all Input/Output in connection with the server.
 * Methods are static to be called with MainActivity as context.
 */
public class ServerIO {
    public static void downloadLyrics(MainActivity activity) {
        downloadHtml(UtilsMisc.DownloadHtmlModes.LYRICS, settingsHelper.getIp() + "/lyrics", activity);
    }

    public static void checkServerConnection(MainActivity mainActivity, String ip) {
        downloadWithProgress(MainActivity.LoadWithProgressModes.CHECK,
                ip, mainActivity);
    }

    public static void getTranslations(MainActivity mainActivity) {
        downloadWithProgress(MainActivity.LoadWithProgressModes.TRANSLATIONS,
                settingsHelper.getIp() + "/translations", mainActivity);
    }

    public static void getSong(String song, MainActivity mainActivity) {
        downloadWithProgress(MainActivity.LoadWithProgressModes.GETSONG,
                settingsHelper.getIp() + song, mainActivity);
    }

    public static void downloadBibleBooks(MainActivity mainActivity) {
        downloadWithProgress(MainActivity.LoadWithProgressModes.BOOKS, settingsHelper.getIp()
                + "/books/" + mainActivity.getBibleTranslation(), mainActivity);
    }

    public static void nextSlide(MainActivity mainActivity) {
        ServerIO.loadInBackground(settingsHelper.getIp() + "/next", mainActivity);
        ServerIO.downloadLyrics(mainActivity);
    }

    public static void nextItem(final MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/nextitem", mainActivity);
        nextItemAnimation(mainActivity);
        mainActivity.setActiveVerse(0);
        mainActivity.setVerseTotal(0);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ServerIO.downloadLyrics(mainActivity);
                mainActivity.getLyricsAdapter().notifyDataSetChanged();
            }
        }, 300);
    }

    public static void prevSlide(MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/prev", mainActivity);
        DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.LYRICS, settingsHelper.getIp() + "/lyrics", mainActivity);
    }

    public static void prevItem(final MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/previtem", mainActivity);
        prevItemAnimation(mainActivity);
        mainActivity.setActiveVerse(0);
        mainActivity.setVerseTotal(0);
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.LYRICS, settingsHelper.getIp() + "/lyrics", mainActivity);
                mainActivity.getLyricsAdapter().notifyDataSetChanged();
            }
        }, 300);
    }

    public static void clear(MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/clear", mainActivity);
    }

    public static void black(MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/black", mainActivity);
    }

    public static void logo(MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + "/tlogo", mainActivity);
    }

    public static void downloadStatus(UtilsMisc.DownloadHtmlModes check, MainActivity mainActivity) {
        DownloadHandler.downloadHtml(check, settingsHelper.getIp() + "/status", mainActivity);
    }

    public static void setTheme(String theme, MainActivity mainActivity) {
        loadInBackground(mainActivity.getSettings().getIp() + "/settheme/" + theme, mainActivity);
    }

    public static void checkSupported(String url, MainActivity mainActivity) {
        DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.SUPPORTED, url, mainActivity);
    }


    public static void loadSong(String songNumber, MainActivity mainActivity) {
        loadInBackground(settingsHelper.getIp() + songNumber, mainActivity);
    }

    public static void sendPassword(String password, MainActivity mainActivity) {
        new MainActivity.SendPassword(mainActivity).execute(password);
    }

    public static void gotoItem(final int newItem, final int oldItem, final MainActivity mainActivity) {
        if (oldItem > newItem) {
            prevItemAnimation(mainActivity);
        } else if (oldItem < newItem) {
            nextItemAnimation(mainActivity);
        }
        new MainActivity.DownloadHtml() {
            @Override
            protected void onPostExecute(String line) {
                if (line.contains("<!DOCTYPE html>")) {
                    if (oldItem < newItem) {
                        for (int i = oldItem; i < (newItem); i++)
                            ServerIO.nextItem(mainActivity);
                    } else {
                        for (int i = newItem; i < (oldItem); i++)
                            ServerIO.prevItem(mainActivity);
                    }
                    mainActivity.setCanJump(false);
                }
            }
        }.execute(settingsHelper.getIp() + "/gotoitem" + newItem);
    }

    public static void downloadSchedule(MainActivity mainActivity) {
        DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.SCHEDULE, settingsHelper.getIp() + "/schedule", mainActivity);
    }

    public static void downloadSlides(MainActivity mainActivity) {
        DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.SLIDES, settingsHelper.getIp() + "/slides", mainActivity);
    }

    public static void getThemes(MainActivity mainActivity) {
        DownloadHandler.downloadHtml(UtilsMisc.DownloadHtmlModes.GETTHEMES, settingsHelper.getIp() + "/getthemes", mainActivity);
    }

    private static void nextItemAnimation(MainActivity mainActivity) {
        final Animation nextAnim = AnimationUtils
                .loadAnimation(mainActivity, R.anim.next);
        mainActivity.getLyricsListView().startAnimation(nextAnim);
        mainActivity.setSlide(true);
    }


    private static void prevItemAnimation(MainActivity mainActivity) {
        final Animation previousAnim = AnimationUtils
                .loadAnimation(mainActivity, R.anim.previous);
        mainActivity.getLyricsListView().startAnimation(previousAnim);
        mainActivity.setSlide(true);
    }

    public static void loadWithProgressDialog(MainActivity mainActivity, MainActivity.LoadWithProgressModes mode, String ip) {
        DownloadHandler.downloadWithProgress(mode, ip, mainActivity);
    }

    @SuppressWarnings("deprecation")
    public static void loadInBackground(final String ip, final MainActivity activity) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(params, 2800);
                HttpConnectionParams.setSoTimeout(params, 2800);
                HttpClient httpclient = new DefaultHttpClient(params);
                HttpResponse response;
                try {
                    response = httpclient.execute(new HttpGet(ip));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                    } else {
                        // Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (IOException e) {
                    Log.e("Load in background",
                            "An error occurred when loading in background: " + e);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.msg_failed_sending, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }
}
