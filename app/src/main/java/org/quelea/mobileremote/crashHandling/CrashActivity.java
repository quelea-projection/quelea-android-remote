package org.quelea.mobileremote.crashHandling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.quelea.mobileremote.R;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        String title = getString(R.string.title_activity_crash);
        setTitle(title.replace("%s", getString(R.string.app_name_mr)));
        TextView tv = findViewById(R.id.crashText);
        StringBuilder sb = new StringBuilder();
        sb.append("<br/><b>").append(String.format(getResources().getString(R.string.title_activity_crash), getResources().getString(R.string.app_name_mr))).append("</b><br/>")
                .append(getResources().getString(R.string.msg_crash_body_1)).append("<br/><br/>")
                .append(getResources().getString(R.string.msg_crash_body_2)).append("<br/><br/>")
                .append(getResources().getString(R.string.msg_crash_body_3));
        tv.setText(Html.fromHtml(sb.toString()));
        tv.setMovementMethod(new ScrollingMovementMethod());
        Button exit = findViewById(R.id.exitButton);
        Button report = findViewById(R.id.reportButton);
        final String error = getIntent().getStringExtra("error");

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(
                        Intent.ACTION_SEND);
                String subject = "Crash report";
                sendIntent.setType("message/rfc822");
                sendIntent.putExtra(Intent.EXTRA_EMAIL,
                        new String[]{"arvid" + "@" + "quelea.org"});
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "" + error);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                        subject);
                sendIntent.setType("message/rfc822");
                try {
                    startActivity(sendIntent);
                } catch (Exception e) {
                    Log.e("Report", "Failed sending report");
                }
                finish();
            }
        });
    }
}
