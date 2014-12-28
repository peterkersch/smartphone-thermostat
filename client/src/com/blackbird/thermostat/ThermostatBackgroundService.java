package com.blackbird.thermostat;

import java.util.ArrayList;
import java.util.List;

import com.blackbird.thermostat.protocol.ThermostatClientProtocol;
import com.blackbird.thermostat.receiver.AlarmReceiver;
import com.blackbird.thermostat.security.ThermostatClientSecurityManager;
import com.blackbird.thermostat.state.BroadcastEventManager;
import com.blackbird.thermostat.state.ControlStateProvider;
import com.blackbird.thermostat.state.ThermostatSensorManager;
import com.blackbird.thermostat.store.ThermostatProfile;
import com.blackbird.thermostat.store.ThermostatProfileStore;
import com.blackbird.thermostat.technology.TechnologySelector;
import com.crittercism.app.Crittercism;
import com.thermostat.protocol.ThermostatProtocol;
import com.thermostat.protocol.ThermostatSocket;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ResidentStatusInfo;
import com.thermostat.protocol.data.ZoneData;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ThermostatBackgroundService extends Service {

	private static final String TAG = ThermostatBackgroundService.class.getSimpleName();

	// Heartbeat period 
	public static final long HEARTBEAT_PERIOD_MS = 1000*60*2;

	// Jitter to avoid potential issues from synchronized heartbeats and give the system flexibility to schedule alarms (from API level 19)  
	private static final double JITTER_RATIO = 0.5;

	private boolean initialized = false;

	private TechnologySelector technologySelector;
	
	private ControlStateProvider controlStateProvider;
	
	private ThermostatSensorManager sensorManager;
	
	private BroadcastEventManager broadcastManager;
	
	private ThermostatClientSecurityManager securityManager;
	
	private ThermostatProfileStore thermostats = new ThermostatProfileStore();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!initialized) {
			try {
				technologySelector = new TechnologySelector(this, thermostats);
				controlStateProvider = ThermostatController.getInstance();
				sensorManager = new ThermostatSensorManager(this);
				broadcastManager = new BroadcastEventManager(this);
				securityManager = new ThermostatClientSecurityManager(getBaseContext());
				Log.d(TAG, "Own public key: " + securityManager.getPublicKeyString());
			} catch (Exception e) {
				// TBD: display error message to the users
				Log.e(TAG, "Exception during initialization", e);
				Crittercism.logHandledException(e);
				stopSelf();
				return Service.START_NOT_STICKY;
			}

			// Register for zone update local broadcasts (from ThermostatClientProtocol)
			IntentFilter zoneIntentFilter = new IntentFilter(Constants.ZONE_UPDATE_ACTION);
			LocalBroadcastManager.getInstance(this).registerReceiver(new ZoneBroadcastReceiver(), zoneIntentFilter);
			// Register for state update local broadcasts (from the GUI)
			IntentFilter stateIntentFilter = new IntentFilter(Constants.STATE_UPDATE_ACTION);
			LocalBroadcastManager.getInstance(this).registerReceiver(new AlarmReceiver(), stateIntentFilter);

			// Notify the system that this service should not be killed
			createForegroundNotification();
			
			initialized = true;
			Log.i(TAG, "Initialization completed");
		}
		
		// Send heart beat message
		Thread t = new HeartbeatMessenger(intent);
		t.start();
		
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		technologySelector.onDestroy();
		super.onDestroy();
	}
	
	private void createForegroundNotification() {
		NotificationCompat.Builder builder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Thermostat manager")
		        .setContentText("Listening to thermostat requests");
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);
		startForeground(1, builder.build());
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// not used
		return null;
	}

	/**
	 * Wait until all protocol instances are closed or a timeout period elapses
	 * 
	 * @param protocols
	 * @param timeoutMs
	 */
	private void waitForClosure(List<ThermostatProtocol> protocols, long timeoutMs) {
		long period = 200;
		for (long time = 0; time < timeoutMs; time += period) {
			boolean pending = false;
			for (ThermostatProtocol protocol : protocols) {
				if (!protocol.isClosed()) {
					pending = true;
					break;
				}
			}
			if (!pending) {
				return;
			}
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted while waiting for protocol instances to close", e);
				Crittercism.logHandledException(e);
				break;
			}
		}
		
		for (ThermostatProtocol protocol : protocols) {
			if (!protocol.isClosed()) {
				protocol.close();
				Log.w(TAG, "Timeout for " + protocol + ", force close");
			}
		}
		
	}

	private ResidentStatusInfo getStatusInfo() {
		// Start reading sensors
		sensorManager.start();
		
		try {
			// Wait until sensor events are available or 3 seconds elapsed
			int periodMs = 100;
			for (int i=0; i<3000 && !sensorManager.hasData(); i+=periodMs) {
				Thread.sleep(periodMs);
			}
		} catch (InterruptedException e) {
			Log.e(TAG, "Interrupted while waiting for sensor info", e);
			Crittercism.logHandledException(e);
		}
		
		return new ResidentStatusInfo(broadcastManager.getEvents(), sensorManager.getValues());
	}

	/**
	 * Sends a heart beat message to a Thermostat over Bluetooth
	 */
	private class HeartbeatMessenger extends Thread {
		
    	private Intent intent;

    	public HeartbeatMessenger(Intent intent) {
    		this.intent = intent;
		}
    	
    	@Override
    	public void run() {
    		
    		if (technologySelector.hasLocalConnectivity()) {
        		// Perform address discovery
        		technologySelector.startDiscovery();
        		
        		// Collect state information
    			ResidentState state = controlStateProvider.getCurrentState();
    			Log.d(TAG, "Reading sensor data");
    			ResidentStatusInfo status = getStatusInfo();
    			Log.d(TAG, "Reading sensor data completed");

    			List<ThermostatProtocol> protocols = new ArrayList<ThermostatProtocol>();
        		for (String id : thermostats.getThermostatIds()) {
    	    		// Send state update message to the given thermostat
        			ThermostatProfile thermostat = thermostats.getThermostatProfile(id);
        			ThermostatSocket socket = technologySelector.getThermostatSocket(thermostat);
        			String fingerprint = thermostat.getFingerprint();
        			if (socket == null) {
        				Log.d(TAG, "Thermostat " + fingerprint + " is not avaialable");
        				continue;
        			}
        			try {
        				ThermostatProtocol protocol = new ThermostatProtocol(socket);
        				protocols.add(protocol);
    					ThermostatClientProtocol clientProtocol = new ThermostatClientProtocol(ThermostatBackgroundService.this, protocol, securityManager);
    					clientProtocol.sendStateUpdate(state, status);
    					Log.d(TAG, "State update message sent to " + fingerprint + ", current state: " + state.getName());
    				} catch (Exception e) {
    	    			Log.e(TAG, "Error when sending state update message to " + fingerprint, e);
    					Crittercism.logHandledException(e);
    				}
        		}

        		// Wait max 3 seconds until all protocol instances are closed
        		waitForClosure(protocols, 3000);
    		}
    		
    		// Schedule wake up for next heartbeat (even if device is sleeping)
			scheduleNextAlarm();

			// Release wake lock
			Log.i(TAG, "Releasing wake lock");
			AlarmReceiver.completeWakefulIntent(intent);
    	}
    	
		private void scheduleNextAlarm() {
			AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(getBaseContext(), AlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, alarmIntent, 0);
			long currentTime = SystemClock.elapsedRealtime();
/*			if (Build.VERSION.SDK_INT >= 19) {
				long minWakeUpTime = (long)(HEARTBEAT_PERIOD_MS * (1-JITTER_RATIO));
				long maxWakeUpTime = (long)(HEARTBEAT_PERIOD_MS * (1+JITTER_RATIO));
				alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, minWakeUpTime, maxWakeUpTime, pendingIntent);				
			} else */ {
				long wakeUpTime = currentTime + (long)(HEARTBEAT_PERIOD_MS * (1.0+(Math.random()-0.5)*JITTER_RATIO));
				alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeUpTime, pendingIntent);				
			}
		}
	}
	
	private class ZoneBroadcastReceiver extends BroadcastReceiver {
		
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.ZONE_UPDATE_ACTION)) {
				ZoneData zoneData = (ZoneData)intent.getExtras().getSerializable(Constants.EXTENDED_ZONE_DATA);
				ZoneActionSender sender = new ZoneActionSender(zoneData);
				sender.start();
			}
		}
	}
	
	private class ZoneActionSender extends Thread {

		private ZoneData zoneData;
		
		public ZoneActionSender(ZoneData zoneData) {
			this.zoneData = zoneData;
		}
		
		@Override
		public void run() {
    		List<ThermostatProtocol> protocols = new ArrayList<ThermostatProtocol>();
    		// TODO: only send action to the selected thermostat
    		for (String id : thermostats.getThermostatIds()) {
	    		// Send state update message to the given thermostat
    			ThermostatProfile thermostat = thermostats.getThermostatProfile(id);
    			ThermostatSocket socket = technologySelector.getThermostatSocket(thermostat);
    			String fingerprint = thermostat.getFingerprint();
    			if (socket == null) {
    				Log.d(TAG, "Thermostat " + fingerprint + " is not avaialable");
    				continue;
    			}
    			try {
    				ThermostatProtocol protocol = new ThermostatProtocol(socket);
    				protocols.add(protocol);
					ThermostatClientProtocol clientProtocol = new ThermostatClientProtocol(ThermostatBackgroundService.this, protocol, securityManager);
					clientProtocol.sendZoneAction(zoneData);
					Log.i(TAG, "Zone action message sent to " + fingerprint);
				} catch (Exception e) {
	    			Log.e(TAG, "Error when sending zone action message to " + fingerprint, e);
					Crittercism.logHandledException(e);
				}
    		}

    		// Wait max 3 seconds until all protocol instances are closed
    		waitForClosure(protocols, 3000);
		}
	}
}
