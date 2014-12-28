package com.thermostat.protocol.data;

import java.io.Serializable;

public class BroadcastEvent implements Serializable {

	private static final long serialVersionUID = -420820481119557236L;
	
	/** Timestamp in milliseconds since the epoch */
	public long timestamp;
	
	/**
	 * Name of the event as defined by Intent.getAction().
	 * For intent with parameters, parameter might be appended to the action String after an underscore.
	 */
	public String name;
	
	public BroadcastEvent(long timestamp, String name) {
		this.timestamp = timestamp;
		this.name = name;
	}

	@Override
	public String toString() {
		return timestamp + ": " + name;
	}
}
