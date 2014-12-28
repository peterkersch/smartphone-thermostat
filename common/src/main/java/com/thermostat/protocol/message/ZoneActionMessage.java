package com.thermostat.protocol.message;

import com.thermostat.protocol.ThermostatMessageType;
import com.thermostat.protocol.data.ZoneData;

public class ZoneActionMessage extends ThermostatMessageInitiator {

	private static final long serialVersionUID = 4730434022412547771L;

	private ZoneData zoneData;
	
	public ZoneActionMessage() {
		// required for serialization
	}
	
	public ZoneActionMessage(String fingerprint, ZoneData zoneData) {
		super(ThermostatMessageType.ZONE_ACTION, fingerprint);
		this.zoneData = zoneData;
	}

	public ZoneData getZone() {
		return zoneData;
	}	
}
