package com.thermostat.server.zone;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoneRegistryFactory {

	private static Logger logger = Logger.getLogger(ZoneRegistryFactory.class.getSimpleName());

	private static ZoneRegistry zoneRegistry;
	
	static {
		try {
			zoneRegistry = new ZoneRegistryPropertiesFile();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error when initializing " + ZoneRegistryFactory.class.getSimpleName(), e);
			System.exit(-1);
		}
	}
	
	public static ZoneRegistry getZoneRegistry() {
		return zoneRegistry;
	}
}
