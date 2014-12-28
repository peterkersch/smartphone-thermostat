package com.blackbird.thermostat.receiver;

import com.blackbird.thermostat.ThermostatBackgroundService;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	private static final String TAG = AlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Firing refresh alarm");
	    Intent service = new Intent(context, ThermostatBackgroundService.class);
	    startWakefulService(context, service);
	}

}
