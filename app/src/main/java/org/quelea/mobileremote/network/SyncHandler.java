package org.quelea.mobileremote.network;

import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.utils.UtilsMisc;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Synchronization handler
 * Created by Arvid on 2017-12-16.
 */

public class SyncHandler {
    private ScheduledExecutorService scheduler;
    private MainActivity context;

    public SyncHandler(MainActivity context) {
        this.context = context;
    }

    private void autoRefresh() {

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Start checking for changes when successfully connected to the server
                if (context.isLoggedIn()) {
                    // Don't download changes until Quelea is fully started
                    if (!context.isFullyStarted()) {
                        SeverIO.downloadStatus(UtilsMisc.DownloadHtmlModes.STARTED, context);
                    } else {
                        // Check if any changes has been made
                        SeverIO.downloadStatus(UtilsMisc.DownloadHtmlModes.STATUS, context);
                        SeverIO.downloadSchedule(context);
                        SeverIO.downloadLyrics(context);
                    }
                }
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

    }

    public void startSync() {
        if (scheduler == null || scheduler.isShutdown()) {
            autoRefresh();
        }
    }

    public void stopSync() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

}
