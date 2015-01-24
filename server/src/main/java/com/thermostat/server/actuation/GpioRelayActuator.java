package com.thermostat.server.actuation;

import java.io.IOException;

import com.thermostat.server.actuation.gpio.GpioDirection;
import com.thermostat.server.actuation.gpio.GpioPort;
import com.thermostat.server.temperature.TemperatureHistory;

/**
 * Actuator driving a simple relay via a GPIO port.
 */
public class GpioRelayActuator extends Actuator {

	private static final double DEFAULT_HISTERESIS = 0.1;
	
	private double histeresis;
	
	private GpioPort gpioPort;
	
	/** True if heating or cooling is active */
	private boolean hvacStatus;
	
	/**
	 * Initializes a new GpioActuator instance
	 * 
	 * @param id the ID of the GPIO port to be handled
	 * @param histeresis
	 */
	public GpioRelayActuator(int id, double histeresis) throws IOException {
		this.histeresis = histeresis;
		
		gpioPort = new GpioPort(id, GpioDirection.OUT);
		hvacStatus = false;
	}
	
	public GpioRelayActuator(int id) throws IOException {
		this(id, DEFAULT_HISTERESIS);
	}
	
	public synchronized void update(TemperatureHistory temperatureHistory) throws IOException {
		double ambientTemperature = temperatureHistory.getCurrentAmbientTemperature();
		double targetTemperature = temperatureHistory.getCurrentTargetTemperature();
		double diff = targetTemperature - ambientTemperature;
		if (diff <= 0 && hvacStatus) {
			// Target temperature reached, turn off heating
			gpioPort.set(false);
			hvacStatus = false;
		} else if (diff > histeresis && !hvacStatus) {
			// Difference to target temperature exceeds hysteresis threshold, start heating again
			gpioPort.set(true);
			hvacStatus = true;
		}
	}

	public double getStatus() throws IOException {
		return hvacStatus ? 1.0 : 0.0;
	}

	@Override
	public String toString() {
		return super.toString() + " on port " + gpioPort.getId();
	}
}
