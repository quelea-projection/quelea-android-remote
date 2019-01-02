package org.quelea.mobileremote.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.adapters.LyricsAdapter;
import org.quelea.mobileremote.adapters.ScheduleAdapter;
import org.quelea.mobileremote.crashHandling.CrashHandler;
import org.quelea.mobileremote.dialogs.DialogsHelper;
import org.quelea.mobileremote.helpers.NavigationHelper;
import org.quelea.mobileremote.helpers.ParseDownloadedTextHelper;
import org.quelea.mobileremote.helpers.SettingsHelper;
import org.quelea.mobileremote.network.DownloadHandler;
import org.quelea.mobileremote.network.ServerIO;
import org.quelea.mobileremote.network.SyncHandler;
import org.quelea.mobileremote.utils.UtilsMisc;
import org.quelea.mobileremote.utils.UtilsNetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Main class to control Quelea projection software using the Mobile Remote server.
 *
 * @author Arvid
 */

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check previously stored settings
        settingsHelper.loadSettings(this);

        initUI();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        context = MainActivity.this;

        // Set custom error manager
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(MainActivity.this));

        initButtons();

        checkTranslation();
    }

    private void initButtons() {
        // Start listening for buttons pressed
        navigationHelper = new NavigationHelper(this);
        navigationHelper.keyListener(lyricsListView);
        navigationHelper.buttonListeners();
        scheduleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                dialogsHelper.scheduleLongClickDialog(i, MainActivity.this);
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    private void initUI() {
        // Start setting theme
        if (settingsHelper.getTheme() != null && settingsHelper.getTheme().equals("1")) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.currentlyDisplaying);
        tv.setText(getResources().getString(R.string.msg_no_live_item));
        tv.setTypeface(Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf"), Typeface.NORMAL);
        if (getResources().getBoolean(R.bool.small_screen))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        else
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        // Setup lyrics view
        lyricsListView = findViewById(R.id.lyrics);
        lyricsAdapter = new LyricsAdapter(MainActivity.this, lyrics);
        lyricsListView.setAdapter(lyricsAdapter);

        // Start schedule drawer (left)
        initSidebarMenu();

        // Continue application theme setup
        if (settingsHelper.getTheme().equals("1")) {
            findViewById(R.id.logo).setBackgroundResource(R.drawable.btn_toggle_synced_dark);
            findViewById(R.id.black).setBackgroundResource(R.drawable.btn_toggle_synced_dark);
            findViewById(R.id.clear).setBackgroundResource(R.drawable.btn_toggle_synced_dark);
            findViewById(R.id.nextItem).setBackgroundResource(R.drawable.btn_navigation_dark);
            findViewById(R.id.nextSlide).setBackgroundResource(R.drawable.btn_navigation_dark);
            findViewById(R.id.previousItem).setBackgroundResource(R.drawable.btn_navigation_dark);
            findViewById(R.id.previousSlide).setBackgroundResource(R.drawable.btn_navigation_dark);
        } else {
            findViewById(R.id.logo).setBackgroundResource(R.drawable.btn_toggle_synced);
            findViewById(R.id.black).setBackgroundResource(R.drawable.btn_toggle_synced);
            findViewById(R.id.clear).setBackgroundResource(R.drawable.btn_toggle_synced);
            lyricsListView.setBackgroundResource(R.color.background);
            if (scheduleDrawerLayout != null)
                scheduleDrawerLayout.setBackgroundResource(R.color.background);
            else {
                findViewById(R.id.left_drawer).setBackgroundColor(UtilsMisc.getBackgroundColor(this));
            }
        }

        initButtons();
    }

    /**
     * Method to resume loading after e.g. checking permissions
     */
    public void continueLoading() {
        if (UtilsNetwork.checkWifiOnAndConnected(this)) {
            ServerIO.checkServerConnection(MainActivity.this, settingsHelper.getIp());
        } else {
            dialogsHelper.enterURLDialog(getString(R.string.troubleshoot_msg_check_wifi), settingsHelper.getIp(), this);
        }

        // Download lyrics and schedule if already online (e.g. screen rotate or
        // backing out and in again)
        if (online) {
            ServerIO.downloadLyrics(this);
            ServerIO.downloadSchedule(this);
        }

        // Start auto-refreshing; search for changes on slides, buttons or
        // schedule each 0.6 seconds
        if (syncHandler == null)
            syncHandler = new SyncHandler(MainActivity.this);
        syncHandler.startSync();

        parseDownloadedTextHelper = new ParseDownloadedTextHelper(this);
    }

    private void checkTranslation() {
        if (settingsHelper.isShowTranslationQuestion()) {
            UtilsMisc.TranslationProgress progress = TranslationActivity.checkTranslation(this);
            int qCount = settingsHelper.getTranslationQuestionDelay();
            switch (progress) {
                case PARTIAL:
                    if (qCount == 0 || qCount >= 4) {
                        if (!isDialogShown)
                            dialogsHelper.missingTranslationDialog(getString(R.string.msg_partially_translated), this);
                    } else {
                        settingsHelper.saveSetting("translationQuestionDelay", (qCount + 1));
                        continueLoading();
                    }
                    break;
                case NONE:
                    if (qCount == 0 || qCount >= 4) {
                        if (!isDialogShown)
                            dialogsHelper.missingTranslationDialog(
                                    String.format("Unfortunately the app is not translated to %s yet. This is most likely since no one has offered to do it yet.\n\nWould you like to help translating it?", Locale.getDefault().getDisplayLanguage()), this);
                    } else {
                        settingsHelper.saveSetting("translationQuestionDelay", (qCount + 1));
                        continueLoading();
                    }
            }
        } else {
            continueLoading();
        }
    }

    private void initSidebarMenu() {
        // Setup schedule drawer (left)
        scheduleListView = findViewById(R.id.left_drawer);
        scheduleAdapter = new ScheduleAdapter(this, scheduleList);
        scheduleListView.setAdapter(scheduleAdapter);

        mTitle = mDrawerTitle = getTitle();
        scheduleDrawerLayout = findViewById(R.id.drawer_layout);
        if (scheduleDrawerLayout != null) {
            scheduleDrawerToggle = new ActionBarDrawerToggle(this, scheduleDrawerLayout,
                    R.string.msg_navigation_drawer_open,
                    R.string.msg_navigation_drawer_close) {

                @Override
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    assert getSupportActionBar() != null;
                    getSupportActionBar().setTitle(mTitle);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    assert getSupportActionBar() != null;
                    getSupportActionBar().setTitle(mDrawerTitle);
                    scheduleListView = findViewById(R.id.left_drawer);
                }
            };

            scheduleDrawerLayout.addDrawerListener(scheduleDrawerToggle);
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Make schedule items clickable
        scheduleListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, final int position,
                                    long id) {
                scheduleListView.setItemChecked(position, true);
                if (scheduleDrawerLayout != null)
                    scheduleDrawerLayout.closeDrawers();
                if (canJump) {
                    ServerIO.gotoItem(position, activeItem, MainActivity.this);
                } else {
                    if (position > activeItem) {
                        for (int i = activeItem; i < (position); i++)
                            ServerIO.nextItem(MainActivity.this);
                    } else {
                        for (int i = 0; i < (activeItem - position); i++)
                            ServerIO.prevItem(MainActivity.this);
                    }
                }
                scheduleListView.requestFocus();
            }
        });

        // Make lyrics sections clickable
        lyricsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id) {
                Animation animation;
                animation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.blink);
                v.startAnimation(animation);
                lyricsListView.setItemChecked(position, true);
                if (playable) {
                    ServerIO.loadInBackground(settingsHelper.getIp() + "/play", MainActivity.this);
                } else if (!slide) {
                    ServerIO.loadInBackground(settingsHelper.getIp() + "/section" + position, MainActivity.this);
                    final Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ServerIO.downloadLyrics(MainActivity.this);
                            ServerIO.downloadSchedule(MainActivity.this);
                        }
                    }, 1000);
                }
            }
        });

        ImageView theme = findViewById(R.id.themeButton);
        theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerIO.getThemes(context);
            }
        });
    }

    public void initAutoConnect() {
        handleAutoIPResult(UtilsNetwork.autoConnect(this));
    }

    // Method to handle result from auto-connecting
    public void handleAutoIPResult(String autoIP) {
        if (autoIP.equals("")) {
            if (!isFinishing())
                dialogsHelper.enterURLDialog(getResources().getString(R.string.msg_no_server_found),
                        settingsHelper.getIp(), this);
        } else {
            String address = "http://" + autoIP + ":50015";
            ServerIO.loadWithProgressDialog(MainActivity.this, LoadWithProgressModes.AUTO, address);
        }
    }

    // Extract the proper server URL from the auto-connect port
    public void extractURLFromText(String line, URL url) {
        setLine(line);
        if (line.contains("http://") && line.contains("\n")) {
            String autoURL = line.substring(line.indexOf("\n") + 1, line.length() - 1);
            if (autoURL.contains("[")) {
                autoURL = "http://192.168.0.2:1112";
            }
            Matcher m1 = UtilsNetwork.VALID_IPV4_PATTERN.matcher(autoURL.substring(
                    7, autoURL.lastIndexOf(":")));
            if (m1.matches()) {
                saveIP(autoURL);
            } else {
                if (url.toString().length() > 5 && line.contains(":")) {
                    autoURL = url.toString().substring(0,
                            url.toString().length() - 6) + ""
                            + line.substring(line.lastIndexOf(":"),
                            line.length() - 1);
                    saveIP(autoURL);
                } else {
                    dialogsHelper.infoDialog(getString(R.string.msg_error_on_save_url), this);
                }
            }
        } else {
            String address = url.toString();
            ServerIO.loadWithProgressDialog(MainActivity.this, LoadWithProgressModes.AUTO, address);
        }
    }

    // Method to store the found IP after auto-connect
    private void saveIP(String found) {
        context.getSettings().saveSetting("urlMR", found);
        serverFound = true;
        dialogsHelper.checkUserInput(found, this);
    }

    // Send signal to update lyrics list
    public void reloadLyrics(ArrayList<String> lyrics) {
        getLyrics().clear();
        getLyrics().addAll(lyrics);
        if (playable) {
            if (lyrics.size() >= 1)
                getLyrics().set(0,
                        getResources().getString(R.string.action_play));
            else
                getLyrics().add(getResources().getString(R.string.action_play));
        }
        lyricsAdapter.notifyDataSetChanged();
    }

    // Send signal to update schedule list
    public void reloadSchedule(ArrayList<String> scheduleList) {
        getScheduleList().clear();
        getScheduleList().addAll(scheduleList);
        scheduleAdapter.notifyDataSetChanged();
    }

    public SettingsHelper getSettings() {
        if (settingsHelper == null)
            settingsHelper = new SettingsHelper();
        return settingsHelper;
    }

    public void lostConnection() {
        loggedIn = false;
        fullyStarted = false;
        dialogsHelper.enterURLDialog(
                getResources().getString(R.string.error_failed_finding_mr_server), settingsHelper.getIp(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the menu action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_buttons, menu);
        getMenuInflater().inflate(R.menu.main_hidden, menu);
        this.menu = menu;
        help = menu.findItem(R.id.help);
        help.setVisible(false);
        edit_book = menu.findItem(R.id.edit_book);
        edit_book.setVisible(false);
        record = menu.findItem(R.id.record);
        record.setVisible(false);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchViewMenu = menu;
        resultView = findViewById(R.id.dropDownSearch);
        translateMenuItem = menu.findItem(R.id.action_translate);

        searchView
                .setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view,
                                              boolean queryTextFocused) {
                        if (!queryTextFocused) {
                            searchMenuItem.collapseActionView();
                            help.setVisible(false);
                            edit_book.setVisible(false);
                            resultView.setVisibility(View.GONE);
                        }
                    }
                });

        // Get search query if return is pressed
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!bibleSearch) {
                    try {
                        String searchURL = settingsHelper.getIp() + "/search/" +
                                URLEncoder.encode(query, "utf-8");
                        ServerIO.loadWithProgressDialog(MainActivity.this,
                                LoadWithProgressModes.SEARCHINDIALOG, searchURL);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Search", "Search encoding failed: " + e);
                    }
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                    help.setVisible(false);
                    edit_book.setVisible(false);
                    resultView.setVisibility(View.GONE);
                } else {
                    try {
                        ServerIO.loadWithProgressDialog(MainActivity.this,
                                LoadWithProgressModes.BIBLE, settingsHelper.getIp() + "/addbible/"
                                        + bibleTranslation + "/" + URLEncoder.encode(bibleBook, "UTF-8")
                                        + "/" + query);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Search", "Add Bible encoding failed: " + e);
                    }
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                    help.setVisible(false);
                    edit_book.setVisible(false);
                }
                return true;
            }

            // Search for songs if more than three characters have been entered
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 3 && !bibleSearch) {
                    try {
                        String searchURL = settingsHelper.getIp() + "/search/" +
                                URLEncoder.encode(newText, "utf-8");
                        ServerIO.loadWithProgressDialog(MainActivity.this,
                                LoadWithProgressModes.SEARCH, searchURL);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Search", "Search encoding failed: " + e);
                    }
                    help.setVisible(false);
                    edit_book.setVisible(false);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks.
        int id = item.getItemId();

        if (scheduleDrawerLayout != null && scheduleDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle log out
        if (id == R.id.action_logout) {
            Toast.makeText(MainActivity.this, R.string.msg_logged_out,
                    Toast.LENGTH_SHORT).show();
            ServerIO.loadInBackground(settingsHelper.getIp() + "/logout", MainActivity.this);
            loggedIn = false;
            ServerIO.checkServerConnection(MainActivity.this, settingsHelper.getIp());
        }

        // Handle setting
        if (id == R.id.action_options) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        // Handle about
        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }

        // Handle report/feedback
        if (id == R.id.action_feedback) {
            Intent sendIntent = new Intent(
                    Intent.ACTION_SEND);
            String subject = "Bug report/Feature request: Quelea Mobile Remote";
            sendIntent.setType("message/rfc822");
            sendIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"arvid" + "@" + "quelea.org"});
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    subject);
            sendIntent.setType("message/rfc822");
            startActivity(sendIntent);
            return true;
        }

        // Handle about
        if (id == R.id.action_translate) {
            startActivity(new Intent(MainActivity.this, TranslationActivity.class));
        }

        // Handle search button
        if (id == R.id.action_search) {
            help.setVisible(true);
            item.getActionView();
            dialogsHelper.searchDialog(SearchMode.SONG, this);
        }

        // Handle edit button
        if (id == R.id.edit_book) {
            dialogsHelper.searchDialog(SearchMode.BIBLE, this);
        }

        // Handle search help button
        if (id == R.id.help) {
            dialogsHelper.infoDialog(getResources().getString(R.string.msg_how_to), this);
        }

        // Handle record button
        if (id == R.id.record) {
            ServerIO.loadInBackground(settingsHelper.getIp() + "/record", MainActivity.this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (settingsHelper.isUseVolume()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                event.startTracking();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                event.startTracking();
                return true;
            } else
                return false;
        } else if (settingsHelper.isUseDpad()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                event.startTracking();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                event.startTracking();
                return true;
            } else
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (!longPress) {
            if (!doubleClicked) {
                if (settingsHelper.isUseVolume()) {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        if (settingsHelper.getLongClick().equals(LongClickModes.PROGRESS.name().toLowerCase()))
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources().getString(
                                            R.string.action_previous_item),
                                    Toast.LENGTH_SHORT).show();
                        navigationHelper.handleLongPress(LongClickModes.PREVIOUS.name().toLowerCase());
                        longPress = true;
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        if (settingsHelper.getLongClick().equals(LongClickModes.PROGRESS.name().toLowerCase()))
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(R.string.action_next_item),
                                    Toast.LENGTH_SHORT).show();
                        navigationHelper.handleLongPress(LongClickModes.NEXT.name().toLowerCase());
                        longPress = true;
                        return true;
                    } else
                        return false;
                } else if (settingsHelper.isUseDpad()) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (settingsHelper.getLongClick().equals(LongClickModes.PROGRESS.name().toLowerCase()))
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources().getString(
                                            R.string.action_previous_item),
                                    Toast.LENGTH_SHORT).show();
                        navigationHelper.handleLongPress(LongClickModes.NEXT.name().toLowerCase());
                        longPress = true;
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (settingsHelper.getLongClick().equals(LongClickModes.PROGRESS.name().toLowerCase()))
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources()
                                            .getString(R.string.action_next_item),
                                    Toast.LENGTH_SHORT).show();
                        navigationHelper.handleLongPress(LongClickModes.PREVIOUS.name().toLowerCase());
                        longPress = true;
                        return true;
                    } else
                        return false;
                }
            } else
                doubleClicked = false;
            return false;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, @NonNull final KeyEvent event) {
        if (settingsHelper.isUseDpad() || settingsHelper.isUseVolume()) {
            if (!longPress && keyCode != 26)
                clicksWebView++;

            returnState = false;

            Handler handler = new Handler();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    if (clicksWebView == 1) {
                        singleClick = System.currentTimeMillis();
                        long diff = singleClick - doubleClick;
                        if (diff > 300)
                            returnState = navigationHelper.keyPress(keyCode, event);
                    }
                    clicksWebView = 0;
                }
            };

            if (clicksWebView == 1) {
                // Single click
                if (doubleClicked)
                    doubleClicked = false;
                handler.postDelayed(r, 150);
            } else if (clicksWebView == 2) {
                // Double click
                doubleClick = System.currentTimeMillis();
                doubleClicked = true;
                if (settingsHelper.isUseVolume()
                        && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                    navigationHelper.handleDoubleClick();
                } else if (settingsHelper.isUseDpad()
                        && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN)) {
                    navigationHelper.handleDoubleClick();
                } else
                    return false;
                clicksWebView = 0;
                returnState = true;
            }

            if (longPress) {
                longPress = false;
            }

            return returnState;
        }

        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (scheduleDrawerLayout != null)
            scheduleDrawerToggle.syncState();
    }


    @Override
    protected void onDestroy() {
        getLyricsAdapter().imageLoader.clearCache();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        // Close drawer if back is pressed when it's open
        if (scheduleDrawerLayout != null && scheduleDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            scheduleDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // If drawer already is closed, show exit dialog
            dialogsHelper.exitDialog(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (syncHandler != null)
            syncHandler.startSync();
        else {
            context.continueLoading();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (syncHandler != null)
            syncHandler.stopSync();
    }


    // Class to download the html code from the pages and convert it to data for the app
    public static class DownloadHtml extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                URLConnection yc = url.openConnection();
                yc.setConnectTimeout(2500);
                yc.setReadTimeout(2500);
                yc.setUseCaches(false);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        yc.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String text;
                while ((text = in.readLine()) != null)
                    sb.append(text).append("\n");
                in.close();
                return sb.toString();

            } catch (IOException e) {
                Log.e("HTMLWithoutProgress", "Exception: " + e.getLocalizedMessage() + " " + urls[0]);
                // TODO: Delete temporary work-around for Quelea bug
                if (urls[0].contains("/slides"))
                    return "slides";
                // Return empty content if no page was found
                return " ";
            }
        }

    }


    // Class to send the password entered in the password dialog
    public static class SendPassword extends AsyncTask<String, Integer, Double> {

        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        public SendPassword(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Double doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            MainActivity activity = activityReference.get();
            if (activity == null) return;

            // Check if password worked
            ServerIO.checkServerConnection(activity, activity.getSettings().getIp());

            // Show toast if login was successful
            if (activity.isLoggedIn())
                Toast.makeText(activity, R.string.msg_logged_in,
                        Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        void postData(String valueIWantToSend) {
            try {
                MainActivity activity = activityReference.get();
                if (activity == null) return;
                String charset = "UTF-8";
                String password = URLEncoder.encode(valueIWantToSend, charset);
                String query = String.format("password=%s", password);

                URLConnection urlConnection = new URL(activity.getSettings().getIp())
                        .openConnection();
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true); // Triggers POST.
                urlConnection.setRequestProperty("accept-charset", charset);
                urlConnection.setRequestProperty("content-type",
                        "application/x-www-form-urlencoded");

                OutputStreamWriter writer = null;
                try {
                    writer = new OutputStreamWriter(urlConnection.getOutputStream(), charset);
                    writer.write(query); // Write POST query string (if any needed).
                } finally {
                    if (writer != null) try {
                        writer.close();
                    } catch (IOException logOrIgnore) {
                        Log.e("Failed", "Failed sending password");
                    }
                }
                urlConnection.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /* ----------------------- Variables, getters and setters ----------------------------------- */


    public ArrayList<String> getVerseLyrics() {
        return verseLyrics;
    }

    public void setVerseLyrics(ArrayList<String> verseLyrics) {
        this.verseLyrics = verseLyrics;
    }

    public boolean isTextHidden() {
        return (isBlackUsed() || isClearUsed() || isLogoUsed());
    }

    public boolean isLogoUsed() {
        return logoUsed;
    }

    public void setLogoUsed(boolean logoUsed) {
        this.logoUsed = logoUsed;
    }

    public boolean isBlackUsed() {
        return blackUsed;
    }

    public void setBlackUsed(boolean blackUsed) {
        this.blackUsed = blackUsed;
    }

    public boolean isClearUsed() {
        return clearUsed;
    }

    public void setClearUsed(boolean clearUsed) {
        this.clearUsed = clearUsed;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public DialogsHelper getDialogsHelper() {
        return dialogsHelper;
    }

    public String getTempLyrics() {
        return tempLyrics;
    }

    public void setTempLyrics(String tempLyrics) {
        this.tempLyrics = tempLyrics;
    }

    public String getTempCurrentlyDisplaying() {
        return tempCurrentlyDisplaying;
    }

    public void setTempCurrentlyDisplaying(String tempCurrentlyDisplaying) {
        this.tempCurrentlyDisplaying = tempCurrentlyDisplaying;
    }

    public boolean isSongSelected() {
        return songSelected;
    }

    public void setSongSelected(boolean songSelected) {
        this.songSelected = songSelected;
    }

    public String getTempCompare() {
        return tempCompare;
    }

    public void setTempCompare(String tempCompare) {
        this.tempCompare = tempCompare;
        setLastSchedule(tempCompare);
    }

    public ArrayList<String> getLyrics() {
        return lyrics;
    }

    public void setLyrics(ArrayList<String> lyrics) {
        this.lyrics = lyrics;
        setLastLyrics(lyrics.toString());
    }

    public void setBibleSearch(boolean bibleSearch) {
        this.bibleSearch = bibleSearch;
    }

    public ListView getLyricsListView() {
        return lyricsListView;
    }

    public LyricsAdapter getLyricsAdapter() {
        return lyricsAdapter;
    }

    public ArrayList<String> getScheduleList() {
        return scheduleList;
    }

    public String[] getBibleTranslations() {
        return bibleTranslations;
    }

    public void setBibleTranslations(String[] bibleTranslations) {
        this.bibleTranslations = bibleTranslations;
    }

    public String[] getBibleBooks() {
        return bibleBooks;
    }

    public void setBibleBooks(String[] bibleBooks) {
        this.bibleBooks = bibleBooks;
    }

    public String getBibleTranslation() {
        return bibleTranslation;
    }

    public void setBibleTranslation(String bibleTranslation) {
        this.bibleTranslation = bibleTranslation;
    }

    public String getBibleBook() {
        return bibleBook;
    }

    public void setBibleBook(String bibleBook) {
        this.bibleBook = bibleBook;
    }

    public SyncHandler getSyncHandler() {
        return syncHandler;
    }

    public boolean isSelectedTranslations() {
        return selectedTranslations;
    }

    public void setSelectedTranslations(boolean selectedTranslations) {
        this.selectedTranslations = selectedTranslations;
    }

    public Menu getSearchViewMenu() {
        return searchViewMenu;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public MenuItem getHelp() {
        return help;
    }

    public MenuItem getRecord() {
        return record;
    }

    public MenuItem getEdit_book() {
        return edit_book;
    }

    public boolean isServerFound() {
        return serverFound;
    }

    public boolean isFullyStarted() {
        return fullyStarted;
    }

    public void setFullyStarted(boolean fullyStarted) {
        this.fullyStarted = fullyStarted;
    }

    public boolean isSlide() {
        return slide;
    }

    public void setSlide(boolean slide) {
        this.slide = slide;
    }

    public ListView getResultView() {
        return resultView;
    }

    public void setResultView(ListView resultView) {
        this.resultView = resultView;
    }

    public boolean isResultIsOpen() {
        return resultIsOpen;
    }

    public void setResultIsOpen(boolean resultIsOpen) {
        this.resultIsOpen = resultIsOpen;
    }

    public boolean isBlackPressed() {
        return blackPressed;
    }

    public void setBlackPressed(boolean blackPressed) {
        this.blackPressed = blackPressed;
    }

    public boolean isLogoPressed() {
        return logoPressed;
    }

    public void setLogoPressed(boolean logoPressed) {
        this.logoPressed = logoPressed;
    }

    public boolean isClearPressed() {
        return clearPressed;
    }

    public void setClearPressed(boolean clearPressed) {
        this.clearPressed = clearPressed;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
        setLastLine(line);
    }

    public boolean isPresentation() {
        return presentation;
    }

    public void setPresentation(boolean presentation) {
        this.presentation = presentation;
    }

    public boolean isLatestVersion() {
        return DownloadHandler.latestVersion;
    }

    public boolean isCanJump() {
        return canJump;
    }

    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }

    public ParseDownloadedTextHelper getParseDownloadedTextHelper() {
        if (parseDownloadedTextHelper != null)
            return parseDownloadedTextHelper;
        else
            return new ParseDownloadedTextHelper(this);
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public String getCurrentlyDisplaying() {
        return currentlyDisplaying;
    }

    public void setCurrentlyDisplaying(String currentlyDisplaying) {
        this.currentlyDisplaying = currentlyDisplaying;
        setLastItem(currentlyDisplaying);
    }

    public Menu getMenu() {
        return menu;
    }

    public String[] getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(String[] searchResult) {
        this.searchResult = searchResult;
    }

    public String[] getTempResult() {
        return tempResult;
    }

    public void setTempResult(String[] tempResult) {
        this.tempResult = tempResult;
    }

    public boolean dialogNotShown() {
        return !isDialogShown;
    }

    public void setDialogShown(boolean dialogShown) {
        this.isDialogShown = dialogShown;
    }

    public int getActiveVerse() {
        return activeVerse;
    }

    public void setActiveVerse(int activeVerse) {
        this.activeVerse = activeVerse;
    }

    public int getVerseTotal() {
        return verseTotal;
    }

    public void setVerseTotal(int verseTotal) {
        this.verseTotal = verseTotal;
    }

    public int getActiveItem() {
        return activeItem;
    }

    public void setActiveItem(int activeItem) {
        this.activeItem = activeItem;
    }

    public int getPreviewItem() {
        return previewItem;
    }

    public void setPreviewItem(int previewItem) {
        this.previewItem = previewItem;
    }

    public int getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(int itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    private static String lastLyrics;

    public static String getLastLyrics() {
        return lastLyrics;
    }

    public void setLastLyrics(String s) {
        MainActivity.lastLyrics = s;
    }

    private static String lastItem;

    public static String getLastItem() {
        return lastItem;
    }

    public void setLastItem(String s) {
        MainActivity.lastItem = s;
    }

    private static String lastSchedule;

    public static String getLastSchedule() {
        return lastSchedule;
    }

    public static void setLastSchedule(String lastSchedule) {
        MainActivity.lastSchedule = lastSchedule;
    }

    private static String lastLine;

    public static String getLastLine() {
        return lastLine;
    }

    public void setLastLine(String s) {
        MainActivity.lastLine = s;
    }


    public long getSingleClick() {
        return singleClick;
    }

    public void setSingleClick(long singleClick) {
        this.singleClick = singleClick;
    }

    public long getDoubleClick() {
        return doubleClick;
    }

    public void setDoubleClick(long doubleClick) {
        this.doubleClick = doubleClick;
    }

    public boolean isNotLongPress() {
        return !longPress;
    }

    public void setLongPress(boolean longPress) {
        this.longPress = longPress;
    }

    public boolean isNotDoubleClicked() {
        return !doubleClicked;
    }

    public void setDoubleClicked(boolean doubleClicked) {
        this.doubleClicked = doubleClicked;
    }

    public DrawerLayout getScheduleDrawerLayout() {
        return scheduleDrawerLayout;
    }

    public MenuItem getTranslateMenuItem() {
        return translateMenuItem;
    }

    public ProgressDialog getProgressDialog() {
        if (progressDialogs.isEmpty()) {
            progressDialogs.add(new ProgressDialog(MainActivity.this));
        }
        return progressDialogs.get(0);
    }

    public ArrayList<ProgressDialog> getProgressDialogs() {
        return progressDialogs;
    }

    public SparseArray<String> getSlideTitles() {
        return slideTitles;
    }

    public static SettingsHelper settingsHelper = new SettingsHelper();
    private DialogsHelper dialogsHelper = new DialogsHelper();
    private ScheduleAdapter scheduleAdapter;
    private ActionBarDrawerToggle scheduleDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private DrawerLayout scheduleDrawerLayout;
    private NavigationHelper navigationHelper;
    private ParseDownloadedTextHelper parseDownloadedTextHelper;
    private MainActivity context;
    private LyricsAdapter lyricsAdapter;
    private SyncHandler syncHandler;
    private ArrayList<ProgressDialog> progressDialogs = new ArrayList<>();
    private Menu searchViewMenu;
    private Menu menu;
    private MenuItem help;
    private MenuItem record;
    private MenuItem edit_book;
    private MenuItem translateMenuItem;
    private ListView scheduleListView;
    private ListView lyricsListView;
    private ListView resultView;
    private ArrayList<String> verseLyrics = new ArrayList<>();
    private ArrayList<String> lyrics = new ArrayList<>();
    private ArrayList<String> scheduleList = new ArrayList<>();
    private String bibleTranslation;
    private String bibleBook;
    private String currentlyDisplaying = "";
    private String tempLyrics = "";
    private String tempCurrentlyDisplaying = "";
    private String tempCompare = "";
    private String line;
    private String[] searchResult = {""};
    private String[] tempResult = {""};
    private String[] bibleTranslations = {""};
    private String[] bibleBooks = {""};
    private int activeVerse;
    private int verseTotal;
    private int activeItem = -1;
    private int previewItem = -1;
    private int itemsTotal = 0;
    private int clicksWebView;
    private long singleClick;
    private long doubleClick;
    private boolean logoUsed = false;
    private boolean blackUsed = false;
    private boolean clearUsed = false;
    private boolean loggedIn = false;
    private boolean online;
    private boolean isDialogShown;
    private boolean songSelected = false;
    private boolean bibleSearch;
    private boolean selectedTranslations = false;
    private boolean playable = false;
    private boolean serverFound;
    private boolean longPress = false;
    private boolean returnState = false;
    private boolean doubleClicked = false;
    private boolean fullyStarted = false;
    private boolean slide;
    private boolean resultIsOpen;
    private boolean blackPressed;
    private boolean logoPressed;
    private boolean clearPressed;
    private boolean presentation;
    private boolean canJump = true;
    private SparseArray<String> slideTitles = new SparseArray<>();

    private enum LongClickModes {
        PROGRESS,
        PREVIOUS,
        NEXT
    }

    public enum LoadWithProgressModes {
        CHECK,
        SEARCH,
        GETSONG,
        BIBLE,
        BOOKS,
        TRANSLATIONS,
        AUTO,
        SEARCHINDIALOG
    }

    public enum SearchMode {
        BIBLE,
        SONG
    }

}
