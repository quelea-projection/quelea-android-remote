package org.quelea.mobileremote.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.quelea.mobileremote.activities.MainActivity;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static android.content.Context.WIFI_SERVICE;

/**
 * Network utilities.
 * @author Arvid
 */
public class UtilsNetwork {
    public static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            Log.e("Pattern", "Failed compiling pattern: " + e);
        }
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        assert wifiMgr != null;
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            Log.d("CheckWifi", "Id: " + wifiInfo.getNetworkId());
            Log.d("CheckWifi", "Network: " + wifiInfo.getSSID());
            int ipAddress = wifiInfo.getIpAddress();
            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipAddress = Integer.reverseBytes(ipAddress);
            }

            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

            String ipAddressString;
            try {
                ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
            } catch (UnknownHostException ex) {
                Log.e("WIFIIP", "Unable to get host address.");
                ipAddressString = null;
            }

            Log.d("CheckWifi", "Ip: " + ipAddressString);

            return wifiInfo.getNetworkId() != -1;
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static String matchIP(String ip) {
        Matcher m1;
        if (ip.length() > 7 && ip.substring(7).contains(":"))
            m1 = VALID_IPV4_PATTERN
                    .matcher(ip.substring(7,
                            ip.lastIndexOf(":")));
        else
            m1 = VALID_IPV4_PATTERN
                    .matcher(ip);
        if (!m1.matches()) {
            Matcher m2;
            if (ip.contains("%") && ip.length() > 7)
                m2 = VALID_IPV6_PATTERN
                        .matcher(ip.substring(7,
                                ip.lastIndexOf("%")));
            else if (ip.length() > 7 && ip.substring(7).contains(":"))
                m2 = VALID_IPV6_PATTERN
                        .matcher(ip.substring(7,
                                ip.lastIndexOf(":")));
            else
                m2 = VALID_IPV6_PATTERN
                        .matcher(ip);
            if (m2.matches()) {
                return "ipv6";
            }
        }
        if (!ip.contains(":")) {
            return "false";
        }
        return "true";
    }

    // Method to search through the 999 first IP numbers of the network
    // that the user is connected to.
    // By default the port is 50015, but can be changed in Quelea and
    // should be possible to change here as well at some point.
    public static String autoConnect(MainActivity context) {
        final ExecutorService es = Executors.newFixedThreadPool(20);
        String ip = getIPAddress(true);
        String found = "";
        if (ip.contains(".")) {
            ip = ip.substring(0, ip.lastIndexOf(".")) + ".";
            final int timeout = Integer.parseInt(context.getSettings().getAutoTimeout());
            final List<Future<String>> futures = new ArrayList<>();

            // Timeout is set by the user in settings but is by default set to 0.5 s.
            for (int i = 1; i <= 999; i++) {
                int port = 50015;
                futures.add(portIsOpen(es, ip + i, port, timeout));
                Log.d("Auto-connect", "Trying to connect to: " + ip + i);
            }
            es.shutdown();
            for (final Future<String> f : futures) {
                try {
                    if (!f.get().equals("")) {
                        found = f.get();
                        Log.d("Auto-connect", "ip is: " + found);
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return found;

    }

    // Auto-connect method to check if port is responding
    private static Future<String> portIsOpen(final ExecutorService es,
                                             final String ip, final int port, final int timeout) {
        return es.submit(new Callable<String>() {
            @Override
            public String call() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    socket.close();
                    return ip;
                } catch (Exception ex) {
                    return "";
                }
            }
        });
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    @SuppressWarnings("SameParameterValue")
    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
