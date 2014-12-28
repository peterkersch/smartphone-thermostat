package com.thermostat.server.configuration;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -3918050616496826269L;

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
