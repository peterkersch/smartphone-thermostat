package com.thermostat.server.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

import com.thermostat.security.ThermostatSecurityManager;
import com.thermostat.server.configuration.Configuration;
import com.thermostat.server.profile.ResidentProfileStoreFactory;

public class ThermostatServerSecurityManager extends ThermostatSecurityManager<RSAPrivateKey, RSAPublicKey> {

	private static Logger logger = Logger.getLogger(ThermostatServerSecurityManager.class.getSimpleName());

	private static final String KEY_STORE_FILE_NAME = "rsa.properties";
	
	private static ThermostatServerSecurityManager instance;
	
	public static ThermostatServerSecurityManager getInstance() throws IOException, GeneralSecurityException {
		if (instance == null) {
			instance = new ThermostatServerSecurityManager();
		}
		return instance;
	}
	
	private ThermostatServerSecurityManager() throws IOException, GeneralSecurityException {
		super(new RSAKeyPairStoreInPropertiesFile(Configuration.getConfigPath(KEY_STORE_FILE_NAME).getAbsolutePath()), 
				ResidentProfileStoreFactory.getResidentProfileStore());
		logger.info("Public key: " + getPublicKeyString());
		logger.info("Public key fingerprint: " + getPublicKeyFingerprint());
	}
}
