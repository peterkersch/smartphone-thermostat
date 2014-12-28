package com.thermostat.protocol;

public enum ThermostatMessageType {
	
	/**
	 * Contains 
	 * 		1) public key used to encrypt all subsequent messages
	 * 		2) name
	 * 		3) Bluetooth and / or WiFi MAC addresses
	 * Sent only once during the registration process.
	 */
	REGISTRATION,
	
	/**
	 * Contains the cryptographic ID of the sender (fingerprint of public key).
	 * Sent at the beginning of every message sequence.
	 */
	CRYPTOGRAPHIC_ID,
	
	/** 
	 * Contains current (and optionally future) state information
	 * Sent periodically by the residents' smartphone application + whenever resident control state changes 
	 */
	STATE_UPDATE,  

	/**
	 * Contains current state information about each zone (ambient temperature, target temperature).
	 * Sent periodically by the thermostat server in response to state update messages.
	 */
	ZONE_INFO,

	/**
	 * Contains an action to change target temperature of zones.
	 * Sent by the thermostat client on the resident's smartphone.
	 */
	ZONE_ACTION,
	
	PREFERENCES_UPDATE,
	PREFERENCES_INFO,
	
	/**
	 * Empty message sent to close the communication sequence.
	 */
	CLOSE;
}
