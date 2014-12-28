package com.thermostat.protocol;

import java.io.Serializable;

public abstract class ThermostatMessage implements Serializable {

	private static final long serialVersionUID = -8118882899859002841L;
	
	private ThermostatMessageType type;
	
	protected ThermostatMessage() {
		// required for serialization
	}
	
	public ThermostatMessage(ThermostatMessageType type) {
		this.type = type;
	}
	
	public ThermostatMessageType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "type=" + type;
	}
}
