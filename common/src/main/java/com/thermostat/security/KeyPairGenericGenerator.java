package com.thermostat.security;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Interface for generating key pairs with the given type
 *   
 * @param <R> the Class for private key
 * @param <U> the class for public key
 */
public abstract class KeyPairGenericGenerator<R extends PrivateKey, U extends PublicKey> {

	private int keySize;
	
	public KeyPairGenericGenerator(int keySize) {
		this.keySize = keySize;
	}
	
	public int getKeySize() {
		return keySize;
	}	

	/**
	 * Generate a new key pair.
	 * 
	 * @return the generated key pair
	 */
	public abstract KeyPairGeneric<R, U> generateKeyPair() throws GeneralSecurityException;

	/**
	 * Get the name of the asymmetric cryptographic algorithm corresponding to the key pair.  
	 * 
	 * @return the standard Java name of the lagorithm
	 */
	public abstract String getAlgorithm();
}
