package com.thermostat.protocol.message;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageType;

public class RegistrationMessage extends ThermostatMessage {

	private static final long serialVersionUID = -6625574447446407740L;

	private String publicKey;
	
	private String name;
	
	private String deviceType;
	
	private String bluetoothAddress;
	
	private String wifiAddress;
	
	protected RegistrationMessage() {
		// required for serialization
	}
	
	public RegistrationMessage(String publicKey, String name, String deviceType, String bluetoothAddress, String wifiAddress) {
		super(ThermostatMessageType.REGISTRATION);
		this.publicKey = publicKey;
		this.name = name;
		this.deviceType = deviceType;
		this.bluetoothAddress = bluetoothAddress;
		this.wifiAddress = wifiAddress;
	}

	public String getPublicKey() {
		return publicKey;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDeviceType() {
		return deviceType;
	}
	
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	
	public String getWifiAddress() {
		return wifiAddress;
	}
}
