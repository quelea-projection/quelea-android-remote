package org.quelea.mobileremote.adapters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.items.TranslationLine;
import org.quelea.mobileremote.activities.TranslationActivity;

import java.util.List;

/**
 * Created by Arvid on 2018-01-27.
 */

public class TranslationAdapter extends ArrayAdapter<TranslationLine> {
    private TranslationActivity context;

    public TranslationAdapter(TranslationActivity context, int resource, List<TranslationLine> items) {
        super(context, resource, items);
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.item_translation_row, null);
            holder.original = convertView.findViewById(R.id.translationOriginal);
            holder.translation = convertView.findViewById(R.id.translationEdit);
            holder.warning = convertView.findViewById(R.id.translation_warning);
            holder.check = convertView.findViewById(R.id.translation_check);
            convertView.setTag(holder);
            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.getLines().get(holder.ref).setIgnoreWarning(true);
                    setUpColors(holder, context.getLines().get(holder.ref));
                    updateProgress();
                }
            });
            holder.warning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.original.getText().toString().trim().toLowerCase().equals(holder.translation.getText().toString().toLowerCase().trim())) {
                        warningDialog("The two translations are identical.", context.getLines().get(holder.ref), holder);
                    }
                    if (context.getLines().get(holder.ref).getOriginal().contains("%s") && !context.getLines().get(holder.ref).getTranslation().contains("%s")) {
                        warningDialog("The translation doesn't contain the %s combination. It symbolizes a string that automatically will be inserted and must exist in the translated string as well.", context.getLines().get(holder.ref), holder);
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ref = position;

        if (context.getLines().get(holder.ref) != null) {
            holder.original.setText(context.getLines().get(holder.ref).getOriginal());
            holder.translation.setText(context.getLines().get(holder.ref).getTranslation());
            holder.translation.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    context.getLines().get(holder.ref).setTranslation(arg0.toString());
                    setUpColors(holder, context.getLines().get(holder.ref));
                    if (context.getLines().get(holder.ref).hasChanged()) {
                        context.enableSave(true);
                        context.getLines().get(holder.ref).setIgnoreWarning(false);
                    }
                }
            });
            setUpColors(holder, context.getLines().get(holder.ref));
            updateProgress();

        }
        return convertView;
    }

    private void updateProgress() {
        int i = 0;
        for (TranslationLine tl : context.getLines()) {
            if (tl.isFinished())
                i++;
        }
        context.updateProgress(i);
    }

    private void setUpColors(final ViewHolder holder, final TranslationLine p) {
        if (holder.translation.getText().toString().trim().isEmpty()) {
            holder.translation.setBackgroundColor(Color.RED);
            holder.warning.setVisibility(View.INVISIBLE);
            holder.check.setVisibility(View.INVISIBLE);
            p.setIgnoreWarning(false);
        } else if (!p.isFinished()) {
            holder.translation.setBackgroundColor(Color.CYAN);
            holder.check.setVisibility(View.VISIBLE);
            if (p.getOriginal().toLowerCase().trim().equals(p.getTranslation().trim().toLowerCase()) || p.getOriginal().contains("%s") && !p.getTranslation().contains("%s")) {
                holder.warning.setVisibility(View.VISIBLE);
            } else {
                holder.warning.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.warning.setVisibility(View.INVISIBLE);
            holder.translation.setBackgroundColor(Color.GREEN);
            holder.check.setVisibility(View.INVISIBLE);
        }
    }

    private void warningDialog(String s, final TranslationLine p, final ViewHolder holder) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Warning");
        alertDialog.setMessage(s);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ignore warning",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        context.getLines().get(holder.ref).setIgnoreWarning(true);
                        context.getLines().get(holder.ref).setTranslation(holder.translation.getText().toString());
                        dialog.dismiss();
                        setUpColors(holder, context.getLines().get(holder.ref));
                        updateProgress();
                    }
                });
        alertDialog.show();
    }

    private class ViewHolder {
        TextView original;
        EditText translation;
        int ref;
        ImageView warning;
        ImageView check;
    }
}
