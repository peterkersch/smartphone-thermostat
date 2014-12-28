package com.thermostat.server.temperature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 1-wire temperature sensor implementation for Raspberry pi. 
 */
public class OneWireTemperatureSensor implements TemperatureSensor {

	private static final String SENSOR_BASE_DIR = "/sys/bus/w1/devices";
	private static final String SENSOR_FILE_NAME = "w1_slave";
	
	private String id;
	
	private float temperatureCorrection;
	
	/** Proc file to read temperature data from */
	private File proceFile;
	
	/**
	 * Creates a new temperature sensor object
	 * 
	 * @param id the unique ID of the 1-wire sensor
	 */
	public OneWireTemperatureSensor(String id, float temperatureCorrection) {
		this.id = id;
		this.temperatureCorrection = temperatureCorrection;
		proceFile = new File(SENSOR_BASE_DIR + "/" + id + "/" + SENSOR_FILE_NAME);
	}
	
	public OneWireTemperatureSensor(String id) {
		this(id, 0.0f);
	}
	
	public float getTemperature() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(proceFile)));
		String firstLine = reader.readLine();
		String secondLine = reader.readLine();
		reader.close();
		if (firstLine == null || secondLine == null) {
			throw new IOException("1-wire sensor " + id + " reading returned less than 2 lines");
		}
		if (!firstLine.contains("YES")) {
			throw new IOException("CRC error when reading from 1-wire sensor " + id);
		}
		String t = secondLine.replaceFirst("^.*t=", "");
		return Float.parseFloat(t) / 1000 + temperatureCorrection;
	}

}
