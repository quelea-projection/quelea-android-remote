package org.quelea.mobileremote.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.adapters.TranslationAdapter;
import org.quelea.mobileremote.dialogs.DefaultDialog;
import org.quelea.mobileremote.items.TranslationLine;
import org.quelea.mobileremote.utils.UtilsMisc;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity that allows the user to create a translation file within the app.
 * NB: Not finished!
 *
 * @author Arvid
 */

public class TranslationActivity extends AppCompatActivity {
    private String[] lines;
    final private int REQUEST_CODE_ASK_PERMISSION_WRITE_EXTERNAL = 124;
    private List<TranslationLine> translationLines;
    private String currentLocale;
    private MenuItem save;
    private ProgressBar progressBar;
    private static List<TranslationLine> translatedLines = new ArrayList<>();
    private File translationFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        // TODO: Add guide
        checkWritePermission();
    }

    private void saveFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<resources>");
        progressBar.setProgress(0);
        for (TranslationLine tl : translationLines) {
            stringBuilder.append("<string name=\"").append(tl.getLabel()).append("\">").append(tl.getTranslation()).append("</string>");
            if (tl.isFinished())
                progressBar.incrementProgressBy(1);
            tl.setLastSave(tl.getTranslation());
        }
        stringBuilder.append("</resources>");
        UtilsMisc.writeToFile(translationFile.getAbsolutePath(), stringBuilder.toString());
        enableSave(false);
        // TODO: On finished suggest send
    }

    private void continueLoading() {
        currentLocale = Locale.getDefault().getLanguage();
        if (currentLocale.length() > 2)
            currentLocale = currentLocale.substring(0, 1);
        lines = UtilsMisc.readAssets("string_id", this).toString().split("\n");
        translationFile = new File(Environment.getExternalStorageDirectory(), "strings-" + currentLocale + ".xml");
        translatedLines.clear();
        translatedLines.addAll(getEditedLabels());
        translationLines = new ArrayList<>();
        setupListView();
    }

    private void setupListView() {
        for (String s : lines) {
            TranslationLine newLine = getTranslationLine(s, currentLocale, this);
            if (newLine != null)
                translationLines.add(newLine);
        }
        ListView yourListView = findViewById(R.id.translationListView);
        TranslationAdapter customAdapter = new TranslationAdapter(this, R.layout.item_translation_row, translationLines);
        yourListView.setAdapter(customAdapter);
        progressBar = findViewById(R.id.translation_progress_bar);
        progressBar.setMax(translationLines.size());
        int finished = 0;
        for (TranslationLine tl : translationLines) {
            if (tl.isFinished()) {
                finished++;
            }
        }
        updateProgress(finished);
    }

    private List<TranslationLine> getEditedLabels() {
        String xml = UtilsMisc.readFromFile(translationFile.getAbsolutePath());
        XmlPullParserFactory factory;
        List<TranslationLine> list = new ArrayList<>();
        try {
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(xml));
            int eventType = xpp.getEventType();
            TranslationLine test;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("string")) {
                        String tag = xpp.getAttributeValue(null, "name");
                        String text = xpp.nextText();
                        test = new TranslationLine("", text, tag);
                        list.add(test);
                        test.setIgnoreWarning(true);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static TranslationLine getTranslationLine(String line, String currentLocale, Context context) {
        if (!line.contains("="))
            return null;
        boolean ignoreWarning = false;
        String englishOriginal = getLocaleStringResource(new Locale("en"),
                Integer.parseInt(line.substring(line.indexOf("=") + 1), 16), context);
        String label = line.split("[=]")[0];
        String desiredTranslation = getLocaleStringResource(new Locale(currentLocale),
                Integer.parseInt(line.substring(line.indexOf("=") + 1), 16), context);
        if (englishOriginal.equals(desiredTranslation))
            desiredTranslation = "";
        for (TranslationLine tl : translatedLines) {
            if (tl.getLabel() != null && tl.getLabel().equals(label) && !tl.getTranslation().trim().isEmpty()) {
                desiredTranslation = tl.getTranslation();
                ignoreWarning = true;
            }
        }
        TranslationLine newLine = new TranslationLine(englishOriginal, desiredTranslation, label);
        newLine.setIgnoreWarning(ignoreWarning);
        return newLine;
    }


    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            try {
                Configuration config = new Configuration(context.getResources().getConfiguration());
                config.setLocale(requestedLocale);
                Context configurationContext = context.createConfigurationContext(config);
                result = configurationContext.getText(resourceId).toString();
            } catch (NullPointerException e) {
                return "";
            } catch (Resources.NotFoundException e) {
                return "";
            }
        } else { // support older android versions
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, null);

            // retrieve resources from desired locale
            result = resources.getString(resourceId);

            // restore original locale
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, null);
        }

        return result;
    }

    private void emailFile(File file) {
        Uri path = Uri.fromFile(file);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"arvid" + "@" + "quelea.org"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name_mr) + " Translation " + "(" + currentLocale + ")");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void checkWritePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                final DefaultDialog dialog = new DefaultDialog(TranslationActivity.this,
                        "To be able to save the file you have translated, you need to allow the app to store a file to your SD card.", "",
                        getResources().getString(R.string.action_ok_label), "",
                        getResources().getString(R.string.action_cancel_label), false, false);
                dialog.getYes().setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View v) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSION_WRITE_EXTERNAL);
                        dialog.getAlertDialog().dismiss();
                    }
                });
                dialog.getNeutral().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        isShowing = false;
                        dialog.getAlertDialog().dismiss();
                    }
                });
            } else {
                continueLoading();
            }
        }
    }

    boolean isShowing = false;


    public static UtilsMisc.TranslationProgress checkTranslation(Context context) {
        UtilsMisc.TranslationProgress progress = UtilsMisc.TranslationProgress.NONE;
        String current = Locale.getDefault().getLanguage();
        if (current.length() > 2)
            current = current.substring(0, 1);
        for (String lang : context.getResources().getStringArray(R.array.supported_languages)) {
            if (lang.substring(0, 2).contains(current)) {
                if (lang.contains(UtilsMisc.TranslationProgress.PARTIAL.name().toLowerCase()))
                    progress = UtilsMisc.TranslationProgress.PARTIAL;
                else if (lang.contains(UtilsMisc.TranslationProgress.FINISHED.name().toLowerCase())) {
                    progress = UtilsMisc.TranslationProgress.FINISHED;
                }
            }
        }
        return progress;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.translate, menu);
        save = menu.findItem(R.id.save);
        enableSave(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            saveFile();
        }
        if (id == R.id.share) {
            saveFile();
            if (progressBar.getProgress() == progressBar.getMax()) {
                emailFile(translationFile);
            } else {
                final DefaultDialog dialog = new DefaultDialog(TranslationActivity.this,
                        "You still have strings left to translate/approve before you can send the file.", "",
                        getResources().getString(R.string.action_ok_label),
                        null,
                        null, false, false);
                dialog.getYes().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.getAlertDialog().dismiss();
                    }
                });
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateProgress(int setProgress) {
        progressBar.setProgress(setProgress);
    }

    public void enableSave(boolean changed) {
        if (save != null && save.getIcon() != null) {
            if (changed) {
                save.getIcon().setAlpha(255);
                save.setEnabled(true);
            } else {
                save.getIcon().setAlpha(80);
                save.setEnabled(false);
            }
        }
    }

    public List<TranslationLine> getLines() {
        return translationLines;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_CODE_ASK_PERMISSION_WRITE_EXTERNAL:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
                        continueLoading();
                    } else {
                        DefaultDialog dialog = new DefaultDialog(TranslationActivity.this,
                                "You cannot use this feature unless you grant the app permission to store a file to your SD card", "",
                                getResources().getString(R.string.action_ok_label),
                                null,
                                null, false, false);
                        dialog.getYes().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (save.isEnabled()) {
            final DefaultDialog dialog = new DefaultDialog(TranslationActivity.this,
                    "Do you want to save the changes?", "",
                    getResources().getString(R.string.action_ok_label),
                    getResources().getString(R.string.action_no_label),
                    getResources().getString(R.string.action_cancel_label), false, false);
            dialog.getYes().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFile();
                    finish();
                }
            });
            dialog.getNo().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            dialog.getNeutral().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    continueLoading();
                    dialog.getAlertDialog().dismiss();
                }
            });
        } else {
            finish();
        }
    }
}
