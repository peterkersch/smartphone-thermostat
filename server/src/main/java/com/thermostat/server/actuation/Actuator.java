package com.thermostat.server.actuation;

import java.io.IOException;

import com.thermostat.server.temperature.TemperatureHistory;

/**
 * Controls heating (and optionally cooling) for a given zone.
 * TODO: implement here generic temperature control logic independent of actuator type.
 * E.g., open window detection.
 */
public abstract class Actuator {

	/**
	 * Receive temperature information updates.
	 * 
	 * @param temperatureHistory describes the evolution of ambient and target temperatures for the given zone
	 */
	public abstract void update(TemperatureHistory temperatureHistory) throws IOException;
	
	/**
	 * Get heating / cooling status for the given zone.
	 * 
	 * @return a number between -1 and +1.
	 * -1 means cooling at max power, 0 means no cooling nor heating while +1 means heating at max power
	 */
	public abstract double getStatus() throws IOException;
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
