package com.thermostat.protocol.data;

import java.io.Serializable;

public class ZoneData implements Serializable {

	private static final long serialVersionUID = -8992501246812951413L;

	public String displayName;

	public Float currentTemperature;
	
	public Float targetTemperature;
	
	public Float boostTemperature;
	
	public ZoneData(String displayName, Float currentTemperature, Float targetTemeperature, Float boostTemperature) {
		this.displayName = displayName;
		this.currentTemperature = currentTemperature;
		this.targetTemperature = targetTemeperature;
		this.boostTemperature = boostTemperature;
	}
	
	public ZoneData(String displayName, Float currentTemperature, Float targetTemeperature) {
		this(displayName, currentTemperature, targetTemeperature, null);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ZoneData) {
			return displayName.equals(((ZoneData)o).displayName);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return displayName.hashCode();
	}
}
