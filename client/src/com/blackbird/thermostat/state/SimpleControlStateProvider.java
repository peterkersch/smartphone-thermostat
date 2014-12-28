package com.blackbird.thermostat.state;

import android.content.ContextWrapper;
import android.provider.Settings;
import com.thermostat.protocol.data.ResidentState;

/**
 * Very basic implementation of control state provider.
 * State = sleeping when alarm clock is set and awake otherwise. 
 * TODO: more sophisticated state provision based on sensors, broadcast events, etc.
 */
public class SimpleControlStateProvider implements ControlStateProvider {

	private ContextWrapper context;
	
	public SimpleControlStateProvider(ContextWrapper context) {
		this.context = context;
	}
	
	@Override
	public ResidentState getCurrentState() {
		String alarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		return alarm.length() > 0 ? ResidentState.HOME_SLEEPING : ResidentState.HOME_AWAKE;
	}
	
}
