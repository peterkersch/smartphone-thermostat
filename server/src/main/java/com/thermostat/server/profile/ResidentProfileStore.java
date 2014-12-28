package com.thermostat.server.profile;

import java.util.Map;

import com.thermostat.security.KeyHashStore;

/**
 * Abstract class for persistent storage of resident profiles
 */
public abstract class ResidentProfileStore implements KeyHashStore {

	/**
	 * Register profile for a new resident
	 * 
	 * @param publicKey
	 */
	public abstract void registerProfile(String publicKey);
	
	/**
	 * Remove profile for the resident with the given smartphone's crytographic id.
	 * 
	 * @param id
	 */
	public abstract void deleteProfile(String id);
	
	/**
	 * Retrieve all registered profiles in a Map
	 * 
	 * @return a cryptographic ID - > ResidentProfile Map
	 */
	public abstract Map<String, ResidentProfile> getProfiles();
	
	/**
	 * Get profile for the resident with the given cryptographic ID.
	 * 
	 * @param id
	 * @return
	 */
	public ResidentProfile getProfile(String id) {
		return getProfiles().get(id);
	}
	
	public String getPublicKey(String keyHash) {
		ResidentProfile profile = getProfile(keyHash);
		return profile == null ? null : profile.getPublicKeyString();
	}
	
}
