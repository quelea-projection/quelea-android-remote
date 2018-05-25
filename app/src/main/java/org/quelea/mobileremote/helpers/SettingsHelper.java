package org.quelea.mobileremote.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class to get and store shared preferences.
 * Created by Arvid on 2017-11-29.
 */

@SuppressWarnings("unused")
public class SettingsHelper {
    private boolean useVolume;
    private boolean useDpad;
    private String autoProgress = "";
    private String doublePress = "";
    private String longClick = "";
    private String useSwipe;
    private boolean useAutoConnect;
    private String autoTimeout;
    private boolean disableRec = true;
    private String ip = "http://192.168.0.2:1112";
    private String theme;
    private SharedPreferences sharedPrefs;
    private int translationQuestionDelay = 0;
    private boolean showTranslationQuestion = true;

    public boolean isUseVolume() {
        return useVolume;
    }

    public boolean isUseDpad() {
        return useDpad;
    }

    public String getAutoProgress() {
        return autoProgress;
    }

    public String getDoublePress() {
        return doublePress;
    }

    public String getLongClick() {
        return longClick;
    }

    public String getUseSwipe() {
        return useSwipe;
    }

    public boolean isUseAutoConnect() {
        return useAutoConnect;
    }

    public String getAutoTimeout() {
        return autoTimeout;
    }

    boolean isDisableRec() {
        return disableRec;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getTranslationQuestionDelay() {
        return translationQuestionDelay;
    }

    public void setTranslationQuestionDelay(int translationQuestionDelay) {
        this.translationQuestionDelay = translationQuestionDelay;
    }

    public boolean isShowTranslationQuestion() {
        return showTranslationQuestion;
    }

    public void setShowTranslationQuestion(boolean showTranslationQuestion) {
        this.showTranslationQuestion = showTranslationQuestion;
    }

    public void loadSettings(Context context) {
         sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        // Get navigation settingsHelper
        useVolume = sharedPrefs.getBoolean("volume_buttons", false);
        useDpad = sharedPrefs.getBoolean("dpad_buttons", false);
        autoProgress = sharedPrefs.getString("on_end", "null");
        doublePress = sharedPrefs.getString("double_press", "null");
        longClick = sharedPrefs.getString("long_press", "null");
        useSwipe = sharedPrefs.getString("swipe_navigation", "null");

        // Get auto-connect settingsHelper
        useAutoConnect = sharedPrefs.getBoolean("autoConnect", true);
        autoTimeout = sharedPrefs.getString("autoTimeout", "200");

        // Get general settingsHelper
        disableRec = sharedPrefs.getBoolean("disableRec", false);

        // Get stored IP
        ip = sharedPrefs.getString("urlMR", "http://192.168.0.2:1112");

        if (ip.contains("0.0.0.0"))
            ip = "http://192.168.0.2:1112";

        // Check if http:// needs to be added to the URL
        if (ip.length() > 7) {
            if (!(ip.substring(0, 7).equals("http://")))
                ip = "http://" + ip;
            ip = ip.replaceAll(" ", "");
        }

        theme = sharedPrefs.getString("app_theme", "0");

        // Translation questions
        translationQuestionDelay = sharedPrefs.getInt("translationQuestionDelay", 0);
        showTranslationQuestion = sharedPrefs.getBoolean("showTranslationQuestion", true);
    }

    public void saveSetting(String id, String value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(id, value);
        editor.apply();
    }

    public void saveSetting(String id, int value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(id, value);
        editor.apply();
    }

    public void saveSetting(String id, boolean value) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(id, value);
        editor.apply();
    }

}
