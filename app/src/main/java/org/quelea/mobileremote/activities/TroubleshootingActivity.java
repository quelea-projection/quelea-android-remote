package org.quelea.mobileremote.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.quelea.mobileremote.dialogs.DefaultDialog;
import org.quelea.mobileremote.utils.OnSwipeTouchListener;
import org.quelea.mobileremote.R;
import org.quelea.mobileremote.crashHandling.CrashHandler;

import java.util.Arrays;

/**
 * Handle uncaught exceptions with a custom activity for sending in bug reports.
 * @author Arvid
 */
public class TroubleshootingActivity extends AppCompatActivity {
    int currentLayout = 0;
    Button next;
    Button nothingWorked;
    String[] layouts = new String[0];
    final int[] images = {R.drawable.troubleshoot_check_url, R.drawable.troubleshoot_check_wifi, R.drawable.troubleshoot_auto_connect_setting, R.drawable.troubleshoot_change_port, R.drawable.troubleshoot_firewall, R.drawable.troubleshoot_check_router};
    ImageView image;
    TextView text;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troubleshooting);
        String[] lines = {getString(R.string.troubleshoot_msg_check_url), getString(R.string.troubleshoot_msg_check_wifi), getString(R.string.troubleshoot_msg_auto_connect), getString(R.string.troubleshoot_msg_change_port), getString(R.string.troubleshoot_msg_disable_firewall), getString(R.string.troubleshoot_msg_check_router)};
        layouts = Arrays.copyOf(lines, lines.length);
        final DefaultDialog dialog = new DefaultDialog(TroubleshootingActivity.this, getString(R.string.troubleshoot_msg_guide_info), "", "", "", getResources().getString(R.string.action_ok_label), false, false);
        dialog.getNeutral().setText(getResources().getString(R.string.action_ok_label));
        dialog.getNeutral().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.getAlertDialog().dismiss();
            }
        });

        TextView title = findViewById(R.id.troubleshootTitle);
        title.setTypeface(Typeface.createFromAsset(TroubleshootingActivity.this.getAssets(), "OpenSans-Bold.ttf"));
        text = findViewById(R.id.troubleshootText);
        image = findViewById(R.id.troubleshootImage);
        nothingWorked = findViewById(R.id.nothingWorked);
        RelativeLayout relativeLayout = findViewById(R.id.troubleshootLayout);
        String tip = (currentLayout + 1) + ". " + layouts[currentLayout];
        text.setText(tip);
        image.setImageResource(images[currentLayout]);

        next = findViewById(R.id.nextTip);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNext();
            }
        });

        Button problemSolved = findViewById(R.id.problemFound);
        problemSolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLayout == 0) {
                    final String info = new CrashHandler(TroubleshootingActivity.this).getDebugInfo();
                    DefaultDialog report = new DefaultDialog(TroubleshootingActivity.this, getString(R.string.troubleshoot_msg_report_bug), "", getResources().getString(R.string.action_yes_label), getResources().getString(R.string.action_no_label), "", false, false);
                    final AlertDialog dial = report.getAlertDialog();
                    report.getYes().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dial.dismiss();
                            Intent sendIntent = new Intent(
                                    Intent.ACTION_SEND);
                            String subject = "Bug report/Connection problem: Quelea Mobile Remote";
                            sendIntent.setType("message/rfc822");
                            sendIntent.putExtra(Intent.EXTRA_EMAIL,
                                    new String[]{"arvid" + "@" + "quelea.org"});
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                                    subject);
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "" + info);
                            sendIntent.setType("message/rfc822");
                            startActivity(sendIntent);
                        }
                    });
                    report.getNo().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dial.dismiss();
                        }
                    });
                } else if (currentLayout > 2) {
                    DefaultDialog finished = new DefaultDialog(TroubleshootingActivity.this, getString(R.string.troubleshoot_msg_not_within_app), "", getResources().getString(R.string.action_ok_label), "", "", false, false);
                    finished.getYes().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                } else {
                    DefaultDialog finished = new DefaultDialog(TroubleshootingActivity.this, getString(R.string.troubleshoot_msg_finished), "", getResources().getString(R.string.action_ok_label), "", "", false, false);
                    finished.getYes().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                }
            }
        });
        Button previous = findViewById(R.id.previousTip);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoPrevious();
            }
        });


        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeRight() {
                gotoPrevious();
            }

            public void onSwipeLeft() {
                gotoNext();
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }

    private void gotoPrevious() {
        if (currentLayout > 0) {
            currentLayout--;
            String tip = (currentLayout + 1) + ". " + layouts[currentLayout];
            text.setText(tip);
            image.setImageResource(images[currentLayout]);
        }
    }

    private void gotoNext() {
        if (currentLayout < layouts.length - 1) {
            currentLayout++;
            String tip = (currentLayout + 1) + ". " + layouts[currentLayout];
            text.setText(tip);
            image.setImageResource(images[currentLayout]);
        }
        if (currentLayout == layouts.length - 1) {
            nothingWorked.setVisibility(View.VISIBLE);
            nothingWorked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DefaultDialog report = new DefaultDialog(TroubleshootingActivity.this, getString(R.string.troubleshoot_msg_nothing_worked), "", getResources().getString(R.string.action_yes_label), getResources().getString(R.string.action_no_label), "", false, false);
                    final AlertDialog dial = report.getAlertDialog();
                    report.getYes().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dial.dismiss();
                            Intent sendIntent = new Intent(
                                    Intent.ACTION_SEND);
                            String subject = "Bug report/Connection problem: Quelea Mobile Remote";
                            sendIntent.setType("message/rfc822");
                            sendIntent.putExtra(Intent.EXTRA_EMAIL,
                                    new String[]{"arvid" + "@" + "quelea.org"});
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                                    subject);
                            sendIntent.setType("message/rfc822");
                            startActivity(sendIntent);
                        }
                    });
                    report.getNo().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dial.dismiss();
                        }
                    });
                }
            });
        }
    }
}
