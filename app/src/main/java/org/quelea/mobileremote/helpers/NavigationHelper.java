package org.quelea.mobileremote.helpers;

import android.annotation.SuppressLint;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import org.quelea.mobileremote.network.SeverIO;
import org.quelea.mobileremote.utils.OnSwipeTouchListener;
import org.quelea.mobileremote.R;
import org.quelea.mobileremote.activities.MainActivity;

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
                    if ((event.getAction() == KeyEvent.ACTION_DOWN)
                            && ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN) || (keyCode == KeyEvent.KEYCODE_PAGE_DOWN))) {
                        // Check if it should advance slide or item
                        if (context.getActiveVerse() < context.getVerseTotal()) {
                            SeverIO.nextSlide(context);
                        } else {
                            switch (context.getSettings().getAutoProgress()) {
                                case "progress":
                                    if (context.getActiveItem() != context.getItemsTotal() - 1)
                                        SeverIO.nextItem(context);
                                    break;
                                case "clear":
                                    SeverIO.clear(context);
                                    break;
                                case "black":
                                    SeverIO.black(context);
                                    break;
                                case "logo":
                                    SeverIO.logo(context);
                                    break;
                                default:
                                    break;
                            }
                        }
                        return true;
                    } else if ((event.getAction() == KeyEvent.ACTION_UP)
                            && ((keyCode == KeyEvent.KEYCODE_DPAD_UP) || (keyCode == KeyEvent.KEYCODE_PAGE_UP))) {
                        if (context.getActiveVerse() > 0) {
                            SeverIO.prevSlide(context);
                        } else if (context.getActiveItem() != 0) {
                            SeverIO.prevItem(context);
                        }
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
                    SeverIO.prevSlide(context);
                }
                if (context.getSettings().getUseSwipe().equals("item") && context.getActiveItem() != 0) {
                    SeverIO.prevItem(context);
                    Log.i("SlideRight", "Slide right. Active slide: " + context.getActiveVerse());
                }
            }

            public void onSwipeLeft() {
                if (context.getSettings().getUseSwipe().equals("slide")) {
                    SeverIO.nextSlide(context);
                }
                if (context.getSettings().getUseSwipe().equals("item") && context.getActiveItem() != context.getItemsTotal() - 1) {
                    SeverIO.nextItem(context);
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
            SeverIO.nextItem(context);
        } else if (context.getSettings().getDoublePress().equals("clear"))
            SeverIO.clear(context);
        else if (context.getSettings().getDoublePress().equals("black"))
            SeverIO.black(context);
        else if (context.getSettings().getDoublePress().equals("logo"))
            SeverIO.logo(context);
    }

    // Handle buttons being long pressed
    public void handleLongPress(String direction) {
        switch (context.getSettings().getLongClick()) {
            case "progress":
                if (direction.equals("next") && context.getActiveItem() != context.getItemsTotal() - 1) {
                    SeverIO.nextItem(context);
                }
                if (direction.equals("previous") && context.getActiveItem() != 0) {
                    SeverIO.prevItem(context);
                }
                break;
            case "clear":
                SeverIO.clear(context);
                break;
            case "black":
                SeverIO.black(context);
                break;
            case "logo":
                SeverIO.logo(context);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void buttonListerners() {
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
                SeverIO.logo(context);
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
                SeverIO.black(context);
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
                SeverIO.clear(context);
                context.getLyricsAdapter().notifyDataSetChanged();
                return true;
            }
        });

        ImageButton prev = context.findViewById(R.id.previousSlide);
        prev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SeverIO.prevSlide(context);
            }
        });

        ImageButton next = context.findViewById(R.id.nextSlide);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SeverIO.nextSlide(context);
            }
        });

        ImageButton previtem = context.findViewById(R.id.previousItem);
        previtem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (context.getActiveItem() != 0) {
                    SeverIO.prevItem(context);
                }
            }
        });

        ImageButton nextitem = context.findViewById(R.id.nextItem);
        nextitem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (context.getActiveItem() != context.getItemsTotal() - 1) {
                    SeverIO.nextItem(context);
                }
            }
        });
    }

    public boolean keyPress(int keyCode) {

        switch (keyCode) {
            // Handle volume keys
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // Check if hardware buttons are enabled
                if (settingsHelper.isUseVolume()) {
                    if (context.isNotLongPress() && context.isNotDoubleClicked()) {
                        // Check if it should advance slide or item
                        if (context.getActiveVerse() < context.getVerseTotal()) {
                            SeverIO.nextSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != context.getItemsTotal() - 1) {
                                SeverIO.nextItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                SeverIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                SeverIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                SeverIO.logo(context);
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
                            SeverIO.prevSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != 0) {
                                SeverIO.prevItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                SeverIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                SeverIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                SeverIO.logo(context);
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
                if (settingsHelper.isUseDpad()) {
                    if (context.isNotLongPress()) {
                        // Check if it should decrease slide or item
                        if (context.getActiveVerse() > 0) {
                            SeverIO.prevSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress") && context.getActiveItem() != 0) {
                                SeverIO.prevItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                SeverIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                SeverIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                SeverIO.logo(context);
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
                if (settingsHelper.isUseDpad()) {
                    if (context.isNotLongPress()) {
                        // Check if it should advance slide or item
                        if (context.getActiveVerse() < context.getVerseTotal()) {
                            SeverIO.nextSlide(context);
                        } else {
                            if (settingsHelper.getAutoProgress().equals("progress")
                                    && context.getActiveItem() != context.getItemsTotal() - 1) {
                                SeverIO.nextItem(context);
                            } else if (settingsHelper.getAutoProgress().equals("clear"))
                                SeverIO.clear(context);
                            else if (settingsHelper.getAutoProgress().equals("black"))
                                SeverIO.black(context);
                            else if (settingsHelper.getAutoProgress().equals("logo"))
                                SeverIO.logo(context);
                        }
                        return true;
                    } else {
                        context.setLongPress(false);
                        return false;
                    }
                } else
                    return false;
            case KeyEvent.KEYCODE_BACK:
                if (context.getScheduleDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                    context.getScheduleDrawerLayout().closeDrawer(GravityCompat.START);
                } else {
                    // Exit if drawer already is closed
                    context.getDialogsHelper().exitDialog(context);
                }
                return true;
            default:
                return false;

        }
    }

}
