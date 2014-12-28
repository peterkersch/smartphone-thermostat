package com.thermostat.server.temperature;

import java.io.IOException;

public interface TemperatureSensor {

	/**
	 * Read current ambient temperature from the sensor
	 * 
	 * @return the current ambient temperature in degree Celsius 
	 */
	float getTemperature() throws IOException;
}
