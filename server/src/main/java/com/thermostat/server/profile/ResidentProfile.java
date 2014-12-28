package com.thermostat.server.profile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.thermostat.protocol.data.ResidentState;

/**
 * Describes properties and states of a resident 
 */
public class ResidentProfile {

	/** Default timeout in ms to be considered as having left the residence */
	private static final long DEFAULT_TIMEOUT_MS = 10 * 60 * 1000;
	
	/** Public key of the resident's smartphone used for authentication */
	private String publicKey;
	
	/** 
	 * The Bluetooth address of the resident's smartphone.
	 * MAC address in hexadecimal format 
	 * (using ":" separators but without trailing "0x").
	 */
	private String bluetoothAddress;
	
	/** The human readable assigned name of the resident's Bluetooth adapter */
	private String bluetoothName;
	
	/** Zone name -> State -> temperature preferences in degree Celsius. */
	private Map<String, Map<ResidentState, Float>> temperaturePreferences;
	
	/** The resident's state at the time of the last successful handshake */
	private ResidentState lastState;
	
	/** 
	 * The last successful handshake with the resident's smartphone.
	 * In Unix timestamp (milliseconds since the epoch) 
	 */
	private long lastTimestampMs;
	
	protected ResidentProfile(String publicKey, 
			String bluetoothName, 
			String bluetoothAddress) {
		this(publicKey, bluetoothName, bluetoothAddress, (new Date()).getTime(), ResidentState.HOME_AWAKE);
	}
	
	protected ResidentProfile(String publicKey, 
			String bluetoothName, 
			String bluetoothAddress,
			long lastTimestampMs,
			ResidentState lastState) {
		this.publicKey = publicKey;
		this.bluetoothName = bluetoothName;
		this.bluetoothAddress = bluetoothAddress;
		this.lastTimestampMs = lastTimestampMs;
		this.lastState = lastState;
		temperaturePreferences = new HashMap<String, Map<ResidentState,Float>>();
	}
	
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	
	public String getBluetoothName() {
		return bluetoothName;
	}
	
	public String getPublicKeyString() {
		return publicKey;
	}
	
	public ResidentState getLastState() {
		return lastState;
	}
	
	public long getLastTimestampMs() {
		return lastTimestampMs;
	}
	
	public ResidentState getCurrentState() {
		long currentTimestampMs = (new Date()).getTime();
		return (currentTimestampMs - lastTimestampMs > DEFAULT_TIMEOUT_MS) ?
				ResidentState.AWAY :
				lastState;
	}
	
	public Map<ResidentState, Float> getTemperaturePreferences(String zoneName) {
		return temperaturePreferences.get(zoneName);
	}
	
	/**
	 * Determine preferred temperature
	 * 
	 * @param zoneName
	 * @return
	 */
	public Float getPreferredTemperature(String zoneName) {
		Map<ResidentState, Float> zonePreferences = getTemperaturePreferences(zoneName);
		return zonePreferences == null ? null : zonePreferences.get(getCurrentState());
	}
	
	public void setTemperaturePreference(String zoneName, ResidentState state, Float targetTemperature) {
		Map<ResidentState, Float> zonePreferences = temperaturePreferences.get(zoneName);
		if (zonePreferences == null) {
			zonePreferences = new HashMap<ResidentState, Float>();
			temperaturePreferences.put(zoneName, zonePreferences);
		}
		zonePreferences.put(state, targetTemperature);
	}
	
	public void setBluetoothAddress(String bluetoothAddress) {
		this.bluetoothAddress = bluetoothAddress;
	}
	
	public void setBluetoothName(String bluetoothName) {
		this.bluetoothName = bluetoothName;
	}
	
	/**
	 * Update profile with current state information
	 * 
	 * @param state the current state of the resident
	 */
	public void updateState(ResidentState state) {
		lastState = state;
		lastTimestampMs = (new Date()).getTime();
	}
	
	/**
	 * Determine the target temperature (in Celsius) based on the resident's current state
	 * 
	 * @param zoneName the name of the zone for which
	 * @return the target temperature set for the current state or null if the resident is away or had not set any preferences for the given zone.
	 */
	public Float getTargetTemparature(String zoneName) {
		Map<ResidentState, Float> zonePreferences = temperaturePreferences.get(zoneName);
		if (zonePreferences == null) {
			return null;
		} else {
			long currentTimestampMs = (new Date()).getTime();
			ResidentState currentState = (currentTimestampMs - lastTimestampMs > DEFAULT_TIMEOUT_MS) ?
					ResidentState.AWAY :
					lastState;
			return currentState == ResidentState.AWAY ? 
					null : 
					zonePreferences.get(currentState);
		}
	}
}
