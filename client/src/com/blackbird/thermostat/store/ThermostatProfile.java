package com.blackbird.thermostat.store;

import java.util.Collections;
import java.util.Map;

import com.blackbird.thermostat.protocol.ThermostatServerIdentifier;
import com.thermostat.technology.Technology;

/**
 * Contains information for a given thermostat
 */
public class ThermostatProfile {

	private Map<Technology, ThermostatServerIdentifier> serverIdentifiers;
	
	private String publicKey;
	
	private String fingerprint;
	
	public ThermostatProfile(String publicKey, String fingerprint, Map<Technology, ThermostatServerIdentifier> serverIdentifiers) {
		this.publicKey = publicKey;
		this.fingerprint = fingerprint;
		this.serverIdentifiers = serverIdentifiers;
	}
	
	public String getPublicKey() {
		return publicKey;
	}
	
	public String getFingerprint() {
		return fingerprint;
	}
	
	public Map<Technology, ThermostatServerIdentifier> getServerIdentifiers() {
		return Collections.unmodifiableMap(serverIdentifiers);
	}
	
	@Override
	public int hashCode() {
		return fingerprint.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ThermostatProfile) {
			return fingerprint.equals(((ThermostatProfile)o).fingerprint);
		} else {
			return false;
		}
	}
}
