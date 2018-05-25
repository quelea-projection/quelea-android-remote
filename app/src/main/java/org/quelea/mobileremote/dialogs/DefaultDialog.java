package org.quelea.mobileremote.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.quelea.mobileremote.R;

/**
 * Created by Arvid on 2016-01-18.
 */

public class DefaultDialog {
    /**
     * Custom dialog default for the app with up to three buttons, optional input and help icon.
     *
     * @param context       app context
     * @param message       message to be displayed (no title is used)
     * @param prefill       predefined value for input, can be empty but not null
     * @param buttonYes     text for positive button, null means not visible
     * @param buttonNo      text for negative button, null means not visible
     * @param buttonNeutral text for neutral button, null means not visible
     * @param useHelp       true for visible help icon, false otherwise
     * @param useInput      true for showing user text input dialog, false otherwise
     */
    public DefaultDialog(Context context, String message, @NonNull String prefill, @Nullable String buttonYes, @Nullable String buttonNo, @Nullable String buttonNeutral, boolean useHelp, boolean useInput) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View customDialog = factory.inflate(
                R.layout.dialog_input, null);
        alert.setView(customDialog);
        yes = customDialog.findViewById(R.id.btn_yes);
        no = customDialog.findViewById(R.id.btn_no);
        neutral = customDialog.findViewById(R.id.btn_neutral);
        help = customDialog.findViewById(R.id.helpButton);

        TextView tv = customDialog.findViewById(R.id.txt_dia);
        Typeface font = Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf");
        tv.setTypeface(font, Typeface.NORMAL);
        tv.setText(message);
        tv.setMovementMethod(new ScrollingMovementMethod());

        title = customDialog.findViewById(R.id.dialogTitle);

        // Set an EditText view to get user input
        input = customDialog.findViewById(R.id.edit_text);
        input.setSingleLine();
        if (!useInput) {
            input.setVisibility(View.GONE);
        } else {
            input.setText(prefill);
        }


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

        if (!useHelp) {
            help.setVisibility(View.GONE);
        }
    }

    private AlertDialog alertDialog;
    private Button yes;
    private Button no;
    private TextView title;
    private Button neutral;
    private EditText input;
    private ImageView help;

    ImageView getHelp() {
        return help;
    }

    public void setTitle(String text) {
        title.setText(text);
        if (!text.equals(""))
            title.setVisibility(View.VISIBLE);
        else
            title.setVisibility(View.GONE);
    }

    EditText getInput() {
        return input;
    }

    public Button getNeutral() {
        return neutral;
    }

    public Button getNo() {
        return no;
    }

    public Button getYes() {
        return yes;
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

}
