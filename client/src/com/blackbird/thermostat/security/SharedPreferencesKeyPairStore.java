package com.blackbird.thermostat.security;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.thermostat.security.KeyPairStoreInterface;

public class SharedPreferencesKeyPairStore implements KeyPairStoreInterface {

	private static final String PRIVATE_KEY_PROPERTY_NAME = "keypair.private";
	private static final String PUBLIC_KEY_PROPERTY_NAME = "keypair.public";

	private SharedPreferences preferences;
	
	private SharedPreferences.Editor editor;
	
	public SharedPreferencesKeyPairStore(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}	
	
	@Override
	public void commit() throws IOException {
		editor.commit();
	}

	@Override
	public String getPrivateKeyString() {
		return preferences.getString(PRIVATE_KEY_PROPERTY_NAME, null);
	}

	@Override
	public String getPublicKeyString() {
		return preferences.getString(PUBLIC_KEY_PROPERTY_NAME, null);
	}

	@Override
	public void storePrivateKeyString(String privateKeyString) {
		editor.putString(PRIVATE_KEY_PROPERTY_NAME, privateKeyString);
	}

	@Override
	public void storePublicKeyString(String publicKeyString) {
		editor.putString(PUBLIC_KEY_PROPERTY_NAME, publicKeyString);
	}

}
