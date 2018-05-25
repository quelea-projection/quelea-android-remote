package org.quelea.mobileremote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.quelea.mobileremote.activities.MainActivity;

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

import static org.quelea.mobileremote.utils.UtilsMisc.HIDE;
import static org.quelea.mobileremote.utils.UtilsMisc.NEXT_ITEM;
import static org.quelea.mobileremote.utils.UtilsMisc.NEXT_SLIDE;
import static org.quelea.mobileremote.utils.UtilsMisc.PREVIOUS_ITEM;
import static org.quelea.mobileremote.utils.UtilsMisc.PREVIOUS_SLIDE;

/**
 * Receiver to handle actions from a notification remote view. NB: Not finished!
 * Created by Arvid on 2018-04-25.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case NEXT_ITEM:
                    new LoadInBackground().execute(MainActivity.settingsHelper.getIp() + "/nextitem");
                    break;
                case NEXT_SLIDE:
                    new LoadInBackground().execute(MainActivity.settingsHelper.getIp() + "/next");
                    break;
                case PREVIOUS_ITEM:
                    new LoadInBackground().execute(MainActivity.settingsHelper.getIp() + "/previtem");
                    break;
                case PREVIOUS_SLIDE:
                    new LoadInBackground().execute(MainActivity.settingsHelper.getIp() + "/prev");
                    break;
                case HIDE:
                    new LoadInBackground().execute(MainActivity.settingsHelper.getIp() + "/tlogo");
                    break;
            }
        }
    }
    @SuppressWarnings("deprecation")
    // Class to handle the links in the background (e.g. http://ip/next)
    static class LoadInBackground extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... uri) {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 800);
            HttpConnectionParams.setSoTimeout(params, 800);
            HttpClient httpclient = new DefaultHttpClient(params);
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (IOException e) {
                Log.e("Load in background",
                        "An error occurred when loading in background: " + e);
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }
}
