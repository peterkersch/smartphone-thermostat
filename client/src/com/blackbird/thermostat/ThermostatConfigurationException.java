package com.blackbird.thermostat;

public class ThermostatConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 7202608599930927468L;

	public ThermostatConfigurationException(String message) {
		super(message);
	}

	public ThermostatConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
