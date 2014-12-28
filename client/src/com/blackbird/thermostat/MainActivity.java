package com.blackbird.thermostat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.blackbird.thermostat.gui.StateSelector;
import com.blackbird.thermostat.gui.ZoneControl;
import com.crittercism.app.Crittercism;
import com.thermostat.protocol.data.ZoneData;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	DateFormat dateFormat = DateFormat.getDateTimeInstance();
	
	private ThermostatController controller;
	
	/** UI for zone data */
	private ZoneControl zoneControl;
	
	/** UI for state selection */
	private StateSelector stateSelector;

	private TextView status;
	
	private ZoneBroadcastReceiver broadcastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crittercism.initialize(getApplicationContext(), "52c47c64a7928a33ab000001");

		controller = ThermostatController.getInstance();
		
		// Load main view
		setContentView(R.layout.activity_main);
		zoneControl = (ZoneControl)findViewById(R.id.zoneControl);
		zoneControl.setController(controller);
		stateSelector = (StateSelector)findViewById(R.id.stateSelector);
		stateSelector.setController(controller);
		status = (TextView)findViewById(R.id.status1);
		
		// Register for zone update local broadcasts
		IntentFilter zoneIntentFilter = new IntentFilter(Constants.ZONE_REFRESH_ACTION);
		broadcastReceiver = new ZoneBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, zoneIntentFilter);
		Log.d(TAG, "onCreate() completed");
		
		// Start background service
		Log.i(TAG, "Refresh zone state uppon activity onCreate()");
		Intent intent = new Intent(this, ThermostatBackgroundService.class);
		startService(intent);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy() started");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Called when the user clicks the "Refresh" button */
	public void sendStateUpdate(View view) {
		Log.i(TAG, "Refresh zone state on user request");
		setStatus();
	    Intent intent = new Intent(Constants.STATE_UPDATE_ACTION);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void setStatus() {
		long timestamp = (new Date()).getTime();
		long lastTimestamp = controller.getLastZoneUpdate();
		if (lastTimestamp < 0) {
			status.setText("Away since last reboot");
			status.setTextColor(Color.RED);
		} else if (lastTimestamp + ThermostatController.DEFAULT_AWAY_TIMEOUT_MIN*60*1000 < timestamp) {
			status.setText("Away since " + dateFormat.format(new Date(lastTimestamp)));
			status.setTextColor(Color.RED);
		} else {
			status.setText("At home");
			status.setTextColor(Color.BLUE);
		}
	}
	
	private class ZoneBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.ZONE_REFRESH_ACTION)) {
				ArrayList<ZoneData> zones = (ArrayList<ZoneData>)intent.getExtras().getSerializable(Constants.EXTENDED_ZONE_DATA);
				for (ZoneData zone : zones) {
					zoneControl.updateZone(zone);
				}
				setStatus();
			}
		}
	}
}
