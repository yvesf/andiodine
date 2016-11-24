package org.xapek.andiodine;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.xapek.andiodine.config.ConfigDatabase;
import org.xapek.andiodine.config.IodineConfiguration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IodineVpnService extends VpnService implements Runnable {
    private class IodineVpnException extends Exception {
        private static final long serialVersionUID = 32487871521160156L;

        IodineVpnException(String message, Throwable e) {
            super(message, e);
        }

        IodineVpnException(String message) {
            super(message);
        }
    }

    private static final String TAG = "VPN_SERVICE";

    public static IodineVpnService instance = null;

    /**
     * long
     */
    public static final String EXTRA_CONFIGURATION_ID = "configuration_id";
    /**
     * String
     */
    public static final String EXTRA_ERROR_MESSAGE = "message";

    /**
     * Intent to connect to VPN Connection
     * CONTROL_CONNECT(EXTRA_CONFIGURATION_ID)
     */

    public static final String ACTION_CONTROL_CONNECT = "org.xapek.andiodine.IodineVpnService.CONTROL_CONNECT";
    /**
     * Intent to close the vpn connection
     */
    public static final String ACTION_CONTROL_DISCONNECT = "org.xapek.andiodine.IodineVpnService.CONTROL_DISCONNECT";

    /**
     * Intent to request a new status update
     */
    public static final String ACTION_CONTROL_UPDATE = "org.xapek.andiodine.IodineVpnService.CONTROL_UPDATE";

    /**
     * Broadcast Action: The tunnel service is idle. This Action contains no
     * extras.
     */
    public static final String ACTION_STATUS_IDLE = "org.xapek.andiodine.IodineVpnService.STATUS_IDLE";

    /**
     * Broadcast Action: The user sent CONTROL_CONNECT and the vpn service is
     * trying to connect.
     *
     * @see #EXTRA_CONFIGURATION_ID
     */
    public static final String ACTION_STATUS_CONNECT = "org.xapek.andiodine.IodineVpnService.STATUS_CONNECT";
    /**
     * Broadcast Action: The tunnel is connected
     *
     * @see #EXTRA_CONFIGURATION_ID
     */
    public static final String ACTION_STATUS_CONNECTED = "org.xapek.andiodine.IodineVpnService.STATUS_CONNECTED";
    /**
     * Broadcast Action: The tunnel was disconnected
     *
     * @see #EXTRA_CONFIGURATION_ID
     */
    public static final String ACTION_STATUS_DISCONNECT = "org.xapek.andiodine.IodineVpnService.STATUS_DISCONNECT";
    /**
     * Broadcast Action: An error occured
     *
     * @see #EXTRA_CONFIGURATION_ID
     * @see #EXTRA_ERROR_MESSAGE
     */
    public static final String ACTION_STATUS_ERROR = "org.xapek.andiodine.IodineVpnService.STATUS_ERROR";

    private Thread mThread;
    private ConfigDatabase configDatabase;
    private IodineConfiguration mConfiguration;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONTROL_DISCONNECT.equals(intent.getAction())) {
                shutdown();
            } else if (ACTION_CONTROL_UPDATE.equals(intent.getAction())) {
                sendStatus();
            } else if (ACTION_CONTROL_CONNECT.equals(intent.getAction())) {
                if (mThread != null) {
                    setStatus(ACTION_STATUS_ERROR, -1L, getString(R.string.vpnservice_error_already_running));
                    return;
                }

                long configurationId = intent.getLongExtra(EXTRA_CONFIGURATION_ID, -1);
                if (configurationId == -1L) {
                    setStatus(ACTION_STATUS_ERROR, -1L, getString(R.string.vpnservice_error_configuration_incomplete));
                    return;
                }

                mConfiguration = configDatabase.selectById(configurationId);
                if (mConfiguration == null) {
                    setStatus(ACTION_STATUS_ERROR, configurationId,
                            getString(R.string.vpnservice_error_configuration_incomplete));
                    return;
                }

                mThread = new Thread(IodineVpnService.this, IodineVpnService.class.getName());
                mThread.start();
            }
        }
    };

    private String currentActionStatus = ACTION_STATUS_IDLE;

    private Long currentConfigurationId = null;

    private String currentMessage = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        configDatabase = new ConfigDatabase(this);

        IntentFilter filterInterruptAction = new IntentFilter();
        filterInterruptAction.addAction(ACTION_CONTROL_CONNECT);
        filterInterruptAction.addAction(ACTION_CONTROL_DISCONNECT);
        filterInterruptAction.addAction(ACTION_CONTROL_UPDATE);
        registerReceiver(broadcastReceiver, filterInterruptAction);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendStatus();
        return START_STICKY;
    }

    private void shutdown() {
    	if (mConfiguration != null)
    		setStatus(ACTION_STATUS_DISCONNECT, mConfiguration.getId(), null);
    	else
    		setStatus(ACTION_STATUS_IDLE, null, null);

        if (mThread != null) {
            mThread.interrupt();
            IodineClient.tunnelInterrupt();
        }
    }

    @Override
    public void onRevoke() {
        shutdown();
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
            IodineClient.tunnelInterrupt();
            mThread = null;
        }
        instance = null;
        configDatabase.close();
        unregisterReceiver(broadcastReceiver);
    }

    private void setStatus(String ACTION_STATUS, Long configurationId, String message) {
        currentActionStatus = ACTION_STATUS;
        currentConfigurationId = configurationId;
        currentMessage = message;
        sendStatus();
    }

    private void sendStatus() {
    	Log.d(TAG, "Send status: " + currentActionStatus);
        if (currentActionStatus != null) {
            Intent intent = new Intent(currentActionStatus);

            if (currentConfigurationId != null) {
                intent.putExtra(EXTRA_CONFIGURATION_ID, currentConfigurationId);
            }
            if (currentMessage != null) {
                intent.putExtra(EXTRA_ERROR_MESSAGE, currentMessage);
            }
            Log.d(TAG, "Send: " + intent);
            sendBroadcast(intent);
        }
    }

    @TargetApi(21)
    private Network getActiveNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo defaultNetworkInfo = cm.getActiveNetworkInfo();
        for (Network n : cm.getAllNetworks()) {
            NetworkInfo info = cm.getNetworkInfo(n);
            if (info.getType() == defaultNetworkInfo.getType() &&
                    info.getSubtype() == defaultNetworkInfo.getSubtype() &&
                    info.isConnected()) {
                return n;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, "VPN Thread enter");
            setStatus(ACTION_STATUS_CONNECT, mConfiguration.getId(), null);

            String tunnelNameserver = "";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager cm =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                Network activeNetwork = getActiveNetwork();
                if (activeNetwork == null) {
                    String errorMessage = getString(R.string.vpnservice_error_no_active_network);
                    setStatus(ACTION_STATUS_ERROR, mConfiguration.getId(), errorMessage);
                    Log.e(TAG, "No active network. Aborting...");
                    return;
                }
                LinkProperties prop = cm.getLinkProperties(activeNetwork);
                List<InetAddress> servers = prop.getDnsServers();
                for (InetAddress candidate : servers) {
                    Log.d(TAG, "detected dns server: " + candidate);
                    if (candidate instanceof Inet4Address) {
                        tunnelNameserver = candidate.getHostAddress();
                        break;
                    }
                }
            } else {
                tunnelNameserver = IodineClient.getPropertyNetDns1();
            }

            if (tunnelNameserver.isEmpty()) {
                String errorMessage = getString(R.string.vpnservice_error_dns_detect_failed);
                setStatus(ACTION_STATUS_ERROR, mConfiguration.getId(), errorMessage);
                Log.e(TAG, "No valid IPv4 name servers detected. Aborting...");
                return;
            }
            Log.d(TAG, "using name server: " + tunnelNameserver);
            if (!"".equals(mConfiguration.getTunnelNameserver())) {
                tunnelNameserver = mConfiguration.getTunnelNameserver();
            }
            String password = "";
            if (!"".equals(mConfiguration.getPassword())) {
                password = mConfiguration.getPassword();
            }

            int ret = IodineClient.connect(tunnelNameserver, mConfiguration.getTopDomain(), mConfiguration.getRawMode(),
                    mConfiguration.getLazyMode(), password, mConfiguration.getRequestHostnameSize(),
                    mConfiguration.getResponseFragmentSize());

            String errorMessage = "";
            switch (ret) {
                case 0:
                    Log.d(TAG, "Handshake successful");
                    setStatus(ACTION_STATUS_CONNECTED, currentConfigurationId, null);
                    runTunnel(); // this blocks until connection is closed
                    setStatus(ACTION_STATUS_IDLE, null, null);
                    break;
                case 1:
                    if (errorMessage.equals(""))
                        errorMessage = getString(R.string.vpnservice_error_cant_open_dnssocket);
                    // Fall through
                case 2:
                    if (errorMessage.equals(""))
                        errorMessage = getString(R.string.vpnservice_error_handshake_failed);
                    // fall through
                default:
                    if (errorMessage.equals(""))
                        errorMessage = String.format(getString(R.string.vpnservice_error_unknown_error_code), ret);

                    setStatus(ACTION_STATUS_ERROR, mConfiguration.getId(), errorMessage);
                    break;
            }
        } catch (IllegalStateException e) {
            String errorMessage = "IllegalStateException";
            if (e.getMessage().contains("Cannot create interface")) {
                errorMessage = getString(R.string.vpnservice_error_create_interface_string);
            } else {
                e.printStackTrace();
            }
            setStatus(ACTION_STATUS_ERROR, mConfiguration.getId(), errorMessage);
        } catch (IodineVpnException e) {
            e.printStackTrace();

            setStatus(ACTION_STATUS_ERROR, mConfiguration.getId(),
                    String.format(getString(R.string.vpnservice_error_unknown_error_string), e.getMessage()));
        } finally {
            mThread = null;
            mConfiguration = null;
            Log.d(TAG, "VPN Thread exit");
        }
    }

    private void runTunnel() throws IodineVpnException {
        Builder b = new Builder();
        b.setSession("Iodine VPN Service");

        String ip = IodineClient.getIp();
        int netbits = IodineClient.getNetbits();
        int mtu = IodineClient.getMtu();
        Log.d(TAG, "Build tunnel for configuration: ip=" + ip + " netbits=" + netbits + " mtu=" + mtu);

        String[] ipBytesString = ip.split("\\.");
        if (ipBytesString.length != 4) {
            throw new IodineVpnException("Server sent invalid IP");
        }
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            try {
                int integer = Integer.parseInt(ipBytesString[i]);
                ipBytes[i] = (byte) (integer);
            } catch (NumberFormatException e) {
                throw new IodineVpnException("Server sent invalid IP", e);
            }
        }

        InetAddress hostAddress;
        try {
            hostAddress = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            throw new IodineVpnException("Server sent invalid IP", e);
        }
        try {
            switch (mConfiguration.getNameserverMode()) {
                case LEAVE_DEFAULT:
                    // do nothing
                    break;
                case SET_SERVER_TUNNEL_IP:
                    b.addDnsServer(IodineClient.getRemoteIp());
                    break;
                case SET_CUSTOM:
                    b.addDnsServer(InetAddress.getByName(mConfiguration.getNameserver()));
                    break;
                default:
                    throw new IodineVpnException("Invalid Nameserver mode");
            }
        } catch (UnknownHostException e) {
            throw new IodineVpnException("Invalid Nameserver address", e);
        }

        b.addAddress(hostAddress, netbits);
        if (mConfiguration.getDefaultRoute()) {
            Log.d(TAG, "Set default route");
            b.addRoute("0.0.0.0", 0); // Default Route
        }
        b.setMtu(mtu);

		Log.d(TAG, "Build tunnel interface");
		ParcelFileDescriptor parcelFD;
		try {
			parcelFD = b.establish();
		} catch (Exception e) {
			if (e.getMessage().contains("fwmark") || e.getMessage().contains("iptables")) {
				throw new IodineVpnException(
						"Error while creating interface, please check issue #9 at https://github.com/yvesf/andiodine/issues/9");
			} else {
				throw new IodineVpnException("Error while creating interface: "
						+ e.getMessage());
			}
		}

        protect(IodineClient.getDnsFd());

        int tun_fd = parcelFD.detachFd();

        setStatus(ACTION_STATUS_CONNECTED, mConfiguration.getId(), null);

		Log.d(TAG, "Tunnel active");
		IodineClient.tunnel(tun_fd);
		try {
			ParcelFileDescriptor.adoptFd(tun_fd).close();
		} catch (IOException e) {
			throw new IodineVpnException(
					"Failed to close fd after tunnel exited");
		}
	}
}
