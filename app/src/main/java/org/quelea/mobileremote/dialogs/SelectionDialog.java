package org.quelea.mobileremote.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.quelea.mobileremote.R;

/**
 * Created by Arvid on 2016-01-18.
 */
@SuppressWarnings("unused")
public class SelectionDialog {
    private AlertDialog alertDialog;
    private Button yes;
    private Button no;
    private Button neutral;
    private ListView listView;

    Button getNeutral() {
        return neutral;
    }

    public Button getNo() {
        return no;
    }

    public Button getYes() {
        return yes;
    }

    AlertDialog getAlertDialog() {
        return alertDialog;
    }

    ListView getListView() {
        return listView;
    }

    /**
     * Custom selection dialog default for the app with up to three buttons
     * and a list view that can be setup through getListView().
     *
     * @param context       app context
     * @param message       message to be displayed (no title is used)
     * @param buttonYes     text for positive button, null means not visible
     * @param buttonNo      text for negative button, null means not visible
     * @param buttonNeutral text for neutral button, null means not visible
     */

    SelectionDialog(Context context, String message, @Nullable String buttonYes, @Nullable String buttonNo, @Nullable String buttonNeutral) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View customDialog = factory.inflate(
                R.layout.dialog_select, null);
        alert.setView(customDialog);
        yes = customDialog.findViewById(R.id.btn_yes);
        no = customDialog.findViewById(R.id.btn_no);
        neutral = customDialog.findViewById(R.id.btn_neutral);
        listView = customDialog.findViewById(R.id.dialogListView);

        TextView tv = customDialog.findViewById(R.id.selectTitle);
        Typeface bold = Typeface.createFromAsset(context.getAssets(), "OpenSans-Bold.ttf");
        tv.setTypeface(bold, Typeface.BOLD);
        tv.setText(message);


        alert.setCancelable(false);

        yes.setText(buttonYes);

        no.setText(buttonNo);

        neutral.setText(buttonNeutral);

        alertDialog = alert.show();

        if (buttonNeutral == null || buttonNeutral.equals("")) {
            neutral.setVisibility(View.GONE);
        } else {
            neutral.setVisibility(View.VISIBLE);
        }

        if (buttonYes == null || buttonYes.equals("")) {
            yes.setVisibility(View.GONE);
        } else {
            yes.setVisibility(View.VISIBLE);
        }

        if (buttonNo == null || buttonNo.equals("")) {
            no.setVisibility(View.GONE);
        } else {
            no.setVisibility(View.VISIBLE);
        }

    }

}
