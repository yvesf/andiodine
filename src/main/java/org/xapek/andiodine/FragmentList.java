package org.xapek.andiodine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.xapek.andiodine.config.ConfigDatabase;
import org.xapek.andiodine.config.IodineConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FragmentList extends Fragment {
    public static final String TAG = "FRAGMENT_LIST";

    private ListView mListView;
    private ConfigDatabase mConfigDatabase;
    private IodineConfiguration mSelectedConfiguration;
    private IodineConfigurationAdapter mAdapter;

    private static final int INTENT_REQUEST_CODE_PREPARE = 0;

    private class IodineConfigurationAdapter extends BaseAdapter {
        private List<IodineConfiguration> configurations;
        private final Set<DataSetObserver> observers = new HashSet<DataSetObserver>();

        public IodineConfigurationAdapter() {
            reload();
        }

        @Override
        public int getCount() {
            return configurations.size();
        }

        @Override
        public IodineConfiguration getItem(int position) {
            return configurations.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final IodineConfiguration item = getItem(position);
            View view = View.inflate(parent.getContext(), R.layout.configitem, null);
            ((TextView) view.findViewById(R.id.configitem_text_name)).setText(item.getName());
            ((TextView) view.findViewById(R.id.configitem_text_topdomain)).setText(item.getTopDomain());

            view.findViewById(R.id.configitem_btn_manage)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentList.this.vpnPreferences(item);
                        }
                    });

            view.findViewById(R.id.configitem_layout_name)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentList.this.vpnServiceConnect(item);
                        }
                    });

            return view;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            observers.add(observer);
        }

        private void reload() {
            this.configurations = mConfigDatabase.selectAll();
            triggerOnChanged();
        }

        public void triggerOnChanged() {
            for (DataSetObserver observer : observers) {
                observer.onChanged();
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            observers.remove(observer);
        }
    }

    private final BroadcastReceiver broadcastReceiverConfigurationChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IodinePref.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                // CONFIGURATION_CHANGED
                mAdapter.reload();
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mConfigDatabase = new ConfigDatabase(getActivity());
        mListView = (ListView) getActivity().findViewById(R.id.list_view);
        mAdapter = new IodineConfigurationAdapter();
        mListView.setAdapter(mAdapter);

        IntentFilter intentFilterConfigurationChanged = new IntentFilter();
        intentFilterConfigurationChanged.addAction(IodinePref.ACTION_CONFIGURATION_CHANGED);
        getActivity().registerReceiver(broadcastReceiverConfigurationChanged, intentFilterConfigurationChanged);

        setHasOptionsMenu(true); //activate onCreateOptionsMenu
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiverConfigurationChanged);
        mConfigDatabase.close();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_list, menu);
    }

    private void vpnPreferences(IodineConfiguration item) {
        Intent intent = new Intent(getActivity(), IodinePref.class);
        intent.putExtra(IodinePref.EXTRA_CONFIGURATION_ID, item.getId());
        startActivity(intent);
    }

    private void vpnServiceConnect(IodineConfiguration configuration) {
        Intent intent = IodineVpnService.prepare(getActivity());
        mSelectedConfiguration = configuration;
        if (intent != null) {
            // Ask for permission
            intent.putExtra(IodineVpnService.EXTRA_CONFIGURATION_ID, configuration.getId());
            startActivityForResult(intent, INTENT_REQUEST_CODE_PREPARE);
        } else {
            // Permission already granted
            new AlertDialog.Builder(getActivity()) //
                    .setTitle(R.string.warning) //
                    .setCancelable(true) //
                    .setMessage(getString(R.string.main_create_tunnel, configuration.getName()))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vpnServiceConnect2(mSelectedConfiguration);
                        }
                    }) //
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }) //
                    .create() //
                    .show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_CODE_PREPARE && resultCode == Activity.RESULT_OK) {
            vpnServiceConnect2(mSelectedConfiguration);
        }
    }

    private void vpnServiceConnect2(IodineConfiguration configuration) {
        Log.d(TAG, "Call VPN Service for configuration: " + configuration.getId());
        Intent intent = new Intent(IodineVpnService.ACTION_CONTROL_CONNECT);
        intent.putExtra(IodineVpnService.EXTRA_CONFIGURATION_ID, configuration.getId());
        getActivity().sendBroadcast(intent);
    }
}
