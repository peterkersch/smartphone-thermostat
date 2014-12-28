package com.thermostat.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import com.thermostat.security.util.HexUtils;

/**
 * Provides public key cryptography (encryption / decryption) using the own key pair of the device 
 * 
 * @param <R>
 * @param <U>
 */
public class ThermostatSecurityManager <R extends PrivateKey, U extends PublicKey> {
	
	private KeyPairStore<R, U> keyPairStore;
	
	private KeyHashStore keyHashStore;

	public ThermostatSecurityManager(KeyPairStore<R, U> keyPairStore, KeyHashStore keyHashStore) {
		this.keyPairStore = keyPairStore;
		this.keyHashStore = keyHashStore;
	}

	/**
	 * Returns own public key.
	 * 
	 * @return a string representation of the own public key
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public String getPublicKeyString() throws GeneralSecurityException, IOException {
		return keyPairStore.getKeyPairSerializer().toString(keyPairStore.getPublicKey());
	}
	
	/**
	 * Get the public key for the given public key fingerprint
	 * 
	 * @param fingerprint
	 * @return
	 */
	public String getPublicKeyString(String fingerprint) {
		return keyHashStore.getPublicKey(fingerprint);
	}
	
	/**
	 * Get fingerprint of own public key
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public String getPublicKeyFingerprint() throws GeneralSecurityException, IOException {
		return KeyHasher.getFingerprint(keyPairStore.getPublicKey());
	}
		
	/**
	 * Get fingerprint of a given public key
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public String getPublicKeyFingerprint(String publicKey) throws GeneralSecurityException, IOException {
		return KeyHasher.getFingerprint(keyPairStore.getKeyPairSerializer().toPublicKey(publicKey));
	}

	/**
	 * Encrypts string data using the public key of the communicating party.
	 * Encryption follows the following process:
	 * String ->  byte [] (using UTF-8 encoding) -> byte [] (encrypted) -> hex String (encrypted)
	 * 
	 * @param data
	 * @param publicKeyString the string representation of the public key from the communicating party
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String encrypt(String data, String publicKeyString) throws GeneralSecurityException, UnsupportedEncodingException {
		final Cipher cipher = Cipher.getInstance(keyPairStore.getKeyPairGenerator().getAlgorithm());
		PublicKey publicKey = keyPairStore.getKeyPairSerializer().toPublicKey(publicKeyString);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte [] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
		return HexUtils.byteToHex(encryptedBytes);
	}
	
	/**
	 * Decrypt data using the own private key.
	 * Decryption follows the following process:
	 * hex String (encrypted) -> byte [] (encrypted) -> byte [] (decrypted) -> String (assuming UTF-8 byte representation)
	 * 
	 * @param encryptedData the encrypted data as a hex String
	 * @return the decrypted original String
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	public String decrypt(String encryptedData) throws GeneralSecurityException, IOException {
		final Cipher cipher = Cipher.getInstance(keyPairStore.getKeyPairGenerator().getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, keyPairStore.getPrivateKey());
		byte [] decryptedBytes = cipher.doFinal(HexUtils.hexToByte(encryptedData));
		return new String(decryptedBytes, "UTF-8");
	}
}
