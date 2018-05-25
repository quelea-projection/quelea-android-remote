package org.quelea.mobileremote.crashHandling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import org.quelea.mobileremote.activities.MainActivity;
import org.quelea.mobileremote.utils.UtilsNetwork;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arvid on 2015-12-26.
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private Context context;

    public CrashHandler(Context ctx) {
        context = ctx;
    }

    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    private void addInformation(StringBuilder message) {
        message.append("Locale: ").append(Locale.getDefault()).append('\n');
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            message.append("Version: ").append(pi.versionName).append('\n');
            message.append("Package: ").append(pi.packageName).append('\n');
        } catch (Exception e) {
            Log.e("CustomExceptionHandler", "Error", e);
            message.append("Could not get Version information for ").append(
                    context.getPackageName());
        }
        message.append("Wifi status: ")
                .append(UtilsNetwork.checkWifiOnAndConnected(context)).append('\n');
        message.append("Phone Model: ").append(android.os.Build.MODEL)
                .append('\n');
        message.append("Android Version: ")
                .append(android.os.Build.VERSION.RELEASE).append('\n');
        message.append("Board: ").append(android.os.Build.BOARD).append('\n');
        message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
        message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
        message.append("Host: ").append(android.os.Build.HOST).append('\n');
        message.append("ID: ").append(android.os.Build.ID).append('\n');
        message.append("Model: ").append(android.os.Build.MODEL).append('\n');
        message.append("Product: ").append(android.os.Build.PRODUCT)
                .append('\n');
        message.append("Type: ").append(android.os.Build.TYPE).append('\n');
        StatFs stat = getStatFs();
        message.append("Total Internal memory: ")
                .append(getTotalInternalMemorySize(stat)).append('\n');
        message.append("Available Internal memory: ")
                .append(getAvailableInternalMemorySize(stat)).append('\n');
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Optional message to the developer:").append("\n");
            report.append('\n').append('\n').append('\n').append('\n');
            report.append("**** Report start ****").append("\n");
            report.append("Crash time: ")
                    .append(curDate.toString()).append('\n').append('\n');
            report.append("Error message/Stack trace:\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append("Variables:").append("\n");
            getVariables(report);
            report.append("\n");
            report.append("Device information:").append('\n');
            addInformation(report);
            report.append("\n");
            report.append("**** End of report ****");
            Log.e(CrashHandler.class.getName(),
                    "Error while sendErrorMail" + report);
            sendErrorMail(report);
        } catch (Throwable ignore) {
            Log.e(CrashHandler.class.getName(),
                    "Error while sending error e-mail", ignore);
        }
    }

    private void getVariables(StringBuilder report) {
        report.append("URL: ").append(((MainActivity)context).getSettings().getIp()).append("\n");
        report.append("Lyrics: ").append((MainActivity.getLastLyrics())).append("\n");
        report.append("Active verse: ").append(((MainActivity)context).getActiveVerse()).append("\n");
        report.append("Active item: ").append(MainActivity.getLastItem()).append("\n");
        report.append("Schedule: ").append(MainActivity.getLastSchedule()).append("\n");
        report.append("Temporary line: ").append(MainActivity.getLastLine()).append("\n");
    }

    /**
     * This method for call alert dialog when application crashed!
     */
    private void sendErrorMail(final StringBuilder errorContent) {
        Intent intent = new Intent(context, CrashActivity.class);
        intent.putExtra("error", errorContent.toString());
        context.startActivity(intent);
        ((Activity) context).finish();
        System.exit(2); //Prevents the service/app from freezing
    }

    public String getDebugInfo() {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Optional message to the developer:").append("\n");
            report.append('\n').append('\n');
            report.append("**** Report start ****").append("\n");
            report.append("Debug log time: ")
                    .append(curDate.toString()).append('\n').append('\n');
            report.append("Stack trace:\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            report.append("Variables:").append("\n");
            getVariables(report);
            report.append("\n");
            report.append("Device information:").append('\n');
            addInformation(report);
            report.append("\n");
            report.append("**** End of report ****");
            Log.e(CrashHandler.class.getName(),
                    "Error while sendErrorMail" + report);
            return report.toString();
        } catch (Throwable ignore) {
            Log.e(CrashHandler.class.getName(),
                    "Error while sending error e-mail", ignore);
        }
        return "";
    }
}
