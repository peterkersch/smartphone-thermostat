package com.thermostat.server.zone;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.protocol.data.ZoneData;
import com.thermostat.server.actuation.Actuator;
import com.thermostat.server.temperature.TargetTemperatureCalculator;
import com.thermostat.server.temperature.TemperatureSensor;
import com.thermostat.server.temperature.TemperatureHistory;

/**
 * Manages ambient temperature of a given zone following a dynamically adjustable target temperature. 
 * Periodically checks on a separate thread the ambient temperature sensor and controls the HVAC actuator accordingly.  
 * 
 * TODO: handle both heating and cooling (currently, only heating is supported).
 */
public class ZoneManager extends Thread {

	private static Logger logger = Logger.getLogger(ZoneManager.class.getSimpleName());

	private static final long DEFAULT_UPDATE_PERIOD_SEC = 15;
	
	private static final long BOOST_TIMEOUT_SEC = 60*20;

	private String zoneName;
	
	private Actuator actuator;
	
	private TemperatureSensor sensor;
	
	private TemperatureHistory history = new TemperatureHistory();
	
	private long updatePeriod;
	
	private long lastSensorReading;
	
	private float ambientTemperature;
	
	private float targetTemperature;
	
	private float actuationTemperature;
	
	/** Temporal fixed target temperature */
	private Float boostTemparature = null;
	
	/** Unix timestamp when boostTemperature had been set for the last time */
	private long lastBoostSetting = 0;
	
	private boolean running = true;
	
	public ZoneManager(String zoneName, Actuator actuator, TemperatureSensor sensor, long updatePeriod) throws IOException {
		this.zoneName = zoneName;
		this.actuator = actuator;
		this.sensor = sensor;
		this.updatePeriod = updatePeriod;
		
		ambientTemperature = sensor.getTemperature();
		lastSensorReading = (new Date()).getTime();
	}
	
	public ZoneManager(String zoneName, Actuator actuator, TemperatureSensor sensor) throws IOException {
		this(zoneName, actuator, sensor, DEFAULT_UPDATE_PERIOD_SEC);
	}
	
	public synchronized void update() throws IOException {
		long timestamp = (new Date()).getTime();
		float previousActuationTemperature = actuationTemperature;
		actuationTemperature = TargetTemperatureCalculator.getTargetTemperature(this, true);
		
		/* Update only if 
		 * 		1) there are changes in resident states or 
		 * 		2) more than updatePeriod SEC elapsed since the last update 
		 */ 
		if (lastSensorReading + updatePeriod*1000 < timestamp || previousActuationTemperature != actuationTemperature) {
			targetTemperature = TargetTemperatureCalculator.getTargetTemperature(this, false);
			ambientTemperature = sensor.getTemperature();
			history.add(ambientTemperature, actuationTemperature);
			actuator.update(history);
			lastSensorReading = timestamp;
			logger.info(zoneName + "\t" + ambientTemperature + "\t" + actuationTemperature + "\t" + actuator.getStatus());
		}
	}
	
	@Override
	public void run() {
		while (running) {
			try {
				update();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Exception when performing zone update", e);
			}
			
			// Sleep 
			try {
				Thread.sleep(updatePeriod*1000);
			} catch (InterruptedException e) {
				logger.warning("Sleep interrupted: " + e.getMessage());
			}
		}
	}
	
	public void setBoostTemperature(Float boostTemparature) {
		this.boostTemparature = boostTemparature;
		lastBoostSetting = (new Date()).getTime();
	}
	
	public String getZoneName() {
		return zoneName;
	}
	
	public Float getBoostTemperature() {
		/** Verify timeout for boost temperature */
		if (boostTemparature != null && lastBoostSetting + BOOST_TIMEOUT_SEC*1000 < (new Date()).getTime()) {
			boostTemparature = null;
		}
		return boostTemparature;
	}
	
	public ZoneData getZoneData() {
		return new ZoneData(zoneName, ambientTemperature, targetTemperature, boostTemparature);
	}
}
