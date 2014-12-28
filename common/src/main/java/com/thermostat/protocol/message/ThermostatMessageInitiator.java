package com.thermostat.protocol.message;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageType;

/**
 * Superclass for thermostat messages initiating a message sequence.
 * All of these initiator messages contain a fingerprint attribute derived from
 * the sender's public key using a cryptographic hash functions.
 * This fingerprint is used to identify the sender on the receiver side 
 * independent of communication medium (Bluetooth, WiFi, etc.) 
 */
public class ThermostatMessageInitiator extends ThermostatMessage {

	private static final long serialVersionUID = 9159756485252205931L;

	private String fingerprint;
	
	public ThermostatMessageInitiator() {
		// required for serialization
	}
	
	public ThermostatMessageInitiator(ThermostatMessageType type, String fingerprint) {
		super(type);
		this.fingerprint = fingerprint;
	}
	
	public String getFingerprint() {
		return fingerprint;
	}
}
