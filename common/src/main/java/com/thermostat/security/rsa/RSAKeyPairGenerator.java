package com.thermostat.security.rsa;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.thermostat.security.KeyPairGeneric;
import com.thermostat.security.KeyPairGenericGenerator;

public class RSAKeyPairGenerator extends KeyPairGenericGenerator<RSAPrivateKey, RSAPublicKey> {

	public RSAKeyPairGenerator(int keySize) {
		super(keySize);
	}
	
	public KeyPairGeneric<RSAPrivateKey, RSAPublicKey> generateKeyPair() throws GeneralSecurityException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(getKeySize());
		KeyPair kp = kpg.genKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey)kp.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();
		return new KeyPairGeneric<RSAPrivateKey, RSAPublicKey>(privateKey, publicKey);
	}

	@Override
	public String getAlgorithm() {
		// Specifying simply "RSA" results into incompatibility between Java and Android
		return "RSA/ECB/PKCS1Padding";
	}
}
