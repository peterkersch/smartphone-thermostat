package com.thermostat.server.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.thermostat.security.KeyPairStoreInterface;

public class PropertiesKeyPairStore implements KeyPairStoreInterface {

	private static final String PRIVATE_KEY_PROPERTY_NAME = "keypair.private";
	private static final String PUBLIC_KEY_PROPERTY_NAME = "keypair.public";
	
	private String propertiesFilePath;
	
	private Properties properties = new Properties();
	
	public PropertiesKeyPairStore(String propertiesFilePath) throws IOException {
		this.propertiesFilePath = propertiesFilePath;
		File f = new File(propertiesFilePath);
		if (f.exists()) {
			properties.load(new FileInputStream(propertiesFilePath));
		}
	}
	
	public String getPrivateKeyString() {
		return properties.getProperty(PRIVATE_KEY_PROPERTY_NAME);
	}

	public String getPublicKeyString() {
		return properties.getProperty(PUBLIC_KEY_PROPERTY_NAME);
	}

	public void storePrivateKeyString(String privateKeyString) {
		properties.setProperty(PRIVATE_KEY_PROPERTY_NAME, privateKeyString);
	}

	public void storePublicKeyString(String publicKeyString) {
		properties.setProperty(PUBLIC_KEY_PROPERTY_NAME, publicKeyString);
	}

	public void commit() throws IOException {
		properties.store(new FileOutputStream(propertiesFilePath), "");
	}
}
