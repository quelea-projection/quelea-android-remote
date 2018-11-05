package org.quelea.mobileremote.helpers;

import android.content.Context;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.network.ServerIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to handle all downloaded text to update UI.
 * Created by Arvid on 2017-11-29.
 */

public class ParseDownloadedTextHelper {
    private MainActivity context;
    private String tempStatus = "";

    public ParseDownloadedTextHelper(Context context) {
        this.context = (MainActivity) context;
    }

    /*
     * Methods for handling the downloaded html code
     */
    public void setSchedule(String line) {
        context.setLine(line);
        // Count number of items
        context.setItemsTotal((line.length() - line.replace("<br/>", "").length()) / 5);

        // Check if schedule is changed or not
        if (!line.equals(context.getTempCompare())) {
            // Calculate what item is active/preview.
            int stop;
            if (line.contains("<b>")) {
                stop = line.indexOf("<b>");
                context.setActiveItem(((line.substring(0, stop).length() - line
                        .substring(0, stop).replaceAll("<br/>", "").length()) / 5));
            } else
                context.setActiveItem(-1);
            if (line.contains("<i>")) {
                stop = line.indexOf("<i>");
                context.setPreviewItem(((line.substring(0, stop).length() - line
                        .substring(0, stop).replaceAll("<br/>", "").length()) / 5));
            } else
                context.setPreviewItem(-1);

            // Store new schedule for later comparison
            context.setTempCompare(line);

            // Create temporary array to later copy to scheduleList and remove HTML code
            ArrayList<String> tempArray = new ArrayList<>(
                    Arrays.asList(android.text.Html.fromHtml(line).toString().split("\\s*\n\\s*")));

            // Reload schedule
            context.reloadSchedule(tempArray);
        }
    }

    public void setLyrics(String line) {
        context.setLine(line);
        if (context.isOnline()) {
            TextView tv = context.findViewById(R.id.currentlyDisplaying);

            // Avoid text getting reset to default on rotate
            if (!context.getTempCurrentlyDisplaying().isEmpty() && !context.getTempCurrentlyDisplaying().equals(context.getString(R.string.msg_no_live_item)) && tv.getText().equals(context.getString(R.string.msg_no_live_item)))
                tv.setText(context.getResources().getString(R.string.msg_currently_displaying, context.getTempCurrentlyDisplaying()));

            // Check if lyrics page is empty
            if (line.contains("<br/></i>")) {

                // Check if the item is a video
                context.setPlayable(line.contains("playbutton"));

                // Check if the TextView with what is currently displaying
                // is equal to the downloaded lyrics
                if (line.contains(":")) {
                    String newItem = line.substring(line.indexOf(":"), line.indexOf("<br/></i>"));
                    if ((!context.getTempCurrentlyDisplaying().equals(newItem) || tv.getText().equals(context.getString(R.string.msg_no_live_item)))) {
                        context.getLyricsAdapter().imageLoader.clearCache();
                        context.setTempCurrentlyDisplaying(line.substring(line.indexOf(":"), line.indexOf("<br/></i>")));
                        // Update the name of the item if it has changed
                        String tempCurrent = line.substring(line.indexOf(":"),
                                line.indexOf("<br/></i>"));
                        tempCurrent = android.text.Html.fromHtml(
                                tempCurrent).toString();
                        tv.setText(context.getResources().getString(R.string.msg_currently_displaying, tempCurrent));
                        context.setPresentation(false);
                    }
                }
            } else {
                // If no item is live, change TextView to stored label
                tv.setText(R.string.msg_no_live_item);
                context.setActiveItem(-1);
            }

            // Check if lyrics has changed
            if (!context.getTempLyrics().equals(line)) {
                // Clean the new lyrics from html
                cleanLyrics(line);
                context.setTempLyrics(line);

                // Get and update schedule
                ServerIO.downloadSchedule(context);
            }
        } else {
            if (context.dialogNotShown())
                context.getDialogsHelper().enterURLDialog(
                        context.getResources().getString(
                                R.string.error_failed_finding_mr_server), context.getSettings().getIp(), context);
        }
    }

    private void cleanLyrics(String line) {
        context.setLine(line);
        context.getVerseLyrics().clear();

        // Calculate which slide is currently active
        if (line.contains("current\">")) {
            String current = line.substring(line.indexOf("current\">") + 43,
                    line.indexOf(")", line.indexOf("current\">") + 43));
            context.setActiveVerse(Integer.parseInt(current));
            context.getLyricsListView().smoothScrollToPosition(context.getActiveVerse() + 1);
        }
        context.setSlide(false);
        int verses = -1;

        if (line.contains("section") || line.contains("play")) {
            int p = 0;
            while (line.contains("section(" + p)) {
                verses++;
                p++;
            }

            // Enable presentation images
            if (line.contains(".ppt") || line.contains(".pdf") || line.contains(".png") || line.contains(".tiff") || line.contains(".jpg") || line.contains(".jpeg") || line.contains(".gif") || line.contains(".bmp")) {
                ServerIO.downloadSlides(context);
            }

            // Make play text clickable
            if (line.contains("playbutton")) {
                context.setPlayable(true);
                line = line.replace("play();", "location.href='" + context.getSettings().getIp()
                        + "/play'");
                line = line.replace(">Play<", ">Play/Pause<");
            } else
                context.setPlayable(false);

            // Assign total verses in item
            context.setVerseTotal(verses);

            // Clean text
            if (line.contains("<br/></i>"))
                context.setCurrentlyDisplaying(line.substring(3,
                        line.indexOf("<br/></i>")));

            // Remove the first line that says what currently is displayed
            // (this is displayed in it's own textview)
            line = line.replace(context.getCurrentlyDisplaying(), "");

            // Split verses to an ArrayList
            context.setVerseLyrics(new ArrayList<>(Arrays.asList(line
                    .split("</p></div>"))));
            context.getVerseLyrics().remove(context.getVerseLyrics().size() - 1);
            int i = 0;
            for (String s : context.getVerseLyrics()) {
                context.getVerseLyrics().set(i, Html.fromHtml(s).toString().replaceAll("￼", "").replaceAll("\n\n", ""));
                i++;
            }
        }

        context.getVerseLyrics().toArray(new String[context.getVerseLyrics().size()]);

        ArrayList<String> tempArray = new ArrayList<>(context.getVerseLyrics());

        // Reload lyrics
        context.reloadLyrics(tempArray);
    }

    public void handleButtonStatus(String line) {
        System.out.println(line);
        if (!tempStatus.equals(line)) {
            context.getLyricsAdapter().notifyDataSetChanged();
            tempStatus = line;
        }
        // Split up string to make it easier to compare
        List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
        if (items.size() > 1) {
            if (context.isLogoPressed() || context.isBlackPressed() || context.isClearPressed()) {
                if (items.get(0).equals(String.valueOf(context.isLogoUsed())))
                    context.setLogoPressed(false);
                if (items.get(1).equals(String.valueOf(context.isBlackUsed())))
                    context.setBlackPressed(false);
                if (items.get(2).equals(String.valueOf(context.isClearUsed())))
                    context.setClearPressed(false);
            } else {
                final ImageButton logo = context.findViewById(R.id.logo);
                if (context.getSettings().getTheme().equals("0"))
                    logo.setBackgroundResource(R.drawable.btn_toggle_synced);
                else
                    logo.setBackgroundResource(R.drawable.btn_toggle_synced_dark);
                // Check logo button changes
                if (items.get(0).equals("true")) {
                    context.setLogoUsed(true);
                    logo.setPressed(true);
                }
                if (items.get(0).equals("false")) {
                    context.setLogoUsed(false);
                    logo.setPressed(false);
                }

                final ImageButton black = context.findViewById(R.id.black);
                if (context.getSettings().getTheme().equals("0"))
                    black.setBackgroundResource(R.drawable.btn_toggle_synced);
                else
                    black.setBackgroundResource(R.drawable.btn_toggle_synced_dark);
                // Check black button changes
                if (items.get(1).equals("true")) {
                    context.setBlackUsed(true);
                    black.setPressed(true);
                }
                if (items.get(1).equals("false")) {
                    context.setBlackUsed(false);
                    black.setPressed(false);
                }

                final ImageButton clear = context.findViewById(R.id.clear);
                if (context.getSettings().getTheme().equals("0"))
                    clear.setBackgroundResource(R.drawable.btn_toggle_synced);
                else
                    clear.setBackgroundResource(R.drawable.btn_toggle_synced_dark);
                // Check clear button changes
                if (items.get(2).equals("true")) {
                    context.setClearUsed(true);
                    clear.setPressed(true);
                }
                if (items.get(2).equals("false")) {
                    context.setClearUsed(false);
                    clear.setPressed(false);
                }

                if (items.get(3).equals("Play") && context.isPlayable()) {
                    if (context.getLyrics().size() >= 1) {
                        context.getLyrics().set(0,
                                context.getResources().getString(R.string.action_play));
                        context.getLyricsAdapter().notifyDataSetChanged();
                    }
                } else if (items.get(3).equals("Pause") && context.isPlayable()) {
                    if (context.getLyrics().size() >= 1) {
                        context.getLyrics().set(0,
                                context.getResources().getString(R.string.action_pause));
                        context.getLyricsAdapter().notifyDataSetChanged();
                    }
                }

                // Check record button changes
                if (items.size() > 4) {
                    final MenuItem searchMenuItem = context.getSearchViewMenu()
                            .findItem(R.id.action_search);
                    if (searchMenuItem.isActionViewExpanded())
                        context.getRecord().setVisible(false);
                    else if (!context.getSettings().isDisableRec())
                        context.getRecord().setVisible(true);
                    if (items.get(4).contains("true")) {
                        context.getRecord().setIcon(R.drawable.btn_record_stop);
                        context.getRecord().setTitle(R.string.action_stop_recording);
                    } else {
                        context.getRecord().setIcon(R.drawable.btn_record);
                        context.getRecord().setTitle(R.string.action_start_recording);
                    }
                }
            }
        }
    }


    public void checkURL(String line) {
        // Look if page headline is Quelea Remote Control, else ask user to enter URL
        // again
        if (line.contains("Quelea Remote Control")) {
            context.setOnline(true);
            Toast.makeText(context.getApplicationContext(),
                    context.getResources().getString(R.string.msg_connected),
                    Toast.LENGTH_SHORT).show();

            // Close progress dialog if still open
            context.getProgressDialog().cancel();

            // Check if log in is needed
            if (line.contains("password\">")) {
                if (context.dialogNotShown())
                    context.getDialogsHelper().loginDialog(context);
            } else {
                context.setLoggedIn(true);
                ServerIO.downloadLyrics(context);
            }
        } else {
            // Check if auto-connect should be used
            context.setOnline(false);
            if (context.getSettings().isUseAutoConnect()) {
                context.initAutoConnect();
            } else {
                if (context.dialogNotShown())
                    context.getDialogsHelper().enterURLDialog(
                            context.getResources().getString(
                                    R.string.error_failed_finding_mr_server), context.getSettings().getIp(), context);
            }
        }
    }

    public void getSongSearchResult(String line) {
        // Check if user is returning from previous dialog
        if (!context.isSongSelected()) {
            // Clean result from html
            String cleaned = android.text.Html.fromHtml(line).toString();

            // Count number of results
            int found = cleaned.length()
                    - cleaned.replaceAll("\n", "").length();
            context.setSearchResult(cleaned.split("\n", found));
            context.setTempResult(line.split("<br/>", line.length()));
            if (line.contains("\"password\">")) {
                context.setLoggedIn(false);
                context.getDialogsHelper().loginDialog(context);
            } else {
                displaySearch();
            }
        } else {
            if (line.contains("=\"") && line.contains("<br/><br/><a href") && line.contains("</html")) {
                String songNumber = line.substring(line.lastIndexOf("=\"") + 2,
                        line.lastIndexOf("\">"));
                line = line.replaceAll(
                        line.substring(line.indexOf("<br/><br/><a href"),
                                line.indexOf("</html")), "");
                if (!context.isResultIsOpen()) {
                    context.getDialogsHelper().addSongToScheduleDialog(line, songNumber, context);
                    context.setSongSelected(false);
                }
            } else {
                context.getDialogsHelper().infoDialog(context.getString(R.string.msg_error_on_add), context);
            }
        }
    }

    public void getBibleAddResult(String line) {
        line = line.replaceAll("\n", "");
        // Display result message from server
        Toast.makeText(context.getApplicationContext(), line, Toast.LENGTH_LONG).show();
    }

    public void getBibleBooks(String line) {
        context.setLine(line);
        if (line.length() > 0)
            context.setBibleBooks(line.substring(0, line.length() - 1).split("\n",
                    line.length()));
        else
            context.setBibleBooks("".split(""));
        context.getDialogsHelper().searchDialog(MainActivity.SearchMode.BIBLE, context);
    }

    public void getBibleTranslations(String line) {
        context.setLine(line);
        if (line.length() > 0)
            context.setBibleTranslations(line.substring(0, line.length() - 1).split("\n",
                    line.length()));
        else
            context.setBibleTranslations("".split(""));
        context.getDialogsHelper().searchDialog(MainActivity.SearchMode.BIBLE, context);
    }


    public void downloadBooks(int which) {
        // The 'which' argument contains the
        // index position
        // of the selected item
        context.setSelectedTranslations(true);
        context.setBibleTranslation(context.getBibleTranslations()[which]);
        if (context.getBibleTranslation().contains("*"))
            context.setBibleTranslation(context.getBibleTranslation()
                    .replaceAll("\\*", ""));
        if (context.getBibleTranslation().contains(" "))
            context.setBibleTranslation(context.getBibleTranslation()
                    .replaceAll(" ", "%20"));
        // Download the books of the selected translation
        ServerIO.downloadBibleBooks(context);
    }

    // Show result in a ListView instead of a dialog
    private void displaySearch() {
        context.setResultView((ListView) context.findViewById(R.id.dropDownSearch));
        List<String> result = new ArrayList<>();
        if (context.getSearchResult()[0].isEmpty()) {
            result.add(context.getResources().getString(R.string.msg_no_songs_message));
        } else
            result = Arrays.asList(context.getSearchResult());
        ListAdapter searchAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_list_item_1, result);
        context.getResultView().setAdapter(searchAdapter);
        context.getResultView().setVisibility(View.VISIBLE);
        context.getResultView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id) {
                if (context.getTempResult()[position].contains("=\"") && context.getTempResult()[position].contains("\">")) {
                    String song = context.getTempResult()[position].substring(
                            context.getTempResult()[position]
                                    .lastIndexOf("=\"") + 2,
                            context.getTempResult()[position]
                                    .lastIndexOf("\">"));
                    ServerIO.getSong(song, context);
                    context.setSongSelected(true);
                } else {
                    context.getDialogsHelper().infoDialog(context.getString(R.string.msg_error_on_add), context);
                }
            }
        });

    }

}
