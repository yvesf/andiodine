package org.xapek.andiodine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.xapek.andiodine.config.ConfigDatabase;
import org.xapek.andiodine.config.IodineConfiguration;
import org.xapek.andiodine.config.IodineConfiguration.NameserverMode;
import org.xapek.andiodine.config.IodineConfiguration.RequestType;

public class IodinePref extends org.xapek.andiodine.preferences.PreferenceActivity {
    public static final String EXTRA_CONFIGURATION_ID = "uuid";
    public static final String ACTION_CONFIGURATION_CHANGED = "org.xapek.andiodine.preferences.PreferenceActivity.CONFIGURATION_CHANGED";

    private static final String TAG = "PREF";

    private ConfigDatabase mConfigDatabase;

    private IodineConfiguration mIodineConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mConfigDatabase = new ConfigDatabase(this);
        Long configurationId = getIntent().getLongExtra(EXTRA_CONFIGURATION_ID, -1);
        if (configurationId == null || configurationId == -1) {
            // Configuration ID is empty; create new configuration
            mIodineConfiguration = new IodineConfiguration();
        } else {
            mIodineConfiguration = mConfigDatabase.selectById(configurationId);
            if (mIodineConfiguration == null) {
                Log.e(TAG, "No configuration with uuid: " + configurationId + " found");
                finish();
            }
        }
        setContentValues(mIodineConfiguration.getContentValues());

        // Name
        addPreference(ConfigDatabase.COLUMN_CONF_NAME, "Name", R.string.pref_help_name, "New Connection");
        // Topdomain
        addPreference(ConfigDatabase.COLUMN_CONF_TOP_DOMAIN, "Tunnel Topdomain", R.string.pref_help_topdomain,
                "tun.example.com");
        // Password
        addPreference(ConfigDatabase.COLUMN_CONF_PASSWORD, "Password", R.string.pref_help_password, "");
        // Tunnel Nameserver
        addPreference(ConfigDatabase.COLUMN_CONF_TUNNEL_NAMESERVER, "Tunnel Nameserver (or empty)",
                R.string.pref_help_tunnel_nameserver, "");
        // Nameserver Mode
        String[] nameserverModes = new String[NameserverMode.values().length];
        for (int i = 0; i < NameserverMode.values().length; i++) {
            nameserverModes[i] = NameserverMode.values()[i].name();
        }
        addPreference(ConfigDatabase.COLUMN_CONF_NAMESERVER_MODE, "Nameserver Mode",
                R.string.pref_help_nameserver_mode, nameserverModes, NameserverMode.LEAVE_DEFAULT.name());
        // Nameserver
        addPreference(ConfigDatabase.COLUMN_CONF_NAMESERVER, "Nameserver", R.string.pref_help_nameserver, "");
        // Request Type
        String[] requestTypes = new String[RequestType.values().length];
        for (int i = 0; i < RequestType.values().length; i++) {
            requestTypes[i] = RequestType.values()[i].name();
        }
        addPreference(ConfigDatabase.COLUMN_CONF_REQUEST_TYPE, "Request Type", R.string.pref_help_request_type,
                requestTypes, RequestType.AUTODETECT.name());
        // Lazy Mode
        addPreference(ConfigDatabase.COLUMN_CONF_LAZY_MODE, "Lazy mode", R.string.pref_help_lazy, true);
        // Raw Mode
        addPreference(ConfigDatabase.COLUMN_CONF_RAW_MODE, "Raw Mode", R.string.pref_help_raw, false);
        // Default Route
        addPreference(ConfigDatabase.COLUMN_CONF_DEFAULT_ROUTE, "Default Route", R.string.pref_help_default_route, true);
    }

    @Override
    protected void onDestroy() {
        mConfigDatabase.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pref, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStop() {
        mConfigDatabase.insertOrUpdate(mIodineConfiguration.getContentValues());
        Intent intent = new Intent(ACTION_CONFIGURATION_CHANGED);
        intent.putExtra(EXTRA_CONFIGURATION_ID, mIodineConfiguration.getId());
        sendBroadcast(intent);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_pref_delete) {
            // Delete current connection
            if (mIodineConfiguration.getId() != null) {
                mConfigDatabase.delete(mIodineConfiguration.getContentValues());
            }
            finish();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
