package com.thermostat.protocol.security;

import java.security.SecureRandom;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageType;

public class CryptographicIdMessage extends ThermostatMessage {

	private static final long serialVersionUID = -5813163519990922534L;

	private String cryptographicId;
	
	private byte[] challenge;
	
	protected CryptographicIdMessage() {
		// required for serialization
	}
	
	public CryptographicIdMessage(String cryptographicId) {
		super(ThermostatMessageType.CRYPTOGRAPHIC_ID);
		this.cryptographicId = cryptographicId;
		SecureRandom random = new SecureRandom();
	    challenge = new byte[20];
	    random.nextBytes(challenge);
	}

	public String getcCryptographicId() {
		return cryptographicId;
	}
	
	public byte[] getChallenge() {
		return challenge;
	}
}