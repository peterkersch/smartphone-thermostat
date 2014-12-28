package com.thermostat.security;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.thermostat.security.util.HexUtils;

/**
 * Generates cryptographic hash of a Key. 
 */
public class KeyHasher {

	public static final String DEFAULT_HASH_ALGORITHM = "SHA-256";
	
	/**
	 * Computes cryptographic hash of a Key object
	 * 
	 * @param key to Key whose hash is computed
	 * @param algorithm the standard Java name of the cryptographic hash algorithm used
	 * @return the computed hash as a HEX string
	 * @throws NoSuchAlgorithmException when an invalid algorithm is specified 
	 */
	public static final String getFingerprint(Key key, String algorithm) throws NoSuchAlgorithmException  {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		byte[] hash = digest.digest(key.getEncoded());
		return HexUtils.byteToHex(hash);
	}

	/**
	 * Computes cryptographic hash of a Key object using the default hash algorithm.
	 * 
	 * @param key to Key whose hash is computed
	 * @return the computed hash as a HEX string
	 * @throws NoSuchAlgorithmException when an invalid algorithm is specified 
	 */
	public static final String getFingerprint(Key key) throws NoSuchAlgorithmException  {
		return getFingerprint(key, DEFAULT_HASH_ALGORITHM);
	}

}
