package com.thermostat.protocol.message;

import java.util.ArrayList;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageType;
import com.thermostat.protocol.data.ZoneData;

public class ZoneDataMessage extends ThermostatMessage {

	private static final long serialVersionUID = 4730434022412547771L;

	private ArrayList<ZoneData> zones;
	
	public ZoneDataMessage() {
		// required for serialization
	}
	
	public ZoneDataMessage(ArrayList<ZoneData> zones) {
		super(ThermostatMessageType.ZONE_INFO);
		this.zones = zones;
	}

	public ArrayList<ZoneData> getZones() {
		return zones;
	}
}
