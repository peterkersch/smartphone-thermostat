package com.thermostat.security;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Interface for converting key pairs to and from strings.
 *   
 * @param <R> the Class for private key
 * @param <U> the class for public key
 */
public interface KeyPairSerializer<R extends PrivateKey, U extends PublicKey> {

	String toString(R privateKey) throws GeneralSecurityException;

	String toString(U publicKey) throws GeneralSecurityException;
	
	R toPrivateKey(String s) throws GeneralSecurityException;
	
	U toPublicKey(String s) throws GeneralSecurityException;

}
