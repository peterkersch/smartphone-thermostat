package com.thermostat.security.rsa;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.thermostat.security.KeyPairSerializer;

/**
 * Implements serialization and de-serialization of RSA key pairs
 */
public class RSAKeyPairSerializer implements KeyPairSerializer<RSAPrivateKey, RSAPublicKey> {

	public String toString(RSAPrivateKey privateKey) throws InvalidKeySpecException {
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
			return privateKeySpec.getModulus() + "," + privateKeySpec.getPrivateExponent();
		} catch (NoSuchAlgorithmException e) {
			// Should not happen
			e.printStackTrace();
			return null;
		}
	}

	public String toString(RSAPublicKey publicKey) throws InvalidKeySpecException {
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
			return publicKeySpec.getModulus() + "," + publicKeySpec.getPublicExponent();
		} catch (NoSuchAlgorithmException e) {
			// Should not happen
			e.printStackTrace();
			return null;
		}
	}

	public RSAPrivateKey toPrivateKey(String s) throws GeneralSecurityException {
		String [] components = s.split(",");
		if (components.length != 2) {
			throw new InvalidKeySpecException("Invalid Serialized format: " + s);
		}
		BigInteger modulus = new BigInteger(components[0]);
		BigInteger privateExponent = new BigInteger(components[1]);
		RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateExponent);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return (RSAPrivateKey)keyFactory.generatePrivate(privateKeySpec);
	}

	public RSAPublicKey toPublicKey(String s) throws GeneralSecurityException {
		String [] components = s.split(",");
		if (components.length != 2) {
			throw new InvalidKeySpecException("Invalid Serialized format: " + s);
		}
		BigInteger modulus = new BigInteger(components[0]);
		BigInteger publicExponent = new BigInteger(components[1]);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return (RSAPublicKey)keyFactory.generatePublic(publicKeySpec);
	}

}
