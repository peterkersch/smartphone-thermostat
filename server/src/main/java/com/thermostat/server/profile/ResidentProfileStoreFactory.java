package com.thermostat.server.profile;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ResidentProfileStoreFactory {

	private static Logger logger = Logger.getLogger(ResidentProfileStoreFactory.class.getSimpleName());

	private static ResidentProfileStore profileStore;
	
	static {
		try {
			profileStore = new PropertiesProfileStore();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error when initializing " + ResidentProfileStoreFactory.class.getSimpleName(), e);
			System.exit(-1);
		}
	}
	
	public static ResidentProfileStore getResidentProfileStore() {
		return profileStore;
	}
}
