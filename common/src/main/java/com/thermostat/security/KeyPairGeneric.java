package com.thermostat.security;

/**
 * Simple key pair with given public and private key type
 *
 * @param <R> the Class for private key
 * @param <U> the class for public key
 */
public class KeyPairGeneric<R,U> {
	public R privateKey;
	public U publicKey;
	
	public KeyPairGeneric(R privateKey, U publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
}
