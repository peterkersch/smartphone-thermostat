package com.thermostat.protocol.message;

import com.thermostat.protocol.ThermostatMessageType;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ResidentStatusInfo;

public class StateUpdateMessage extends ThermostatMessageInitiator {
	
	private static final long serialVersionUID = 4650690085336993005L;

	private ResidentState state;
	
	private ResidentStatusInfo status;
	
	public StateUpdateMessage() {
		// required for serialization
	}
	
	public StateUpdateMessage(String fingerprint, ResidentState state, ResidentStatusInfo status) {
		super(ThermostatMessageType.STATE_UPDATE, fingerprint);
		this.state = state;
		this.status = status;
	}
	
	public ResidentState getState() {
		return state;
	}
	
	public ResidentStatusInfo getStatus() {
		return status;
	}
}
