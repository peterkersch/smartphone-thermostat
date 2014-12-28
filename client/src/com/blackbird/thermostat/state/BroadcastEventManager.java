package com.blackbird.thermostat.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thermostat.protocol.data.BroadcastEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.Settings;

/**
 * Registers for broadcast events tightly bound to user state and stores these events.
 * Event list can be fetched and reset periodically  
 */
public class BroadcastEventManager extends BroadcastReceiver {

	private static final String SEPARATOR = "\t";
	
	private static final String DESK_CLOCK_ALARM_CHANGED = "android.intent.action.ALARM_CHANGED";
	
	private String lastAlarm = "";
	
	private List<BroadcastEvent> events = new ArrayList<BroadcastEvent>();
	
	public BroadcastEventManager(ContextWrapper context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(DESK_CLOCK_ALARM_CHANGED);
		context.registerReceiver(this, filter);

	}
	
	/**
	 * Get accumulated events and resets event list
	 * 
	 * @return the list of events collected since the last call to this method
	 */
	public synchronized List<BroadcastEvent> getEvents() {
		List<BroadcastEvent> oldEvents = events;
		events = new ArrayList<BroadcastEvent>();
		return oldEvents;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String name = intent.getAction();
		if (name.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
			name += SEPARATOR + intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, Integer.MAX_VALUE);
		} else if (name.equals(DESK_CLOCK_ALARM_CHANGED) && intent.getBooleanExtra("alarmSet", false)) {
			name += SEPARATOR + Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		}
		long timestamp = (new Date()).getTime();
		BroadcastEvent event = new BroadcastEvent(timestamp, name);
		synchronized (this) {
			events.add(event);
		}
		
//		checkAlarm(context, timestamp);
	}

	// Not needed when stock alarm clock is used since we get notifications
	private void checkAlarm(Context context, long timestamp) {
		String newAlarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		if (!newAlarm.equals(lastAlarm)) {
			synchronized (this) {
				events.add(new BroadcastEvent(timestamp, DESK_CLOCK_ALARM_CHANGED + "_" + newAlarm));
			}
			lastAlarm = newAlarm;
		}
	}

}
