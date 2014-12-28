package com.thermostat.security;

import java.io.IOException;

public interface KeyPairStoreInterface {

	/**
	 * Get the stored private key string
	 * 
	 * @return the stored private key string or null if store is empty
	 */
	String getPrivateKeyString();

	/**
	 * Get the stored public key string
	 * 
	 * @return the stored public key string or null if store is empty
	 */
	String getPublicKeyString();
	
	/**
	 * Put the given key in the persisted store
	 * 
	 * @param privateKeyString the private key string to be stored
	 */
	void storePrivateKeyString(String privateKeyString);

	/**
	 * Put the given key in the persisted store
	 * 
	 * @param privateKeyString the public key string to be stored
	 */
	void storePublicKeyString(String publicKeyString);
	
	/** Commit changes in the persistent key store */
	void commit() throws IOException;

}
