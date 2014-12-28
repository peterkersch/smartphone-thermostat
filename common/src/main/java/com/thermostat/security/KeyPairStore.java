package com.thermostat.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Provides storage and retrieval methods for cryptographic key pairs.
 * Key generation and initialization of persistent key store is performed during the first run.
 *  
 * @param <R> the Class for private key
 * @param <U> the class for public key
 */
public abstract class KeyPairStore<R extends PrivateKey, U extends PublicKey> {

	/** Cached private key string */
	private String privateKeyString = null;
	
	/** Cached public key string */
	private String publicKeyString = null;
	
	/** Cached key pair */
	private KeyPairGeneric<R, U> keyPair = null;
	
	/**
	 * Get the key pair generator to generate new key pairs
	 */
	public abstract KeyPairGenericGenerator<R, U> getKeyPairGenerator();
	
	/**
	 * Get the KeyPairSerializer instance used for Key <-> String conversions
	 * 
	 * @return the KeyPairSerializer instance associated with this class
	 */
	public abstract KeyPairSerializer<R, U> getKeyPairSerializer();

	/**
	 * Get the persistent store object storing keys.
	 */
	public abstract KeyPairStoreInterface getStore();
	
	private void readFromStore() throws GeneralSecurityException, IOException {
		privateKeyString = getStore().getPrivateKeyString();
		publicKeyString = getStore().getPublicKeyString();
//		System.out.println("public: " + publicKeyString + ", private: " + privateKeyString);
		if (privateKeyString != null && publicKeyString != null) {
			R privateKey = getKeyPairSerializer().toPrivateKey(privateKeyString);
			U publicKey = getKeyPairSerializer().toPublicKey(publicKeyString);
			keyPair = new KeyPairGeneric<R,U>(privateKey, publicKey);
		} else {
			initializeStore();
		}
	}
	
	private void initializeStore() throws GeneralSecurityException, IOException {
		keyPair = getKeyPairGenerator().generateKeyPair();
		privateKeyString = getKeyPairSerializer().toString(keyPair.privateKey);
		publicKeyString = getKeyPairSerializer().toString(keyPair.publicKey);
		getStore().storePrivateKeyString(privateKeyString);
		getStore().storePublicKeyString(publicKeyString);
		getStore().commit();
	}
	
	/**
	 * Get the stored public key
	 * @throws GeneralSecurityException 
	 * @throws IOException 
	 */
	public U getPublicKey() throws GeneralSecurityException, IOException {
		if (keyPair == null) {
			readFromStore();
		}
		return keyPair.publicKey;
	}

	/**
	 * Get the stored private key
	 * @throws GeneralSecurityException 
	 * @throws IOException 
	 */
	public R getPrivateKey() throws GeneralSecurityException, IOException {
		if (keyPair == null) {
			readFromStore();
		}
		return keyPair.privateKey;
	}	
}
