package org.xapek.andiodine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.xapek.andiodine.config.ConfigDatabase;

import java.util.Scanner;

public class IodineMain extends Activity {
    private static final String TAG = "MAIN";
    private ConfigDatabase mConfigDatabase;

    private final FragmentList fragmentList = new FragmentList();
    private final FragmentStatus fragmentStatus = new FragmentStatus();

    private final BroadcastReceiver broadcastReceiverStatusUpdates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got intent: " + intent);
            if (IodineVpnService.ACTION_STATUS_ERROR.equals(intent.getAction())) {
                // Switch to List of Configurations Fragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment_status, fragmentList);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            } else if (IodineVpnService.ACTION_STATUS_IDLE.equals(intent.getAction())) {
                // Switch to List of Configurations Fragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment_status, fragmentList);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            } else if (IodineVpnService.ACTION_STATUS_CONNECT.equals(intent.getAction()) 
            		|| IodineVpnService.ACTION_STATUS_CONNECTED.equals(intent.getAction())) {
                // Switch to Status Fragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment_status, fragmentStatus);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mConfigDatabase = new ConfigDatabase(this);

        startService(new Intent(this, IodineVpnService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	IntentFilter intentFilterStatusUpdates = new IntentFilter();
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_CONNECT);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_CONNECTED);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_ERROR);
        intentFilterStatusUpdates.addAction(IodineVpnService.ACTION_STATUS_IDLE);
        registerReceiver(broadcastReceiverStatusUpdates, intentFilterStatusUpdates);
        
        Log.d(TAG, "Request CONTROL_UPDATE");
        sendBroadcast(new Intent(IodineVpnService.ACTION_CONTROL_UPDATE));
    }
    
    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiverStatusUpdates);
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        mConfigDatabase.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_about) {
            Scanner scanner = new Scanner(getResources().openRawResource(R.raw.license));
            scanner.useDelimiter("\\A");
            new AlertDialog.Builder(IodineMain.this)//
                    .setMessage(scanner.next()) //
                    .setCancelable(true)//
                    .create() //
                    .show();
            scanner.close();
        } else if (item.getItemId() == R.id.menu_main_add) {
            startActivity(new Intent(this, IodinePref.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
