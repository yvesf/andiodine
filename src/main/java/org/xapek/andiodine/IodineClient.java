package org.xapek.andiodine;

import android.content.Intent;
import android.util.Log;

public class IodineClient {
    public static final String TAG = "NATIVE";

    public static native int getDnsFd();

    public static native int connect(String nameserv_addr, String topdomain, boolean raw_mode, boolean lazy_mode,
                                     String password);

    public static native String getIp();

    public static native String getRemoteIp();

    public static native int getNetbits();

    public static native int getMtu();

    public static native int tunnel(int fd);

    public static native void tunnelInterrupt();

    public static native String getPropertyNetDns1();

    /**
     * Intent to distribute logmessages from native code
     * LOG_MESSAGE(EXTRA_MESSAGE)
     */
    public static final String ACTION_LOG_MESSAGE = "org.xapek.andiodine.IodineClient.ACTION_LOG_MESSAGE";

    public static final String EXTRA_MESSAGE = "message";

    @SuppressWarnings("UnusedDeclaration")
    public static void log_callback(String message) {
        Intent intent = new Intent(ACTION_LOG_MESSAGE);

        intent.putExtra(EXTRA_MESSAGE, message);
        if (IodineVpnService.instance != null) {
            IodineVpnService.instance.sendBroadcast(intent);
        } else {
            Log.d(TAG, "No VPNService running, cannot broadcast native message");
        }

    }

    static {
        System.loadLibrary("iodine-client");
        Log.d(TAG, "Native Library iodine-client loaded");
    }
}