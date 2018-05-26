package org.quelea.mobileremote.network;

import android.app.ProgressDialog;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.utils.UtilsMisc;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class to download text from the server.
 * @author Arvid
 */

public class DownloadHandler {
    private static int connectionFailedMR;
    public static boolean latestVersion;

    public static void downloadHtml(final UtilsMisc.DownloadHtmlModes mode, final String ip, final MainActivity activity) {
        new MainActivity.DownloadHtml() {
            @Override
            protected void onPostExecute(String line) {
                if (activity == null || line == null) return;

                activity.setLine(line);

                // Check if Quelea seems to be fully started
                if (!(mode.equals(UtilsMisc.DownloadHtmlModes.STARTED) && (line.equals(" ")))) {
                    if (mode.equals(UtilsMisc.DownloadHtmlModes.STARTED)) {
                        activity.setFullyStarted(true);
                    }

                    // Check if connection has failed and if that's true for 8 times in a row, alert user that the connection has failed
                    if (line.equals(" ")) {
                        connectionFailedMR++;
                    } else {
                        connectionFailedMR = 0;
                        activity.setOnline(true);
                    }
                    if (connectionFailedMR == 8)
                        activity.lostConnection();
                }

                // Handle the html differently based on what page has been downloaded
                switch (mode) {
                    case SCHEDULE:
                        activity.getParseDownloadedTextHelper().setSchedule(line);
                        break;
                    case LYRICS:
                        activity.getParseDownloadedTextHelper().setLyrics(line);
                        break;
                    case STATUS:
                        System.out.println(line);
                        activity.getParseDownloadedTextHelper().handleButtonStatus(line);
                        break;
                    case CHECK:
                        activity.getParseDownloadedTextHelper().checkURL(line);
                        break;
                    case SEARCH:
                        activity.getParseDownloadedTextHelper().getSongSearchResult(line);
                        break;
                    case BIBLE:
                        activity.getParseDownloadedTextHelper().getBibleAddResult(line);
                        break;
                    case BOOKS:
                        activity.getParseDownloadedTextHelper().getBibleBooks(line);
                        break;
                    case TRANSLATIONS:
                        activity.getParseDownloadedTextHelper().getBibleTranslations(line);
                        break;
                    case GETTHEMES:
                        activity.getDialogsHelper().selectThemeDialog(line, activity);
                        break;
                    case SUPPORTED:
                        if (line.contains("<!DOCTYPE html>")) {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    activity.getDialogsHelper().infoDialog(activity.getString(R.string.msg_not_supported), activity);
                                }
                            });
                        }
                        break;
                    case SLIDES:
                        latestVersion = !line.contains("<!DOCTYPE html>");
                        if (activity.isLatestVersion()) {
                            activity.setPresentation(true);
                            activity.getLyricsAdapter().notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        }.execute(ip);
    }

    public static void downloadWithProgress(final MainActivity.LoadWithProgressModes mode, final String ip, final MainActivity mainActivity) {
        try {
            final URL url = new URL(ip);
            new MainActivity.DownloadHtml() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (mainActivity == null) return;
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            final ProgressDialog progressDialog = mainActivity.getProgressDialog();
                            for (ProgressDialog pd : mainActivity.getProgressDialogs()) {
                                pd.dismiss();
                            }
                            if (mode.equals(MainActivity.LoadWithProgressModes.AUTO))
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_trying_to_find_server_automatically));
                            if (mode.equals(MainActivity.LoadWithProgressModes.TRANSLATIONS))
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_getting_bible_translations));
                            if (mode.equals(MainActivity.LoadWithProgressModes.BIBLE))
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_trying_to_add_passage));
                            if (mode.equals(MainActivity.LoadWithProgressModes.CHECK) && !mainActivity.getSettings()
                                    .isUseAutoConnect())
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_checking_stored_url));
                            if (mode.equals(MainActivity.LoadWithProgressModes.SEARCHINDIALOG)
                                    || mode.equals(MainActivity.LoadWithProgressModes.GETSONG))
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_searching));
                            if (mode.equals(MainActivity.LoadWithProgressModes.BOOKS))
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_getting_bible_books));
                            if (mode.equals(MainActivity.LoadWithProgressModes.CHECK) && mainActivity.getSettings()
                                    .isUseAutoConnect())
                                progressDialog.setMessage(mainActivity.getResources().getString(
                                        R.string.msg_checking_stored_url_and_auto_connecting));
                            progressDialog.setIndeterminate(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setCancelable(false);
                            if (!progressDialog.isShowing() && !mode.equals(MainActivity.LoadWithProgressModes.SEARCH))
                                progressDialog.show();
                        }
                    });
                }

                @Override
                protected void onPostExecute(String line) {
                    if (mainActivity == null || line == null) return;
                    switch (mode) {
                        case CHECK:
                            mainActivity.getParseDownloadedTextHelper().checkURL(line);
                            break;
                        case SEARCH:
                            mainActivity.getParseDownloadedTextHelper().getSongSearchResult(line);
                            break;
                        case GETSONG:
                            mainActivity.getParseDownloadedTextHelper().getSongSearchResult(line);
                            break;
                        case BIBLE:
                            mainActivity.getParseDownloadedTextHelper().getBibleAddResult(line);
                            break;
                        case BOOKS:
                            mainActivity.getParseDownloadedTextHelper().getBibleBooks(line);
                            break;
                        case TRANSLATIONS:
                            mainActivity.getParseDownloadedTextHelper().getBibleTranslations(line);
                            break;
                        case AUTO:
                            mainActivity.extractURLFromText(line, url);
                            break;
                        case SEARCHINDIALOG:
                            mainActivity.getDialogsHelper().showResultInDialog(mainActivity);
                            break;
                        default:
                            break;
                    }
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            for (ProgressDialog pd : mainActivity.getProgressDialogs()) {
                                pd.dismiss();
                            }
                        }
                    });
                }

                @Override
                protected void onCancelled() {
                    if (mainActivity == null) return;
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            for (ProgressDialog pd : mainActivity.getProgressDialogs()) {
                                pd.dismiss();
                            }
                        }
                    });
                    super.onCancelled();
                }
            }.execute(ip);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
