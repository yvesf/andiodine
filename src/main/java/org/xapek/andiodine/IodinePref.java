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
        addPreference(ConfigDatabase.COLUMN_CONF_NAME, getString(R.string.pref_text_name_label_short), R.string.pref_help_name, "New Connection");
        // Topdomain
        addPreference(ConfigDatabase.COLUMN_CONF_TOP_DOMAIN, getString(R.string.pref_text_topdomain_label), R.string.pref_help_topdomain,
                "tun.example.com");
        // Password
        addPreference(ConfigDatabase.COLUMN_CONF_PASSWORD, getString(R.string.pref_text_password_label), R.string.pref_help_password, "");
        // Tunnel Nameserver
        addPreference(ConfigDatabase.COLUMN_CONF_TUNNEL_NAMESERVER, getString(R.string.pref_text_tunnel_nameserver_label_or_empty),
                R.string.pref_help_tunnel_nameserver, "");
        // Nameserver Mode
        String[] nameserverModes = new String[NameserverMode.values().length];
        for (int i = 0; i < NameserverMode.values().length; i++) {
            nameserverModes[i] = NameserverMode.values()[i].name();
        }
        addPreference(ConfigDatabase.COLUMN_CONF_NAMESERVER_MODE, getString(R.string.pref_text_nameserver_mode_label),
                R.string.pref_help_nameserver_mode, nameserverModes, NameserverMode.LEAVE_DEFAULT.name());
        // Nameserver
        addPreference(ConfigDatabase.COLUMN_CONF_NAMESERVER, getString(R.string.pref_text_nameserver_label), R.string.pref_help_nameserver, "");
        // Request Type
        String[] requestTypes = new String[RequestType.values().length];
        for (int i = 0; i < RequestType.values().length; i++) {
            requestTypes[i] = RequestType.values()[i].name();
        }
        addPreference(ConfigDatabase.COLUMN_CONF_REQUEST_TYPE, getString(R.string.pref_text_request_type_label), R.string.pref_help_request_type,
                requestTypes, RequestType.AUTODETECT.name());
        // Lazy Mode
        addPreference(ConfigDatabase.COLUMN_CONF_LAZY_MODE, getString(R.string.pref_text_lazy_mode_label), R.string.pref_help_lazy, true);
        // Raw Mode
        addPreference(ConfigDatabase.COLUMN_CONF_RAW_MODE, getString(R.string.pref_text_raw_mode_label), R.string.pref_help_raw, false);
        // Default Route
        addPreference(ConfigDatabase.COLUMN_CONF_DEFAULT_ROUTE, getString(R.string.pref_text_default_route_label), R.string.pref_help_default_route, true);

        addPreference(ConfigDatabase.COLUMN_CONF_REQUEST_HOSTNAME_SIZE,
                getString(R.string.pref_text_request_hostname_size_label), R.string.pref_request_hostname_size, "255");
        addPreference(ConfigDatabase.COLUMN_CONF_RESPONSE_FRAGMENT_SIZE,
                getString(R.string.pref_text_response_fragment_size_label), R.string.pref_response_fragment_size, "0");
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
