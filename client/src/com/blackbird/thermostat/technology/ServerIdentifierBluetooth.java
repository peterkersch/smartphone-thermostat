package com.blackbird.thermostat.technology;

import com.blackbird.thermostat.protocol.ThermostatServerIdentifier;

public class ServerIdentifierBluetooth extends ThermostatServerIdentifier {

	private String bluetoothAddress;
	
	private String bluetoothName;
	
	public ServerIdentifierBluetooth(String bluetoothAddress, String bluetoothName) {
		this.bluetoothAddress = bluetoothAddress;
		this.bluetoothName = bluetoothName;
	}
	
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	
	public String getBluetoothName() {
		return bluetoothName;
	}
}
