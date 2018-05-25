package org.quelea.mobileremote.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.activities.TranslationActivity;
import org.quelea.mobileremote.activities.TroubleshootingActivity;
import org.quelea.mobileremote.adapters.ThemeAdapter;
import org.quelea.mobileremote.network.SeverIO;
import org.quelea.mobileremote.utils.UtilsMisc;
import org.quelea.mobileremote.utils.UtilsNetwork;

import java.util.Arrays;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Class to handle all types of dialogs
 * Created by Arvid on 2017-11-29.
 */

public class DialogsHelper {
    private int urlNotFound = 0;

    // Show dialog for info
    public void infoDialog(final String message, final MainActivity context) {
        if (context.dialogNotShown()) {
            context.setDialogShown(true);
            final DefaultDialog dialog = new DefaultDialog(context, message, "", "", "", context.getResources().getString(R.string.action_ok_label), false, false);
            dialog.getNeutral().setText(context.getResources().getString(R.string.action_ok_label));
            dialog.getNeutral().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.getAlertDialog().dismiss();
                    context.setDialogShown(false);
                }
            });
        }
    }

    // Show dialog telling user to enter URL
    public void enterURLDialog(String message, String prefill, final MainActivity context) {
        urlNotFound++;
        context.setDialogShown(true);
        DefaultDialog alert = new DefaultDialog(context, message, prefill, context.getResources().getString(R.string.action_ok_label), context.getResources().getString(R.string.action_exit), context.getResources().getString(R.string.action_search_server), true, true);
        final EditText input = alert.getInput();
        final AlertDialog dialog = alert.getAlertDialog();

        alert.getYes().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.setDialogShown(false);
                Editable value = input.getText();
                String ip = value.toString();
                checkUserInput(ip, context);
                dialog.dismiss();
            }
        });

        alert.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exit the app
                closeApp(context);
            }
        });

        alert.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                context.setDialogShown(false);
                SeverIO.checkServerConnection(context, context.getSettings().getIp());
            }
        });

        if (urlNotFound > 1) {
            Animation scale = AnimationUtils.loadAnimation(context, R.anim.pop);
            alert.getHelp().startAnimation(scale);
            urlNotFound = 0;
        }

        alert.getHelp().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, TroubleshootingActivity.class));
            }
        });

        // Listen for enter/return press on input
        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView input, int actionId,
                                          KeyEvent event) {
                if (((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Editable value = input.getEditableText();
                    String ip = value.toString();
                    checkUserInput(ip, context);
                    dialog.dismiss();
                }
                return true;
            }
        };

        input.setOnEditorActionListener(exampleListener);
    }

    public void checkUserInput(String ip, MainActivity context) {
        // Check if input is empty or contains less than 7 characters
        // (which both is the smallest potential IP with port
        // number and the amount of characters needed to check if "http://"
        // is added or not)
        if ((ip.equals("")) || (ip.length() < 7)
                || (!(ip.contains(":")))) {
            if (context.dialogNotShown())
                enterURLDialog(
                        context.getResources().getString(
                                R.string.error_url_incorrect), "", context);

        } else {
            // Check if http:// needs to be added
            if (ip.length() > 7 && !(ip.substring(0, 7).equals("http://")))
                ip = "http://" + ip;
            ip = ip.replaceAll(" ", "");
            String match = UtilsNetwork.matchIP(ip);
            switch (match) {
                case "true":
                    context.getSettings().saveSetting("urlMR", ip);
                    break;
                case "ipv6":
                    enterURLDialog(
                            context.getResources().getString(
                                    R.string.msg_cant_use_ipv6), "", context);
                    return;
                default:
                    enterURLDialog(context.getString(R.string.error_url_incorrect), ip, context);
                    return;
            }

            // Check if login is needed
            if (Patterns.WEB_URL.matcher(ip).matches() && !context.isLoggedIn()) {
                context.getSettings().setIp(ip);
                SeverIO.checkServerConnection(context, ip);
            } else if (!context.isLoggedIn()) {
                enterURLDialog(context.getString(R.string.error_url_incorrect), ip, context);
            }

            // Download lyrics if already logged in
            if (context.isLoggedIn()) {
                SeverIO.downloadLyrics(context);
            }
            context.setDialogShown(false);
        }
    }

    // Show dialog for logging in to Mobile Remote server
    public void loginDialog(final MainActivity context) {
        context.setDialogShown(true);
        DefaultDialog alert = new DefaultDialog(context, context.getResources().getString(R.string.msg_enter_password), "", context.getResources().getString(R.string.action_ok_label), context.getResources().getString(R.string.action_exit), "", false, true);
        final EditText input = alert.getInput();
        final AlertDialog alertText = alert.getAlertDialog();

        alert.getYes().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable value = input.getText();
                String password = value.toString();
                alertText.dismiss();

                // Log in
                SeverIO.sendPassword(password, context);
                context.setDialogShown(false);
            }
        });

        alert.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exit app
                closeApp(context);
            }
        });

        // Listen for enter/return press on input
        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView input, int actionId,
                                          KeyEvent event) {
                if (((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    alertText.getButton(Dialog.BUTTON_POSITIVE).performClick();
                    if (alertText.isShowing()) {
                        context.setDialogShown(false);
                        Editable value = input.getEditableText();
                        String password = value.toString();

                        // Log in
                        SeverIO.sendPassword(password, context);
                        alertText.dismiss();
                    }
                }
                return true;
            }
        };

        input.setOnEditorActionListener(exampleListener);

    }


    public void scheduleLongClickDialog(final int i, final MainActivity context) {
        context.setDialogShown(true);
        SelectionDialog csd = new SelectionDialog(context, String.format(context.getString(R.string.msg_choose_action), context.getScheduleList().get(i)), "", "", "");
        String[] options = {context.getString(R.string.action_remove_item), context.getString(R.string.action_move_up), context.getString(R.string.action_move_down)};
        final AlertDialog dialog = csd.getAlertDialog();
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                context.setDialogShown(false);
            }
        });
        ListView list = csd.getListView();
        ListAdapter adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, options);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                if (which == 0) {
                    String url = context.getSettings().getIp() + "/remove/" + i;
                    SeverIO.checkSupported(url, context);
                } else if (which == 1) {
                    String url = context.getSettings().getIp() + "/moveup/" + i;
                    SeverIO.checkSupported(url, context);
                } else if (which == 2) {
                    String url = context.getSettings().getIp() + "/movedown/" + i;
                    SeverIO.checkSupported(url, context);
                }
                context.setDialogShown(false);
                dialog.dismiss();
            }
        });
    }

    public void selectThemeDialog(String line, final MainActivity context) {
        if (context.dialogNotShown()) {
            context.setDialogShown(true);
            if (!line.contains("<!DOCTYPE html>")) {
                SelectionDialog csd = new SelectionDialog(context, context.getString(R.string.title_select_theme), "", "", context.getString(R.string.action_cancel_label));
                final String[] options = line.split("\n");
                final AlertDialog dialog = csd.getAlertDialog();
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        context.setDialogShown(false);
                    }
                });
                ListView list = csd.getListView();
                ThemeAdapter adapter = new ThemeAdapter(context, Arrays.asList(options));
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                        Toast.makeText(context, String.format(context.getString(R.string.msg_setting_theme), options[which]), Toast.LENGTH_SHORT).show();
                        SeverIO.setTheme(options[which].replaceAll(" ", "%20"), context);
                        dialog.dismiss();
                        context.setDialogShown(false);
                    }
                });
                csd.getNeutral().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        context.setDialogShown(false);
                    }
                });
            } else {
                context.setDialogShown(false);
                infoDialog(context.getString(R.string.msg_not_supported), context);
            }
        }
    }

    public void exitDialog(final MainActivity context) {
        DefaultDialog cd = new DefaultDialog(context, context.getResources().getString(R.string.msg_want_to_exit), "", context.getResources().getString(R.string.action_yes_label), context.getResources().getString(R.string.action_no_label), "", false, false);
        final AlertDialog dialog = cd.getAlertDialog();

        cd.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cd.getYes().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Exit the app
                context.getLyricsAdapter().imageLoader.clearCache();
                closeApp(context);
                dialog.dismiss();
            }
        });
    }

    // Dialog for selecting what to search for
    public void searchDialog(MainActivity.SearchMode newSearch, MainActivity context) {
        if (newSearch == MainActivity.SearchMode.SONG)
            newSearchDialog(context);
        else if (newSearch == MainActivity.SearchMode.BIBLE) {
            bibleSearchDialog(context);
        }
    }

    private void newSearchDialog(final MainActivity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View customDialog = factory.inflate(
                R.layout.dialog_search, null);
        builder.setView(customDialog);
        TextView t = customDialog.findViewById(R.id.bibleSearch);
        t.setText(context.getResources().getString(R.string.action_add_bible));
        TextView t2 = customDialog.findViewById(R.id.songSearch);
        t2.setText(context.getResources().getString(R.string.action_search_song));
        builder.setCancelable(false);
        final AlertDialog alertText = builder.show();
        customDialog.findViewById(R.id.songButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MenuItem searchMenuItem = context.getSearchViewMenu()
                        .findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchMenuItem.getActionView();
                searchView.setQueryHint(context.getResources()
                        .getString(R.string.title_enter_song));
                context.setBibleSearch(false);
                alertText.dismiss();

            }
        });
        customDialog.findViewById(R.id.bibleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.setBibleSearch(true);
                SeverIO.getTranslations(context);
                alertText.dismiss();
            }
        });
        customDialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertText.dismiss();
                final MenuItem searchMenuItem = context.getSearchViewMenu()
                        .findItem(R.id.action_search);
                searchMenuItem.collapseActionView();
                context.getHelp().setVisible(false);
                context.getEdit_book().setVisible(false);
            }
        });
    }

    // Dialog for bible searches
    private void bibleSearchDialog(MainActivity context) {
        // Check if no translation is selected (= new search)
        if (!context.isSelectedTranslations()) {
            bibleTranslationSelectionDialog(context);
        } else {
            // If translation already is selected, show the books that are already downloaded
            bibleBookSelectionDialog(context);
        }
    }

    private void bibleBookSelectionDialog(final MainActivity context) {
        if (!(context.getBibleBooks()[0].isEmpty())) {
            SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.msg_select_book), "", context.getResources().getString(R.string.action_change_translation), context.getResources().getString(R.string.action_cancel_label));
            final AlertDialog dialog = csd.getAlertDialog();
            ListView list = csd.getListView();
            ListAdapter adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, context.getBibleBooks());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                    // The 'which' argument contains the
                    // index position of the selected item
                    context.setBibleBook(context.getBibleBooks()[which]);
                    final MenuItem searchMenuItem = context.getSearchViewMenu()
                            .findItem(R.id.action_search);
                    final SearchView search = (SearchView) searchMenuItem.getActionView();
                    search.setQueryHint(context.getResources()
                            .getString(
                                    R.string.msg_bible_query_hint)
                            + " "
                            + context.getBibleBook()
                            + " ("
                            + context.getBibleTranslation().replaceAll(
                            "%20", " ") + ")");
                    context.getEdit_book().setVisible(true);
                    StringBuilder sb = UtilsMisc.readAssets("chapter_lengths.txt", context);
                    int chapters = 0;
                    for (String s : sb.toString().split("\n")) {
                        if (s.startsWith((which + 1) + ",")) {
                            String[] val = s.split(",");
                            chapters = Integer.valueOf(val[1]);
                        }
                    }
                    bibleChapterSelectionDialog(chapters, (which + 1), context);
                    dialog.dismiss();
                }
            });
            csd.getNeutral().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Close dialog and hide buttons
                    dialog.dismiss();
                    // Hide buttons and search bar
                    final MenuItem searchMenuItem = context.getSearchViewMenu()
                            .findItem(R.id.action_search);
                    searchMenuItem.collapseActionView();
                    context.getHelp().setVisible(false);
                    context.getEdit_book().setVisible(false);
                }
            });
            csd.getNo().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.setSelectedTranslations(false);
                    searchDialog(MainActivity.SearchMode.BIBLE, context);
                    dialog.dismiss();
                }
            });
        }
    }

    private void bibleChapterSelectionDialog(int chapters, final int bookNum, final MainActivity context) {
        final MenuItem searchMenuItem = context.getSearchViewMenu()
                .findItem(R.id.action_search);
        final SearchView search = (SearchView) searchMenuItem.getActionView();
        SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.title_select_chapter) + " (" + context.getBibleBook() + ")", "", context.getResources().getString(R.string.action_change_book), context.getResources().getString(R.string.action_cancel_label));
        final AlertDialog dialog = csd.getAlertDialog();
        ListView list = csd.getListView();

        final String[] strings = new String[chapters];
        for (int i = 0; i < chapters; i++) {
            strings[i] = "" + (i + 1);
        }

        ListAdapter adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, strings);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                // The 'which' argument contains the
                // index position of the selected item
                search.setQuery(strings[which], false);
                StringBuilder sb = UtilsMisc.readAssets("chapter_lengths.txt", context);
                int verses = 0;
                for (String s : sb.toString().split("\n")) {
                    if (s.startsWith(bookNum + "," + strings[which] + ",")) {
                        String[] val = s.split(",");
                        verses = Integer.valueOf(val[2]);
                    }
                }
                bibleVerseSelectionDialog(verses, context);
                dialog.dismiss();
            }
        });
        csd.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close dialog
                dialog.dismiss();
            }
        });
        csd.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.setSelectedTranslations(false);
                bibleBookSelectionDialog(context);
                dialog.dismiss();
            }
        });
    }

    private void bibleVerseSelectionDialog(int verses, final MainActivity context) {
        final MenuItem searchMenuItem = context.getSearchViewMenu()
                .findItem(R.id.action_search);
        final SearchView search = (SearchView) searchMenuItem.getActionView();
        SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.title_select_verse) + " (" + context.getBibleBook() + " " + search.getQuery() + ")", "", "", context.getResources().getString(R.string.action_cancel_label));
        final AlertDialog dialog = csd.getAlertDialog();
        ListView list = csd.getListView();

        final String[] strings = new String[verses];
        for (int i = 0; i < verses; i++) {
            strings[i] = "" + (i + 1);
        }

        ListAdapter adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, strings);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                // The 'which' argument contains the
                // index position of the selected item
                search.setQuery(search.getQuery() + ":" + strings[which], false);
                StringBuilder remaining = new StringBuilder();
                for (int i = (which + 1); i < strings.length; i++) {
                    remaining.append(strings[i]).append("\n");
                }
                if (!remaining.toString().isEmpty())
                    bibleVerseToSelectionDialog(remaining.toString().split("\n"), context);
                dialog.dismiss();
            }
        });
        csd.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close dialog
                dialog.dismiss();
            }
        });
    }


    private void bibleVerseToSelectionDialog(final String[] remainingVerses, final MainActivity context) {
        final MenuItem searchMenuItem = context.getSearchViewMenu()
                .findItem(R.id.action_search);
        final SearchView search = (SearchView) searchMenuItem.getActionView();
        SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.title_select_ending_verse) + " (" + context.getBibleBook() + " " + search.getQuery() + ")?", "", context.getResources().getString(R.string.action_add), context.getResources().getString(R.string.action_cancel_label));
        final AlertDialog dialog = csd.getAlertDialog();
        ListView list = csd.getListView();

        ListAdapter adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, remainingVerses);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                search.setQuery(search.getQuery() + "-" + remainingVerses[which], true);
                dialog.dismiss();
            }
        });
        csd.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close dialog
                dialog.dismiss();
            }
        });
        csd.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setQuery(search.getQuery(), true);
                dialog.dismiss();
            }
        });
    }

    private void bibleTranslationSelectionDialog(final MainActivity context) {
        if (!(context.getBibleTranslations()[0].isEmpty())) {
            SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.action_select_translation), "", "", context.getResources().getString(R.string.action_cancel_label));
            final AlertDialog dialog = csd.getAlertDialog();
            ListView list = csd.getListView();
            ListAdapter adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, context.getBibleTranslations());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                    context.getParseDownloadedTextHelper().downloadBooks(which);
                    dialog.dismiss();
                }
            });
            csd.getNeutral().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Close dialog and hide buttons
                    dialog.dismiss();
                    final MenuItem searchMenuItem = context.getSearchViewMenu()
                            .findItem(R.id.action_search);
                    searchMenuItem.collapseActionView();
                    context.getHelp().setVisible(false);
                    context.getEdit_book().setVisible(false);
                }
            });
        }
    }

    public void showResultInDialog(final MainActivity context) {
        // Show the results of the songs found
        if (!(context.getSearchResult()[0].isEmpty())) {
            SelectionDialog csd = new SelectionDialog(context, context.getResources().getString(R.string.msg_select_song), "", "", context.getResources().getString(R.string.action_cancel_label));
            final AlertDialog dialog = csd.getAlertDialog();
            ListView list = csd.getListView();
            ListAdapter adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, context.getSearchResult());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int which, long l) {
                    // The 'which' argument contains the
                    // index position of the selected item
                    if (context.getLine().contains("=\"") && context.getLine().contains("=\"") && context.getLine().contains("\">")) {
                        SeverIO.getSong(context.getTempResult()[which].substring(
                                context.getTempResult()[which]
                                        .lastIndexOf("=\"") + 2,
                                context.getTempResult()[which]
                                        .lastIndexOf("\">")), context);
                        context.setSongSelected(true);
                    } else {
                        infoDialog(context.getString(R.string.msg_error_on_add), context);
                    }
                    dialog.dismiss();
                }
            });
            csd.getNeutral().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        } else {
            // Show message is no songs were found
            infoDialog(context.getResources().getString(R.string.msg_no_songs_message), context);
        }
    }

    // Dialog to ask if the user wants to add the selected song to the schedule
    public void addSongToScheduleDialog(String string, final String songNumber, final MainActivity context) {
        DefaultDialog alert = new DefaultDialog(context, Html.fromHtml(string).toString(), "", context.getResources().getString(R.string.action_yes_label), context.getResources().getString(R.string.action_no_label), context.getResources().getString(R.string.action_add_go_live), false, false);
        alert.setTitle(context.getResources().getString(R.string.msg_add_song));
        final AlertDialog dialog = alert.getAlertDialog();
        final MenuItem searchMenuItem = context.getSearchViewMenu()
                .findItem(R.id.action_search);
        final SearchView sv = (SearchView) searchMenuItem.getActionView();
        context.setResultIsOpen(true);

        alert.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context.getResultView().getVisibility() == View.GONE)
                    showResultInDialog(context);
                dialog.dismiss();
                context.setResultIsOpen(false);
            }
        });

        alert.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SeverIO.loadSong(songNumber, context);

                if (!context.isCanJump()) {
                    for (int i = context.getActiveItem(); i < (context.getScheduleList().size()); i++)
                        SeverIO.nextItem(context);
                } else {
                    SeverIO.gotoItem(context.getScheduleList().size(), context.getActiveItem(), context);
                }

                final Animation nextAnim = AnimationUtils.loadAnimation(context, R.anim.next);
                context.getLyricsListView().startAnimation(nextAnim);
                context.setSlide(true);
                context.getLyricsListView().setAdapter(context.getLyricsAdapter());
                searchMenuItem.collapseActionView();
                sv.setQuery("", false);
                context.setResultIsOpen(false);
                context.getResultView().setVisibility(View.GONE);
                dialog.dismiss();
            }
        });

        alert.getYes().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SeverIO.loadSong(songNumber, context);
                dialog.dismiss();
                searchMenuItem.collapseActionView();
                sv.setQuery("", false);
                context.setResultIsOpen(false);
                context.getResultView().setVisibility(View.GONE);
            }
        });
    }

    public void missingTranslationDialog(String message, final MainActivity context) {
        DefaultDialog cd = new DefaultDialog(context, message, "", context.getResources().getString(R.string.action_yes_label), context.getResources().getString(R.string.action_no_label), "Later", false, false);
        final AlertDialog dialog = cd.getAlertDialog();

        cd.getNo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                context.getSettings().saveSetting("showTranslationQuestion", false);
                context.continueLoading();
            }
        });

        cd.getYes().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                context.startActivity(new Intent(context, TranslationActivity.class));
            }
        });

        cd.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int qCount = context.getSettings().getTranslationQuestionDelay();
                if (qCount >= 4) {
                    context.getSettings().saveSetting("translationQuestionDelay", 1);
                } else {
                    context.getSettings().saveSetting("translationQuestionDelay", (qCount + 1));
                }
                context.continueLoading();
            }
        });
    }

    private void closeApp(final MainActivity context) {
        context.finish();
        if (context.getSyncHandler() != null)
            context.getSyncHandler().stopSync();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        System.exit(0);
    }


}
