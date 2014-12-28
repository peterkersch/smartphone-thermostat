package com.thermostat.security.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRandomGenerator {

	private static final int DEFAULT_RANDOM_BYTE_LENGTH = 32;
	
	private static SecureRandom randomGenerator;
	
	static {
		try {
			randomGenerator = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			System.exit(-1);
			e.printStackTrace();
		}
	}

	public static SecureRandom getRandomGenerator() {
		return randomGenerator;
	}
	
	/**
	 * Generate cryptographic random data 
	 * 
	 * @param length the length of the generate data in bytes
	 * @return the generated random bytes in hexadecimal format
	 */
	public static final String getRandomHexString(int length) {
		byte [] bytes = new byte[length];
		randomGenerator.nextBytes(bytes);
		return HexUtils.byteToHex(bytes);
	}
	
	/**
	 * Generate cryptographic random data with default length 
	 * 
	 * @return the generated random bytes in hexadecimal format
	 */
	public static final String getRandomHexString() {
		return getRandomHexString(DEFAULT_RANDOM_BYTE_LENGTH);
	}
	
}
