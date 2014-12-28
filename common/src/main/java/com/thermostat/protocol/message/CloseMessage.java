package com.thermostat.protocol.message;

import com.thermostat.protocol.ThermostatMessage;
import com.thermostat.protocol.ThermostatMessageType;

public class CloseMessage extends ThermostatMessage {
	
	private static final long serialVersionUID = 8788684401620395886L;

	public CloseMessage() {
		super(ThermostatMessageType.CLOSE);
	}
}

