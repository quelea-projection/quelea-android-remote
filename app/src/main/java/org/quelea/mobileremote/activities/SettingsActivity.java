package org.quelea.mobileremote.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.quelea.mobileremote.R;

public class SettingsActivity extends PreferenceActivity {
    private Preference onEnd;
    private Preference doublePress;
    private Preference longPress;
    private boolean volumeChecked;
    private boolean dpadChecked;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        volumeChecked = sharedPrefs.getBoolean("volume_buttons", false);
        dpadChecked = sharedPrefs.getBoolean("dpad_buttons", false);

        CheckBoxPreference volumeButtons = (CheckBoxPreference) findPreference("volume_buttons");
        CheckBoxPreference dpad = (CheckBoxPreference) findPreference("dpad_buttons");

        final PreferenceCategory navigation_category = (PreferenceCategory) findPreference("pref_key_navigation_settings");
        onEnd = findPreference("on_end");
        doublePress = findPreference("double_press");
        longPress = findPreference("long_press");

        if (!dpadChecked && !volumeChecked && navigation_category != null) {
            navigation_category.removePreference(onEnd);
            navigation_category.removePreference(doublePress);
            navigation_category.removePreference(longPress);
        }

        volumeButtons
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {

                        boolean switchedOn = (Boolean) newValue;
                        if (switchedOn) {
                            volumeChecked = sharedPrefs.getBoolean(
                                    "volume_buttons", false);
                            dpadChecked = sharedPrefs.getBoolean(
                                    "dpad_buttons", false);
                            if (!dpadChecked) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.msg_new_settings_added, Toast.LENGTH_LONG)
                                        .show();

                                if (navigation_category != null) {
                                    navigation_category.addPreference(onEnd);
                                    navigation_category.addPreference(doublePress);
                                    navigation_category.addPreference(longPress);
                                }
                            }

                        } else {
                            volumeChecked = sharedPrefs.getBoolean(
                                    "volume_buttons", false);
                            dpadChecked = sharedPrefs.getBoolean(
                                    "dpad_buttons", false);
                            if (!dpadChecked && navigation_category != null) {
                                navigation_category.removePreference(onEnd);
                                navigation_category.removePreference(doublePress);
                                navigation_category.removePreference(longPress);
                            }
                        }

                        return true;
                    }
                });

        dpad.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {

                boolean switchedOn = (Boolean) newValue;
                if (switchedOn) {
                    volumeChecked = sharedPrefs.getBoolean("volume_buttons",
                            false);
                    dpadChecked = sharedPrefs.getBoolean("dpad_buttons", false);
                    if (!volumeChecked) {
                        Toast.makeText(getApplicationContext(),
                                R.string.msg_new_settings_added, Toast.LENGTH_LONG)
                                .show();

                        if (navigation_category != null) {
                            navigation_category.addPreference(onEnd);
                            navigation_category.addPreference(doublePress);
                            navigation_category.addPreference(longPress);
                        }
                    }
                } else {
                    volumeChecked = sharedPrefs.getBoolean("volume_buttons",
                            false);
                    dpadChecked = sharedPrefs.getBoolean("dpad_buttons", false);
                    if (!volumeChecked && navigation_category != null) {
                        navigation_category.removePreference(onEnd);
                        navigation_category.removePreference(doublePress);
                        navigation_category.removePreference(longPress);
                    }
                }

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));

    }


}
