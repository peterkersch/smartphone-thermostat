package com.thermostat.server.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.thermostat.protocol.data.ResidentState;
import com.thermostat.server.configuration.Configuration;
import com.thermostat.server.security.ThermostatServerSecurityManager;

/**
 * Implements a resident profile store using properties files.
 * There is a separate properties file for each resident.
 * 
 * TODO: add update operations
 */
public class PropertiesProfileStore extends ResidentProfileStore {

	private static Logger logger = Logger.getLogger(PropertiesProfileStore.class.getSimpleName());

	private static final String PROPERTY_NAME_PUBLIC_KEY = "key.public";
	private static final String PROPERTY_NAME_BLUETOOTH_ADDRESS = "bluetooth.address";
	private static final String PROPERTY_NAME_BLUETOOTH_NAME = "bluetooth.name";
	private static final String PROPERTY_NAME_LAST_TIMESTAMP_MS = "last.timestamp";
	private static final String PROPERTY_NAME_LAST_STATE = "last.state";
	
	private static final String RESIDENT_CONF_DIR = "residents";
	
	private static final String TEMPERATURE_PREFERENCES_CONF = "preferences.txt";

	/** Cryptographic ID of the resident's smartphone -> resident's profile */
	private Map<String, ResidentProfile> profiles = new HashMap<String, ResidentProfile>();

	/** Cryptographic ID of the resident's smartphone -> properties */
	private Map<String, Properties> properties = new HashMap<String, Properties>();
	
	private ThermostatServerSecurityManager securityManager;

	/**
	 * Load profiles from properties files.
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws GeneralSecurityException 
	 */
	public PropertiesProfileStore() throws IOException, NumberFormatException, GeneralSecurityException {
		securityManager = ThermostatServerSecurityManager.getInstance();
		File baseDirectory = Configuration.getConfigPath(RESIDENT_CONF_DIR);
		if (!baseDirectory.exists()) {
			boolean result = baseDirectory.mkdir();
			// TODO: check result
		}
		
		// Parse properties files for each resident
		for (String id : baseDirectory.list()) {
			Properties p = new Properties();
			p.load(new FileInputStream(new File(baseDirectory, id)));
			ResidentProfile profile = propertiesToProfile(p);
			String fingerprint = securityManager.getPublicKeyFingerprint(profile.getPublicKeyString());
			properties.put(fingerprint, p);
			profiles.put(fingerprint, profile);
			logger.info("Read profile info from " + id);
		}
		
		// Parse temperature preferences
		File temperaturePreferences = Configuration.getConfigPath(TEMPERATURE_PREFERENCES_CONF);
		if (temperaturePreferences.exists()) {
			temperaturePreferences.createNewFile();
			// TODO: check result
		}
		BufferedReader br = new BufferedReader(new FileReader(temperaturePreferences));
		String line;
		while ((line = br.readLine()) != null) {
			String s[] = line.split("\\t");
			if (s.length != 4) {
				logger.warning("Invalide temperature preference entry: " + line);
				continue;
			}
			ResidentProfile profile = profiles.get(s[0]);
			if (profile == null) {
				logger.warning("No registered user with fingerprint " + s[0]);
				continue;
			}
			profile.setTemperaturePreference(s[1], new ResidentState(s[2]), Float.parseFloat(s[3]));
		}
		br.close();
	}
	
	private ResidentProfile propertiesToProfile(Properties p) throws NumberFormatException {
		String publicKey = p.getProperty(PROPERTY_NAME_PUBLIC_KEY);
		String bluetoothName = p.getProperty(PROPERTY_NAME_BLUETOOTH_NAME);
		String bluetoothAddress = p.getProperty(PROPERTY_NAME_BLUETOOTH_ADDRESS);
		ResidentState lastState = new ResidentState(p.getProperty(PROPERTY_NAME_LAST_STATE));
		long lastTimestampMs = Long.parseLong(p.getProperty(PROPERTY_NAME_LAST_TIMESTAMP_MS));
		ResidentProfile profile = new ResidentProfile(publicKey, bluetoothName, bluetoothAddress, lastTimestampMs, lastState);
		return profile;
	}
	
	public Map<String, ResidentProfile> getProfiles() {
		return Collections.unmodifiableMap(profiles);
	}
	
	public void registerProfile(String publicKey) {
		// TODO
	}
	
	public void deleteProfile(String id) {
		// TODO
	}
}
