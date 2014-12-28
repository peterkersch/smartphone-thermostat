package com.blackbird.thermostat.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import android.content.Context;

import com.thermostat.security.KeyPairGenericGenerator;
import com.thermostat.security.KeyPairSerializer;
import com.thermostat.security.KeyPairStore;
import com.thermostat.security.KeyPairStoreInterface;
import com.thermostat.security.rsa.RSAKeyPairGenerator;
import com.thermostat.security.rsa.RSAKeyPairSerializer;

public class RSAKeyPairStoreSharedPreferences extends KeyPairStore<RSAPrivateKey, RSAPublicKey> {

	private static final int DEFAULT_KEY_SIZE = 2048;
	
	private RSAKeyPairSerializer serializer = new RSAKeyPairSerializer();
	
	private RSAKeyPairGenerator generator;
	
	private SharedPreferencesKeyPairStore store;
	
	public RSAKeyPairStoreSharedPreferences(int keySize, Context context) {
		generator = new RSAKeyPairGenerator(keySize);
		store = new SharedPreferencesKeyPairStore(context);
	}
	
	public RSAKeyPairStoreSharedPreferences(Context context) {
		this(DEFAULT_KEY_SIZE, context);
	}
	
	@Override
	public KeyPairGenericGenerator<RSAPrivateKey, RSAPublicKey> getKeyPairGenerator() {
		return generator;
	}

	@Override
	public KeyPairSerializer<RSAPrivateKey, RSAPublicKey> getKeyPairSerializer() {
		return serializer;
	}

	@Override
	public KeyPairStoreInterface getStore() {
		return store;
	}
}
