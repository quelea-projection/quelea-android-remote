package org.quelea.mobileremote.helpers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.activities.SettingsActivity;
import org.quelea.mobileremote.network.ServerIO;
import org.quelea.mobileremote.utils.OnSwipeTouchListener;

import static org.quelea.mobileremote.activities.MainActivity.settingsHelper;

/**
 * Class to handle all types of clicks
 * Created by Arvid on 2017-11-29.
 */

public class NavigationHelper {
    private MainActivity context;

    public NavigationHelper(MainActivity context) {
        this.context = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void keyListener(ListView listView1) {
        // Listen for buttons pressed on keypad or external device, such as
        // AirTurn pedal or Bluetooth keyboard
        listView1.setOnKeyListener(new ListView.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event == null)
                    return false;
                Log.i("click", "Clicked something. useDpad? " + context.getSettings().isUseDpad() + " "
                        + event.getAction() + " keyevent.actiondown"
                        + KeyEvent.ACTION_DOWN + " event. KeyCode: " + keyCode
                        + " = " + KeyEvent.KEYCODE_DPAD_DOWN);
                // Check if hardware buttons are activated
                if (context.getSettings().isUseDpad()) {
                    if ((event.getAction() == KeyEvent.ACTION_UP)
                            && ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN) || (keyCode == KeyEvent.KEYCODE_PAGE_DOWN))) {
                        // Check if it should advance slide or item
                        if (event.isCtrlPressed())
                            ServerIO.nextItem(context);
                        else if (context.getActiveVerse() < context.getVerseTotal()) {
                            ServerIO.nextSlide(context);
                        } else {
                            switch (context.getSettings().getAutoProgress()) {
                                case "progress":
                                    if (context.getActiveItem() != context.getItemsTotal() - 1)
                                        ServerIO.nextItem(context);
                                    break;
                                case "clear":
                                    ServerIO.clear(context);
                                    break;
                                case "black":
                                    ServerIO.black(context);
                                    break;
                                case "logo":
                                    ServerIO.logo(context);
                                    break;
                                default:
                                    break;
                            }
                        }
                        return true;
                    } else if ((event.getAction() == KeyEvent.ACTION_UP)
                            && ((keyCode == KeyEvent.KEYCODE_DPAD_UP) || (keyCode == KeyEvent.KEYCODE_PAGE_UP))) {
                        if (event.isCtrlPressed())
                            ServerIO.prevItem(context);
                        else if (context.getActiveVerse() > 0) {
                            ServerIO.prevSlide(context);
                        } else if (context.getActiveItem() != 0) {
                            ServerIO.prevItem(context);
                        }
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_F5) {
                        ServerIO.logo(context);
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_F6) {
                        ServerIO.black(context);
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_F7) {
                        ServerIO.clear(context);
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode >= 8 && keyCode <= 16) {
                        ServerIO.loadInBackground(settingsHelper.getIp() + "/section" + (keyCode - 8), context);
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_T && event.isCtrlPressed()) {
                        context.startActivity(new Intent(context, SettingsActivity.class));
                        return true;
                    } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        context.getDialogsHelper().exitDialog(context);
                        return true;
                    } else
                        return false;
                } else
                    return false;

            }
        });


        listView1.setOnTouchListener(new OnSwipeTouchListener(context) {

            public void onSwipeRight() {
                if (context.getSettings().getUseSwipe().equals("slide")) {
                    ServerIO.prevSlide(context);
                }
                if (context.getSettings().getUseSwipe().equals("item") && context.getActiveItem() != 0) {
                    ServerIO.prevItem(context);
                    Log.i("SlideRight", "Slide right. Active slide: " + context.getActiveVerse());
                }
            }

            public void onSwipeLeft() {
                if (context.getSettings().getUseSwipe().equals("slide")) {
                    ServerIO.nextSlide(context);
                }
                if (context.getSettings().getUseSwipe().equals("item") && context.getActiveItem() != context.getItemsTotal() - 1) {
                    ServerIO.nextItem(context);
                    Log.i("SlideRight", "Slide left. Active slide: " + context.getActiveVerse());
                }
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }

    // Handle two buttons pressed simultaneously
    public void handleDoubleClick() {
        if (context.getSettings().getDoublePress().equals("progress") && context.getActiveItem() != context.getItemsTotal() - 1) {
            ServerIO.nextItem(context);
        } else if (context.getSettings().getDoublePress().equals("clear"))
            ServerIO.clear(context);
        else if (context.getSettings().getDoublePress().equals("black"))
            ServerIO.black(context);
        else if (context.getSettings().getDoublePress().equals("logo"))
            ServerIO.logo(context);
    }

    // Handle buttons being long pressed
    public void handleLongPress(String direction) {
        switch (context.getSettings().getLongClick()) {
            case "progress":
                if (direction.equals("next") && context.getActiveItem() != context.getItemsTotal() - 1) {
                    ServerIO.nextItem(context);
                }
                if (direction.equals("previous") && context.getActiveItem() != 0) {
                    ServerIO.prevItem(context);
                }
                break;
            case "clear":
                ServerIO.clear(context);
                break;
            case "black":
                ServerIO.black(context);
                break;
            case "logo":
                ServerIO.logo(context);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void buttonListeners() {
        // Check if buttons are clicked
        final ImageButton logo = context.findViewById(R.id.logo);

        logo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (context.getSettings().getTheme().equals("0"))
                    logo.setBackgroundResource(R.drawable.bnt_toggle_not_synced);
                else
                    logo.setBackgroundResource(R.drawable.btn_toggle_not_synced_dark);
                context.setLogoPressed(true);
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    return true;
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (!context.isLogoUsed()) {
                    logo.setPressed(true);
                    context.setLogoUsed(true);
                } else {
                    logo.setPressed(false);
                    context.setLogoUsed(false);
                }
                ServerIO.logo(context);
                context.getLyricsAdapter().notifyDataSetChanged();
                return true;
            }
        });

        final ImageButton black = context.findViewById(R.id.black);
        black.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (context.getSettings().getTheme().equals("0"))
                    black.setBackgroundResource(R.drawable.bnt_toggle_not_synced);
                else
                    black.setBackgroundResource(R.drawable.btn_toggle_not_synced_dark);
                context.setBlackPressed(true);
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    return true;
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (!context.isBlackUsed()) {
                    black.setPressed(true);
                    context.setBlackUsed(true);
                } else {
                    black.setPressed(false);
                    context.setBlackUsed(false);
                }
                ServerIO.black(context);
                context.getLyricsAdapter().notifyDataSetChanged();
                return true;
            }
        });

        final ImageButton clear = context.findViewById(R.id.clear);
        clear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (context.getSettings().getTheme().equals("0"))
                    clear.setBackgroundResource(R.drawable.bnt_toggle_not_synced);
                else
                    clear.setBackgroundResource(R.drawable.btn_toggle_not_synced_dark);
                context.setClearPressed(true);
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    return true;
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (!context.isClearUsed()) {
                    clear.setPressed(true);
                    context.setClearUsed(true);
                } else {
                    clear.setPressed(false);
                    context.setClearUsed(false);
                }
                ServerIO.clear(context);
                context.getLyricsAdapter().notifyDataSetChanged();
                return true;
            }
        });

        ImageButton prev = context.findViewById(R.id.previousSlide);
        prev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ServerIO.prevSlide(context);
            }
        });

        ImageButton next = context.findViewById(R.id.nextSlide);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ServerIO.nextSlide(context);
            }
        });

        ImageButton previtem = context.findViewById(R.id.previousItem);
        previtem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (context.getActiveItem() != 0) {
                    ServerIO.prevItem(context);
                }
            }
        });

        ImageButton nextitem = context.findViewById(R.id.nextItem);
        nextitem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (context.getActiveItem() != context.getItemsTotal() - 1) {
                    ServerIO.nextItem(context);
                }
            }
        });
    }

    public boolean keyPress(int keyCode, KeyEvent event) {

        switch (keyCode) {
            // Handle volume keys
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // Check if hardware buttons are enabled
                if (settingsHelper.isUseVolume()) {
                    if (context.isNotLongPress() && context.isNotDoubleClicked()) {
                        // Check if it should advance slide or item
                        if (context.getActiveVerse() < context.getVerseTotal()) {
                            ServerIO.nextSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != context.getItemsTotal() - 1) {
                                ServerIO.nextItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                ServerIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                ServerIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                ServerIO.logo(context);
                        }
                        return true;
                    } else {
                        context.setLogoPressed(false);
                        return false;
                    }
                } else
                    return false;

            case KeyEvent.KEYCODE_VOLUME_UP:
                // Check if hardware buttons are enabled
                if (settingsHelper.isUseVolume()) {
                    if (context.isNotLongPress() && context.isNotDoubleClicked()) {
                        // Check if it should decrease slide or item
                        if (context.getActiveVerse() > 0) {
                            ServerIO.prevSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != 0) {
                                ServerIO.prevItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                ServerIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                ServerIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                ServerIO.logo(context);
                        }
                        return true;
                    } else {
                        context.setLongPress(false);
                        return false;
                    }
                } else
                    return false;

                // Handle hardware joy-pad/keyboard arrow buttons - also enables
                // pedals (like AirTurn) to be used.
            case KeyEvent.KEYCODE_DPAD_UP:
                // Check if hardware buttons are enabled
                if (settingsHelper.isUseDpad() && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (context.isNotLongPress()) {
                        // Check if it should decrease slide or item
                        if (event.isCtrlPressed())
                            ServerIO.prevItem(context);
                        else if (context.getActiveVerse() > 0) {
                            ServerIO.prevSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != 0) {
                                ServerIO.prevItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                ServerIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                ServerIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                ServerIO.logo(context);
                        }
                        return true;
                    } else {
                        context.setLongPress(false);
                        return false;
                    }
                } else
                    return false;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Check if hardware buttons are enabled
                if (settingsHelper.isUseDpad() && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (context.isNotLongPress()) {
                        // Check if it should advance slide or item
                        if (event.isCtrlPressed())
                            ServerIO.nextItem(context);
                        else if (context.getActiveVerse() < context.getVerseTotal()) {
                            ServerIO.nextSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress")
                                    && context.getActiveItem() != context.getItemsTotal() - 1) {
                                ServerIO.nextItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                ServerIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                ServerIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                ServerIO.logo(context);
                        }
                        return true;
                    } else {
                        context.setLongPress(false);
                        return false;
                    }
                } else
                    return false;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (context.getScheduleDrawerLayout() != null && context.getScheduleDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                    context.getScheduleDrawerLayout().closeDrawer(GravityCompat.START);
                } else {
                    // Exit if drawer already is closed
                    context.getDialogsHelper().exitDialog(context);
                }
                return true;
            case KeyEvent.KEYCODE_F5:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    ServerIO.logo(context);
                return true;
            case KeyEvent.KEYCODE_F6:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    ServerIO.black(context);
                return true;
            case KeyEvent.KEYCODE_F7:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    ServerIO.clear(context);
                return true;
            case KeyEvent.KEYCODE_T:
                if (event.isCtrlPressed() && event.getAction() == KeyEvent.ACTION_DOWN)
                    context.startActivity(new Intent(context, SettingsActivity.class));
                return true;
            case KeyEvent.KEYCODE_ESCAPE:
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    context.getDialogsHelper().exitDialog(context);
                return true;
            default:
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode >= 8 && keyCode <= 16) {
                    ServerIO.loadInBackground(settingsHelper.getIp() + "/section" + (keyCode - 8), context);
                }
                return false;

        }
    }

}
