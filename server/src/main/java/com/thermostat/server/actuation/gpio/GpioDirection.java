package com.thermostat.server.actuation.gpio;

public enum GpioDirection {
	IN("in"),
	OUT("out");
	
	private String name;
	
	private GpioDirection(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
