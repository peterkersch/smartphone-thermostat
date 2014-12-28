package com.thermostat.protocol.data;

import java.io.Serializable;

/** 
 * Describes possible states of residents from a heating / cooling perspective.
 */
public class ResidentState implements Serializable {
	
	private static final long serialVersionUID = 7428776539707249917L;

	public static final ResidentState HOME_AWAKE = new ResidentState("Awake");
	public static final ResidentState HOME_SLEEPING = new ResidentState("Sleeping");
	public static final ResidentState AWAY = new ResidentState("Away");

	private String name;
	
	public ResidentState() {
		// required for serialization
	}
	
	public ResidentState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ResidentState) {
			ResidentState other = (ResidentState)o;
			return name.equals(other.name);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
