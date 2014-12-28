package com.thermostat.security;

/**
 * Store allowing to query previously registered public key by their hash
 */
public interface KeyHashStore {

	/**
	 * query the public based on its fingerpprint
	 * 
	 * @param keyHash the hash of the public key
	 * @return the public having the given hash
	 */
	String getPublicKey(String keyHash);
}
