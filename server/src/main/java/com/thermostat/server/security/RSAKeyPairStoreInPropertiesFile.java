package com.thermostat.server.security;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.thermostat.security.KeyPairGenericGenerator;
import com.thermostat.security.KeyPairSerializer;
import com.thermostat.security.KeyPairStore;
import com.thermostat.security.KeyPairStoreInterface;
import com.thermostat.security.rsa.RSAKeyPairGenerator;
import com.thermostat.security.rsa.RSAKeyPairSerializer;

public class RSAKeyPairStoreInPropertiesFile extends KeyPairStore<RSAPrivateKey, RSAPublicKey> {

	private static final int DEFAULT_KEY_SIZE = 2048;
	
	private RSAKeyPairSerializer serializer = new RSAKeyPairSerializer();
	
	private RSAKeyPairGenerator generator;
	
	private PropertiesKeyPairStore store;
	
	public RSAKeyPairStoreInPropertiesFile(int keySize, String propertiesFilePath) throws IOException {
		generator = new RSAKeyPairGenerator(keySize);
		store = new PropertiesKeyPairStore(propertiesFilePath);
	}
	
	public RSAKeyPairStoreInPropertiesFile(String propertiesFilePath) throws IOException {
		this(DEFAULT_KEY_SIZE, propertiesFilePath);
	}
	
	@Override
	public KeyPairSerializer<RSAPrivateKey, RSAPublicKey> getKeyPairSerializer() {
		return serializer; 
	}

	@Override
	public KeyPairGenericGenerator<RSAPrivateKey, RSAPublicKey> getKeyPairGenerator() {
		return generator;
	}

	@Override
	public KeyPairStoreInterface getStore() {
		return store;
	}
	
}
