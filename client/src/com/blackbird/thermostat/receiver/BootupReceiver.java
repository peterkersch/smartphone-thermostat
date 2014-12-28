package com.blackbird.thermostat.receiver;

import com.blackbird.thermostat.ThermostatBackgroundService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver  {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
    	    Intent i = new Intent(context, ThermostatBackgroundService.class);
    	    context.startService(i);
        }
	}

}
