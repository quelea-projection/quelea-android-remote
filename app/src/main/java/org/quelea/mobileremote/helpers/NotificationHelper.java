package org.quelea.mobileremote.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import org.quelea.mobileremote.R;
import org.quelea.mobileremote.receivers.NotificationReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.quelea.mobileremote.utils.UtilsMisc.HIDE;
import static org.quelea.mobileremote.utils.UtilsMisc.NEXT_ITEM;
import static org.quelea.mobileremote.utils.UtilsMisc.NEXT_SLIDE;
import static org.quelea.mobileremote.utils.UtilsMisc.PREVIOUS_ITEM;
import static org.quelea.mobileremote.utils.UtilsMisc.PREVIOUS_SLIDE;

/**
 * Class for setting up a notification remote. NB: Not finished!
 * @author Arvid
 */

public class NotificationHelper {
    private RemoteViews notificationView;
    public void startNotification(Context context) {
        notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
//        notificationView.setTextViewText(R.id.notification_text, currentlyDisplaying);

        // Next item button
        Intent niIntent = new Intent(context, NotificationReceiver.class);
        niIntent.setAction(NEXT_ITEM);
        PendingIntent pNIIntent = PendingIntent.getBroadcast(context, 12345, niIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_nextItem, pNIIntent);

        // Next slide button
        Intent nsIntent = new Intent(context, NotificationReceiver.class);
        nsIntent.setAction(NEXT_SLIDE);
        PendingIntent pNSIntent = PendingIntent.getBroadcast(context, 12345, nsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_nextSlide, pNSIntent);

        // Previous slide button
        Intent psIntent = new Intent(context, NotificationReceiver.class);
        psIntent.setAction(PREVIOUS_SLIDE);
        PendingIntent pPSIntent = PendingIntent.getBroadcast(context, 12345, psIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_previousSlide, pPSIntent);

        // Previous item button
        Intent piIntent = new Intent(context, NotificationReceiver.class);
        piIntent.setAction(PREVIOUS_ITEM);
        PendingIntent pPIIntent = PendingIntent.getBroadcast(context, 12345, piIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_previousItem, pPIIntent);

        // Hide button
        Intent hideIntent = new Intent(context, NotificationReceiver.class);
        hideIntent.setAction(HIDE);
        PendingIntent pHideIntent = PendingIntent.getBroadcast(context, 12345, hideIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationView.setOnClickPendingIntent(R.id.notification_hide, pHideIntent);

        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContent(notificationView);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, mBuilder.build());
        }
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
