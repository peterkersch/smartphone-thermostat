package com.thermostat.server.zone;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;

import com.thermostat.protocol.data.ZoneData;

/**
 * Interface for getting zone manager instances for different HVAC zones. 
 */
public interface ZoneRegistry {

	ZoneManager getZoneConfiguration(String name);
	
	Set<String> getZoneNames();
	
	Collection<ZoneManager> getZoneManagers();
	
	ArrayList<ZoneData> getZoneData();
}
