package com.blackbird.thermostat.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import android.content.Context;

import com.blackbird.thermostat.store.ThermostatProfileStore;
import com.thermostat.security.ThermostatSecurityManager;

public class ThermostatClientSecurityManager extends ThermostatSecurityManager<RSAPrivateKey, RSAPublicKey> {

	public ThermostatClientSecurityManager(Context context) {
		super(new RSAKeyPairStoreSharedPreferences(context), new ThermostatProfileStore());
	}

}
