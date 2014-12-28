package com.thermostat.server.temperature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.thermostat.protocol.data.ResidentState;
import com.thermostat.server.profile.ResidentProfile;
import com.thermostat.server.profile.ResidentProfileStoreFactory;
import com.thermostat.server.zone.ZoneManager;

/**
 * Derives target temperature for a given zone based on current state and temperature preferences of residents.
 */
public class TargetTemperatureCalculator {

	private static Logger logger = Logger.getLogger(TargetTemperatureCalculator.class.getSimpleName());

	public static final float getTargetTemperature(ZoneManager zone, boolean considerBoost) {
		String zoneName = zone.getZoneName();
		Collection<ResidentProfile> profiles = ResidentProfileStoreFactory.getResidentProfileStore().getProfiles().values();
		
		List<Float> homePreferences = new ArrayList<Float>(profiles.size());
		List<Float> awayPreferences = new ArrayList<Float>(profiles.size());
		
		logger.fine("Calculating target temperature for zone " + zoneName);
		
		for (ResidentProfile profile : profiles) {
			Map<ResidentState, Float> preferences = profile.getTemperaturePreferences(zoneName);
			if (preferences == null) {
				// No preferences defined for this zone by this resident
				continue;
			}
			ResidentState state = profile.getCurrentState();
			// Register current preferred temperature
			if (!state.equals(ResidentState.AWAY)) {
				Float homePreference = preferences.get(state);
				if (homePreference != null) {
					homePreferences.add(homePreference);
				}				
			}
			Float awayPreference = preferences.get(state);
			if (awayPreference != null) {
				awayPreferences.add(awayPreference);
			}
		}
		
		if (homePreferences.isEmpty()) {
			/* If nobody is at home then use the average of away temperatures */
			return average(awayPreferences);
		} else {
			/* Otherwise, use the max of home temperature preferences among residents being at home or the boost temperature */
			Float boostTemperature = zone.getBoostTemperature();
			return considerBoost && boostTemperature != null ? boostTemperature : max(homePreferences);
		}
	}
	
	private static final float average(List<Float> temperaturePreferences) {
		float sum = 0.0f;
		for (float t : temperaturePreferences) {
			sum += t;
		}
		return sum / temperaturePreferences.size();		
	}
	
	private static final float max(List<Float> temperaturePreferences) {
		float max = 0.0f;
		for (float t : temperaturePreferences) {
			if (t > max) {
				max = t;
			}
		}
		return max;		
	}

}
